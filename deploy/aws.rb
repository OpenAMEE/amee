# sudo gem install amazon-ec2

# Describe security groups
# If no ssh group then create
# Create instance
# Add group
# Encapsulate pending/running logic
# Improve logging
# ssh - need to remove old instance from known_hosts - need to generate a random host key on first boot
# Do EBS mounting - currently using ephemeral storage

# cap aws bootstrap DOMAIN=ec2-79-125-53-224.eu-west-1.compute.amazonaws.com - bootstrap should be part of the instance startup task

# http://www.denofubiquity.com/ruby/bootstrapping-a-deploy-user-with-capistrano-on-ec2/
# http://developer.amazonwebservices.com/connect/thread.jspa?threadID=21867&start=0&tstart=0
# http://nutrun.com/weblog/rubyworks-production-stack-on-amazon-ec2/

# Do i need all these?
set :user, "root"

set :keypair, "steve"

set :account_id, "7801-4565-4812"
set :access_key_id, ENV['AMAZON_ACCESS_KEY_ID']
set :secret_access_key, ENV['AMAZON_SECRET_ACCESS_KEY']

set :ami_id, ENV['AMI'] || 'ami-dc1038a8'
set :instance_id, ENV['INSTANCE']
set :host_name, ENV['HOST']
set :domain, ENV['DOMAIN']
set :ssh_user, "root"
set :ssh_key, "/Users/stevematthew/.ssh/steve.pem"
set :availability_zone, "eu-west-1a"

# rubyworks-ec2
#set :pk, "pk-aws.pem"
#set :cert, "cert-aws.pem"
set :packages, %w(apache2 subversion mysql-server less) # plus any additional packages you'd like to install on the image
set :gems, %w(aws-s3 ezcrypto) #plus any additional gems you'd like to install on the instance

set :instance_id, "i-d6744ca2"
set :instance_url, "ec2-79-125-53-68.eu-west-1.compute.amazonaws.com"

role :app, instance_url
role :web, instance_url
role :db,  instance_url, :primary => true

