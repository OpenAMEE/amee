# The hosts where are are deploying
role :app, "platform-qa.amee.com"
role :db,  "platform-qa.amee.com", :primary => true

set :application, "amee-qa"
set :deploy_to, "/var/www/apps/#{application}"