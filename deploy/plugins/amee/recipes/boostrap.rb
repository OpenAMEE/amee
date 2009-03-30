=begin

require 'rubygems'
require 'EC2'
require 'pp'

# TODO:
# Move EC2::Base.new....
# Move logging to method
# Reading instances to shutdown
# Improve logging

namespace :myec2 do

  namespace :instances do
  
    desc "Show all ec2 instances."
    task :list do 
      @ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )

      pp @ec2.describe_instances
    end
    
    desc "Desribe security groups."
    task :describe_security_groups do 
      @ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )

      pp @ec2.describe_security_groups
    end

    desc "Launch a new ec2 instance."
    task :run do
        @ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
        
        pp @ec2.run_instances(:image_id => 'ami-dc1038a8', :min_count => '1', :max_count => '1', :instance_type => 'm1.large', :availability_zone => 'eu-west-1a')
    end
    
    desc "Terminate an ec2 instance"
    task :terminate do
      @ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )

      pp @ec2.terminate_instances(:instance_id => 'i-860930f2')
    end

  end
  
  namespace :bootstrap do
    
    desc "Bootstrap an AMEE appserver instance."
    task :default do

      # TODO - Test the image is running...
      
      
      #git
      #java
      #jruby
      #deploy user
    
    end

  end

end

=end