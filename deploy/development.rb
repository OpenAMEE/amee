# The hosts where are are deploying 
role :app, "flood.amee.com"
role :db,  "flood.amee.com", :primary => true
