import * as pulumi from "@pulumi/pulumi";

interface ProjectConfig {
    common: Common;
    tags: Tags;
    s3Bucket: S3BucketConfig;
}

interface Common {
    environment: string
}

interface Tags {
    Environment: string;
    Project: string;
}

interface S3BucketConfig {
    bucketName: string;
    tags?: { [key: string]: string };
}

const config = new pulumi.Config("commerce");

export const projectConfig: ProjectConfig = {
    common: {
        environment: pulumi.getStack()
    },
    s3Bucket: {
        bucketName: config.require("bucketName"),
        tags: config.get("tags") ? JSON.parse(config.require("tags")) : undefined,
    },
    tags: {
        Environment: pulumi.getStack(),
        Project: "commerce",
    }
}