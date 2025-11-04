#!/bin/bash

echo "Initializing LocalStack SQS resources..."

# SQS 메인 큐 생성
awslocal sqs create-queue \
  --queue-name sqs-commerce \
  --attributes VisibilityTimeout=300,MessageRetentionPeriod=1209600

echo "Created sqs-commerce"

# DLQ 생성
awslocal sqs create-queue \
  --queue-name dlq-commerce \
  --attributes MessageRetentionPeriod=1209600

echo "Created dlq-commerce"

# DLQ ARN 가져오기
DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://sqs.ap-northeast-2.localhost.localstack.cloud:4566/000000000000/dlq-commerce \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "DLQ ARN: $DLQ_ARN"

# Redrive Policy 설정 (3회 실패 시 DLQ로 이동)
awslocal sqs set-queue-attributes \
  --queue-url http://sqs.ap-northeast-2.localhost.localstack.cloud:4566/000000000000/sqs-commerce \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"${DLQ_ARN}\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}"

echo "Configured Redrive Policy (maxReceiveCount: 3)"

# 큐 목록 확인
echo ""
echo "Available queues:"
awslocal sqs list-queues

echo ""
echo "LocalStack SQS initialization completed!"