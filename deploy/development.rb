# The Git branch we are deploying from
#set :branch, "development"

# The hosts where are are deploying 
role :app, "flood.amee.com"
role :db,  "flood.amee.com", :primary => true
