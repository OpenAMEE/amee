#!/bin/bash

source ec2_credentials.sh

# Ubuntu 9.0.4 (Alestic)
#ec2-run-instances --user-data-file ghg-boot.udf.o --key amee-gsg-keypair ami-ed46a784
ec2-run-instances --user-data-file ghg-boot.udf --key amee-gsg-keypair ami-ed46a784

ec2-authorize default -p 22

# Connect with for Canonical AMIs
# ssh -i ~/dev/aws/id_rsa-gsg-keypair ubuntu@$HOST

# Connect with for alestic AMIs
# ssh -i ~/dev/aws/id_rsa-gsg-keypair root@$HOST

#
# Next steps
#

# Check Ubuntu version - do we need Jaunty or Karmic? Any reason to use canoncial vs alestic kernels.

# Confirm buckets are private
# Store the ghg script in an S3 bucket and confirm private to amee

# Store private key and cert in amee s3 bucket and confirm private to amee
# Extend the script to download and install the ec2 tools and the private key and cert

# Setup elastic IP and point DNS

# ===== #

#
# Note that the scripts were written from the perspective of a ubuntu user - hence
# the proliferation of sudo invocations. If running as root, these can, obviously,
# all be deleted.
#
# Confirm expected build time
#  - the ami-ed46a784 instance took 13 mins to build
#     Sep 15 10:00:48 domU-12-31-39-00-59-02 S71ec2-run-user-data: Running user-data
#     Sep 15 10:00:48 domU-12-31-39-00-59-02 S71ec2-run-user-data: Running user-data
#
#  - the ami-5d59be34 instance took ??? mins to build
#     I think it took less than half the time
#
# Related to AMI or simply instance specific?
#