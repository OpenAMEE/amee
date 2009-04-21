set :instance_url, ENV['INSTANCE_URL'] || "ec2-79-125-50-185.eu-west-1.compute.amazonaws.com"

role :app, instance_url
role :web, instance_url
role :db,  instance_url, :primary => true
