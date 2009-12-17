# sudo gem install amazon-ec2
# http://www.denofubiquity.com/ruby/bootstrapping-a-deploy-user-with-capistrano-on-ec2/
# http://developer.amazonwebservices.com/connect/thread.jspa?threadID=21867&start=0&tstart=0
# http://nutrun.com/weblog/rubyworks-production-stack-on-amazon-ec2/

default_run_options[:pty] = true

set :application, "amee"
set :repository,  "git@github.com:catditch/amee.deploy.git"
set :deploy_to, "/var/www/apps/#{application}"

# Sudo command on remote machine
set :sudo, "/usr/bin/sudo"
set :use_sudo, false
   
#set :timestamp, Time.now.utc.strftime("%b%d%Y")

# Local directory containing the ec2 keypairs and certs.
set :ec2_dir, ENV['HOME'] + '/.ec2'

# Remote directory into which are copied utility scripts.
set :ec2_utils_dir, "#{deploy_to}/shared/ec2_utils"

# The amazon image id from which to create a running instance.
set :ami_id, ENV['AMI_ID'] || 'ami-dc1038a8'

# The amazon availability zone in which to create a running instance.
set :availability_zone, "eu-west-1a"

# SSH user and keypair. 
set :user, "root"
set :keypair, "steve.pem"
set :keypair_full_path, "~/.ec2/#{keypair}"
ssh_options[:username] = user
ssh_options[:keys] = keypair_full_path

# AWS certificates.
set :pk, "pk-aws.pem"
set :cert, "cert-aws.pem"

# AWS account and access credentials.
set :account_id, "7801-4565-4812"
set :access_key_id, ENV['AMAZON_ACCESS_KEY_ID']
set :secret_access_key, ENV['AMAZON_SECRET_ACCESS_KEY']

# Bootstrap packages and gems
set :packages, %w(apache2 openjdk-6-jre mysql-server less sudo git-core rubygems monit)
set :gems, %w(aws-s3)

# Running instance id and url
set :instance_id,  ENV['INSTANCE_ID'] || "i-3e23184a"
set :instance_url, ENV['INSTANCE_URL'] || "ec2-79-125-50-185.eu-west-1.compute.amazonaws.com"

role :app, instance_url
role :web, instance_url
role :db,  instance_url, :primary => true
