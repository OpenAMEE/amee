# The Git branch we are deploying from
set :branch, "staging"

# The hosts where are are deploying 
role :app, "stage.co2.dgen.net"
role :db,  "stage.co2.dgen.net", :primary => true
