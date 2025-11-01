import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

import {projectConfig} from "../config";

export interface S3BucketComponentArgs {
    bucketName: string;
    tags?: { [key: string]: string };
}

export class S3BucketComponent extends pulumi.ComponentResource {
    public readonly bucket: aws.s3.Bucket;
    public readonly bucketId: pulumi.Output<string>;
    public readonly bucketArn: pulumi.Output<string>;

    constructor(name: string, args: S3BucketComponentArgs, opts?: pulumi.ComponentResourceOptions) {
        super("commerce:s3:S3Bucket", name, {}, opts);

        // S3 버킷 생성
        this.bucket = this.createS3Bucket(name, args);

        // 서버 사이드 암호화 & 퍼블릭 액세스 차단
        this.configureEncryption(name);
        this.configurePublicAccessBlock(name);

        this.bucketId = this.bucket.id;
        this.bucketArn = this.bucket.arn;

        this.registerOutputs({
            bucketId: this.bucketId,
            bucketArn: this.bucketArn,
        });
    }

    private createS3Bucket(name: string, args: S3BucketComponentArgs) {
        const bucketName = `${args.bucketName}-${projectConfig.common.environment}`;
        return new aws.s3.Bucket(
            `${name}-bucket`,
            {
                bucket: bucketName,
                tags: args.tags,
            },
            {parent: this}
        );
    }

    private configureEncryption(name: string): void {
        new aws.s3.BucketServerSideEncryptionConfiguration(
            `${name}-encryption`,
            {
                bucket: this.bucket.id,
                rules: [
                    {
                        applyServerSideEncryptionByDefault: {
                            sseAlgorithm: "AES256",
                        },
                    },
                ],
            },
            {parent: this}
        );
    }

    private configurePublicAccessBlock(name: string): void {
        new aws.s3.BucketPublicAccessBlock(
            `${name}-public-access-block`,
            {
                bucket: this.bucket.id,
                blockPublicAcls: true,
                blockPublicPolicy: true,
                ignorePublicAcls: true,
                restrictPublicBuckets: true,
            },
            {parent: this}
        );
    }
}
