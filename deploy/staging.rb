# The hosts where are are deploying 
role :app, "ec2-204-236-242-253.compute-1.amazonaws.com"

set :application, "amee-staging.amee.com"
set :deploy_to, "/var/www/apps/#{application}"
