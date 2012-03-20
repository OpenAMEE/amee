# The hosts where are are deploying
role :app, "ec2-23-20-138-137.compute-1.amazonaws.com", "ec2-107-21-135-182.compute-1.amazonaws.com"

set :application, "amee-qa"
set :deploy_to, "/var/www/apps/#{application}"