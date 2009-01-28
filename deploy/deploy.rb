default_run_options[:pty] = true

# This will be the name of the top-level directory under /var/www/apps
set :application, "amee"

# Where Capistrano should get the deployable artifacts
set :repository,  "git@github.com:sambogoat/amee-release-builds.git"

# If you aren't deploying to /u/apps/#{application} on the target
# servers (which is the default), you can specify the actual location
# via the :deploy_to variable:
set :deploy_to, "/var/www/apps/#{application}"

# If you aren't using Subversion to manage your source code, specify
# your SCM below:
# set :scm, :subversion
set :scm, :git
set :scm_command, "/opt/local/bin/git"

# Multi-host config......
set :branch, "stage"

# The deployment user. This should exist in the scm and on each of the deployed-to hosts
set :user, "deploy"
set :scm_passphrase, "deploy"

# The hosts where are are deploying 
role :app, "localhost"
role :db,  "localhost", :primary => true

# Override Capistrano tasks
deploy.task :start, :roles => :app do
  amee.start
end

deploy.task  :stop, :roles => :app do
end

deploy.task :restart, :roles => :app do
  amee.restart
end

deploy.task :migrate, :roles => :db, :only => { :primary => true } do
  mysql.migrate
end
