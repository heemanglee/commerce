import { S3BucketComponent } from "./components/s3Bucket";
import { projectConfig } from "./config";

const s3Bucket = new S3BucketComponent("commerce-bucket", {
    bucketName: projectConfig.s3Bucket.bucketName,
    tags: projectConfig.s3Bucket.tags,
});

export const s3BucketId = s3Bucket.bucketId;
export const s3BucketArn = s3Bucket.bucketArn;
