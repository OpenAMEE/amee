set :stages, %w(development science staging production qa local)
set :default_stage, "development"
set :stage_dir, "deploy"

default_run_options[:pty] = true

# This will be the name of the top-level directory under /var/www/apps
set :application, "amee"

# Where Capistrano should get the deployable artifacts
set :repository,  "git@github.com:AMEE/amee.deploy.git"

# If you aren't deploying to /u/apps/#{application} on the target
# servers (which is the default), you can specify the actual location
# via the :deploy_to variable:
set :deploy_to, "/var/www/apps/#{application}"

# If you aren't using Subversion to manage your source code, specify
# your SCM below:
# set :scm, :subversion
set :scm, :git
#set :scm_command, "/usr/bin/git"

# Sudo command on remote machine
set :sudo, "/usr/bin/sudo"
set :use_sudo, false
   
# The deployment user. This should exist in the scm and on each of the deployed-to hosts
set :user, "amee"

# Use ssh agent forwarding for access to GitHub
ssh_options[:forward_agent] = true

# Source code and build locations
raise "PLATFORM_SRC_DIR - #{ENV['PLATFORM_SRC_DIR']} does not exist" unless File.exists?(ENV['PLATFORM_SRC_DIR'])
raise "PLATFORM_PKG_DIR - #{ENV['PLATFORM_PKG_DIR']} does not exist" unless File.exists?(ENV['PLATFORM_PKG_DIR'])
set :src_dir, ENV['PLATFORM_SRC_DIR']
set :package_dir, ENV['PLATFORM_PKG_DIR']

# Pick tag to deploy.
set :branch do
  default_tag = `git tag`.split("\n").last
  tag = Capistrano::CLI.ui.ask "Tag to deploy (make sure to push the tag first): [#{default_tag}] "
  tag = default_tag if tag.empty?
  tag
end

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

deploy.task :finalize_update do
  # Override the rails stuff
end

# Add keys to ssh-agent for agent forwarding
case RbConfig::CONFIG['host_os']
  when /darwin/
    before "deploy:update" do
      `ssh-add`
    end
end
