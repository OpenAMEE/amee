# The hosts where are are deploying 
role :app, "localhost"
role :db,  "localhost", :primary => true

set :user, "david.keen"