# The Git branch we are deploying from
set :branch, "steve"

# The hosts where are are deploying 
role :app, "localhost"
role :db,  "localhost", :primary => true
