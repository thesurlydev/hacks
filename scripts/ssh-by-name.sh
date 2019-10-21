#!/bin/bash

source common.sh

NAME_TAG="${1:-recon}"
SSH_USER="${2:-ubuntu}"

if [[ $1 = "--help" ]]; then
  echo "Usage: ./ssh-by-name [NAME_TAG:recon] [SSH_USER:ubuntu]"
  exit 1
fi

echo "Finding instance with name $NAME_TAG"

# find running instance by name
PUBLIC_DNS=$(aws ec2 describe-instances \
--filters "Name=tag:Name,Values=$NAME_TAG" "Name=instance-state-name,Values=running" \
--profile $PROFILE --region $REGION \
| jq -r .Reservations[0].Instances[0].NetworkInterfaces[0].Association.PublicDnsName)


if [[ $PUBLIC_DNS = null ]]; then
  echo "No instance found with name $NAME_TAG"
  exit 1
fi

echo "ssh $PUBLIC_DNS ..."

# ssh to instance
ssh -i "$LOCAL_KEY_PATH" -o "StrictHostKeyChecking no" -o "LogLevel ERROR" "$SSH_USER@$PUBLIC_DNS"

