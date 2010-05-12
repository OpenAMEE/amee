# The hosts where are are deploying 
role :app, "localhost"
role :db,  "localhost", :primary => true

set :user, "david.keen"
set :scm_command, "/opt/local/bin/git"

set :application, "amee-test"
set :deploy_to, "/var/www/apps/#{application}"

# Don't deploy from github
set :repository,  "/Development/AMEE/amee.deploy"
#set :deploy_via, :copy
#set :copy_cache, true
#set :copy_exclude, [".git"]
