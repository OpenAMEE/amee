# The hosts where are are deploying
role :app, "app3.amee.com"
role :db,  "app3.amee.com", :primary => true

set :application, "amee-qa"
set :deploy_to, "/var/www/apps/#{application}"