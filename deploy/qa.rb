# The hosts where are are deploying
role :app, "ec2-184-72-166-99.compute-1.amazonaws.com", "ec2-50-19-29-105.compute-1.amazonaws.com"

set :application, "amee-qa"
set :deploy_to, "/var/www/apps/#{application}"