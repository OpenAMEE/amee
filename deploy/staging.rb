# The hosts where are are deploying 
role :app, "app5.amee.com"
role :db,  "app5.amee.com", :primary => true
