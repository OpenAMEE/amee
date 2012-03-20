# The hosts where are are deploying 
role :app, "app3.amee.com"

set :application, "amee-dev"
set :deploy_to, "/var/www/apps/#{application}"