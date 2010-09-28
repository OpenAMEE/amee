# The hosts where are are deploying
role :app, "ec2-79-125-72-205.eu-west-1.compute.amazonaws.com"
role :db,  "ec2-79-125-72-205.eu-west-1.compute.amazonaws.com", :primary => true

set :application, "amee-qa"
set :deploy_to, "/var/www/apps/#{application}"