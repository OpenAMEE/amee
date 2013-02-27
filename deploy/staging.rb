# The hosts where are are deploying 
role :app, "ec2-54-242-111-35.compute-1.amazonaws.com", "ec2-184-73-122-24.compute-1.amazonaws.com"

set :application, "v2-staging.amee.com"
set :deploy_to, "/var/www/apps/#{application}"
