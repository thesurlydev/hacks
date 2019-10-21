#!/bin/bash

source common.sh

if [[ $1 = "--help" ]]; then
  echo "Usage: ./running-list"
  exit 1
fi

aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" --profile $PROFILE --region $REGION \
| jq -r .Reservations[].Instances[].Tags[].Value
