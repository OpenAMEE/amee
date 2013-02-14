# The hosts where are are deploying 
role :app, "ec2-23-23-35-49.compute-1.amazonaws.com", "ec2-107-21-149-121.compute-1.amazonaws.com"

set :application, "amee-live.amee.com"
set :deploy_to, "/var/www/apps/#{application}"