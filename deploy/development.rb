# The Git branch we are deploying from
#set :branch, "development"

# The hosts where are are deploying 
role :app, "dev.amee.com"
role :db,  "dev.amee.com", :primary => true
