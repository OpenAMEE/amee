# The hosts where are are deploying 
role :app, "ec2-23-22-0-248.compute-1.amazonaws.com", "ec2-54-242-0-90.compute-1.amazonaws.com"

set :application, "v2-staging.amee.com"
set :deploy_to, "/var/www/apps/#{application}"
