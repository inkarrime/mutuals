#!/bin/bash
# EC2 user data (Amazon Linux 2023): installs Docker and Git and adds swap so Maven can build on a t2/t3.micro.
set -euxo pipefail

dnf update -y
dnf install -y docker git
systemctl enable --now docker
usermod -aG docker ec2-user

if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
