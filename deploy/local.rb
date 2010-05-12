# The hosts where are are deploying 
role :app, "localhost"
role :db,  "localhost", :primary => true

set :application, "amee"
set :deploy_to, "/var/www/apps/#{application}"

unset :user
#set :user, "local.user"

# Deploy from local folder
set :repository,  "/Development/AMEE/amee.deploy"

#set :scm, :git
#set :scm_command, "/opt/local/bin/git"

set :scm, :none
set :deploy_via, :copy
set :copy_exclude, [".git"]

