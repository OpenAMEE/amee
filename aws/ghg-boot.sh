#!/bin/bash

# Prepare instance 
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y build-essential 

# Install Ruby
sudo apt-get install -y ruby1.8-dev ruby1.8 ri1.8 rdoc1.8 irb1.8 libreadline-ruby1.8 libruby1.8 libopenssl-ruby git-core
sudo ln -s /usr/bin/ruby1.8 /usr/bin/ruby
sudo ln -s /usr/bin/ri1.8 /usr/bin/ri
sudo ln -s /usr/bin/rdoc1.8 /usr/bin/rdoc
sudo ln -s /usr/bin/irb1.8 /usr/bin/irb

# Install RubyGems
wget http://rubyforge.org/frs/download.php/57643/rubygems-1.3.4.tgz
tar xzvf rubygems-1.3.4.tgz 
cd rubygems-1.3.4/
sudo ruby setup.rb
sudo ln -s /usr/bin/gem1.8 /usr/bin/gem
sudo gem update --system
cd ..
rm -rf rubygems-1.3.4

# Install Merb and other dependencies
sudo gem install merb-core merb-action-args merb-assets merb-helpers merb-mailer merb-param-protection merb-exceptions fastercsv ferret mongrel

# Make deployment dir
sudo mkdir -p /var/www/apps

# Copy private key to ami from S3
#scp -i  ~/.amee-ec2/id_rsa-gsg-keypair ~/.amee-ec2/id_rsa-gsg-keypair  #root@ec2-67-202-28-200.compute-1.amazonaws.com:/root/.ssh/id_rsa id_rsa-gsg-keypair

# Do git checkout of code (master and akamai branches)

# Start master (merb -p 80 -d) akamai (merb -p 81 -d) merb instances  
