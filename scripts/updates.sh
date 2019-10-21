#!/bin/bash

echo ""
echo "Updating aws cli"
pip3 install awscli --upgrade --user || true

echo ""
echo "Updating aws-cdk"
npm i -g aws-cdk  || true

echo ""
echo "Updating rustup"
rustup self update || true

echo ""
echo "Updating apt"
sudo apt update
apt list --upgradable


