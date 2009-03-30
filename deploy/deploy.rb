set :stages, %w(development staging production)
set :default_stage, "development"
set :stage_dir, "deploy"

require 'capazon'
require 'capistrano/ext/multistage'
require 'capistrano/ext/monitor'

default_run_options[:pty] = true

# This will be the name of the top-level directory under /var/www/apps
set :application, "amee"

# Where Capistrano should get the deployable artifacts
set :repository,  "git@github.com:sambogoat/amee.deploy.git"

# If you aren't deploying to /u/apps/#{application} on the target
# servers (which is the default), you can specify the actual location
# via the :deploy_to variable:
set :deploy_to, "/var/www/apps/#{application}"

# If you aren't using Subversion to manage your source code, specify
# your SCM below:
# set :scm, :subversion
set :scm, :git
set :scm_command, "/usr/bin/git"

# Sudo command on remote machine
set :sudo, "/usr/bin/sudo"
set :use_sudo, false
   
# The deployment user. This should exist in the scm and on each of the deployed-to hosts
set :user, "deploy"

# Source code and build locations
set :src_dir, "/Development/AMEE/amee.2.1"
set :package_dir, "/Development/AMEE/amee.deploy"

#AWS login info
set :aws_access_key_id, ENV['AMAZON_ACCESS_KEY_ID'] 
set :aws_secret_access_key, ENV['AMAZON_SECRET_ACCESS_KEY'] 

# Name of the keypair used to spawn and connect to the Amazon EC2 Instance
# Defaults to one created by the setup_keypair task
#set :aws_keypair_name, "#{application}-capazon"

# Path to the private key for the Amazon EC2 Instance mentioned above
# Detaults to one created by setup_keypair task
#set :aws_private_key_path, "#{Dir.pwd}/#{aws_keypair_name}-key"

#defaults to an ubuntu image
#set :aws_ami_id, "ami-e4b6538d"

#defaults to, um, default
#set :aws_security_group, "default"

# Override Capistrano tasks
deploy.task :start, :roles => :app do
  amee.start
end

deploy.task  :stop, :roles => :app do
  amee.stop
end

deploy.task :restart, :roles => :app do
  amee.restart
end

deploy.task :migrate, :roles => :db, :only => { :primary => true } do
  #mysql.migrate
end

