#!/bin/bash

LOCAL_KEY_PATH="$HOME/.ssh/YOURS_HERE.pem"
AWS_KEY_NAME="YOURS_HERE"
# see: https://ec2instances.info
#INSTANCE_TYPE="t2.micro" # $0.0116 per Hour
INSTANCE_TYPE="m5.2xlarge" # $0.384000 hourly
REGION="us-west-2"
PROFILE="futz"
SUBNET="YOURS_HERE"
COUNT=1
VOLUME_SIZE=1000
BLOCK_DEVICE_MAPPINGS="[{ \"DeviceName\": \"/dev/sda1\", \"Ebs\": { \"DeleteOnTermination\": true, \"VolumeSize\": $VOLUME_SIZE, \"VolumeType\": \"gp2\", \"Encrypted\": false} }]"
SEC_GROUP="YOURS_HERE"

IAM_ROLE="Name=YOURS_HERE"

ACCOUNT_ID="YOURS_HERE"
