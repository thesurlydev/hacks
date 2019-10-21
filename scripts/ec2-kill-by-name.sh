#!/bin/bash

source common.sh

NAME_TAG="${1:-recon}"

echo "Getting InstanceId from $NAME_TAG"

RESULT=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=$NAME_TAG" --profile $PROFILE --region $REGION)

INSTANCE_ID="$(echo $RESULT | jq -r .Reservations[0].Instances[0].InstanceId)"

if [[ $INSTANCE_ID = null ]]; then
  echo "No instance found with name $NAME_TAG"
  exit 1
fi

echo "Found $INSTANCE_ID from $NAME_TAG"

echo "Killing $INSTANCE_ID"

aws ec2 terminate-instances --instance-ids "$INSTANCE_ID" --profile $PROFILE --region $REGION
