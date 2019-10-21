#!/bin/bash

source common.sh

NAME_TAG=$1
DISTRO="${2:-recon}"

# requires gpw package
#RANDOM_SUFFIX=$(gpw 1 7)
usage() {
  echo "Usage: ./ec2.sh [NAME_TAG] [DISTRO]; DISTRO default is 'recon'.
                                             DISTRO valid values: 'recon' or 'ubun' or 'amz2'"
  exit 1
}

if [[ -z $NAME_TAG ]]; then
  usage
fi

if [[ $DISTRO = "recon" ]]; then
  AMI="ami-0dc1a6f12a77e53da" # recon-v7 (derived from Ubuntu 18.04)
  SSH_USER="ubuntu"
elif [[ $DISTRO = "ubun" ]]; then
  AMI="ami-09a3d8a7177216dcf" # Ubuntu 18.04 (bionic beaver), ebs-ssd
  SSH_USER="ubuntu"
elif [[ $DISTRO = "amz2" ]]; then
  AMI="ami-04b762b4289fba92b" # Amazon Linux 2
  SSH_USER="ec2-user" # for Amazon Linux 2
else
  usage
  exit 1
fi

echo $SSH_USER

# Ubuntu AMI Search: https://cloud-images.ubuntu.com/locator/ec2/

printf "Launching $DISTRO $AMI in \e[1m\e[32m%s\e[0m with $INSTANCE_TYPE instance type\n" $REGION

INSTANCE_ID=$(aws ec2 run-instances --image-id $AMI --count $COUNT --instance-type $INSTANCE_TYPE --key-name $AWS_KEY_NAME \
 --security-group-ids $SEC_GROUP --subnet-id $SUBNET --region $REGION --profile $PROFILE --associate-public-ip-address \
 --iam-instance-profile $IAM_ROLE \
 --block-device-mappings "$BLOCK_DEVICE_MAPPINGS" \
 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$NAME_TAG}]" | jq -r .Instances[0].InstanceId)

if [[ -z $INSTANCE_ID ]]; then
  printf "\e[1m\e[31m%s\e[0m\n\n" "Error with run-instances; No INSTANCE_ID returned"
  exit 1
fi

printf "Launched \e[1m\e[32m%s\e[0m\n" "$INSTANCE_ID"
echo -ne "Waiting until instance achieves running state..."

state="pending"
until [ $state = "running" ]
do
  state="$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --profile $PROFILE | jq -r .Reservations[0].Instances[0].State.Name)"
  printf "."
  sleep 1
done

PUBLIC_DNS="$(aws ec2 describe-instances --instance-id "$INSTANCE_ID" --profile $PROFILE \
| jq -r .Reservations[0].Instances[0].NetworkInterfaces[0].Association.PublicDnsName)"
echo
printf "Public DNS: \e[1m\e[32m%s\e[0m\n" "$PUBLIC_DNS"

echo -ne "Waiting until SSH is available..."
ssh_avail=1
until [ $ssh_avail = 0 ]; do
  nc -zv "$PUBLIC_DNS" 22 &> /dev/null
  ssh_avail=$?
  sleep 1
  printf "."
done
echo
echo "Connecting..."
echo

ssh -i "$LOCAL_KEY_PATH" -o "StrictHostKeyChecking no" -o "LogLevel ERROR"  "$SSH_USER@$PUBLIC_DNS"
