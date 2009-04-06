namespace :ec2 do

    desc "Start and configure a basic AMEE EC2 instance."
    task :new do
      new_instance
      describe_instances
    end
    
    desc "Show all ec2 instances."
    task :describe_instances do 

      ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
      response = ec2.describe_instances
      response.reservationSet.item.each do |reservation|
        reservation_id = reservation.reservationId
        reservation.instancesSet.item.each do |instance|
          instance_id = instance.instanceId
          instance_state = instance.instanceState.name
          dns_name = instance.dnsName

          puts "[#{reservation_id}] [instance_id=#{instance_id}]"
          puts "[#{reservation_id}] [instance_state=#{instance_state}]"
          puts "[#{reservation_id}] [dns_name=#{dns_name}]"
          puts "\n"

        end
      end
    end

    desc "Describe security groups."
    task :describe_security_groups do 
      ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
      response = ec2.describe_security_groups
      pp response
    end

    desc "Create the SSH security group."
    task :create_ssh_security_group do

      ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )

      puts "Creating ssh security group...."
      pp ec2.create_security_group(:group_name => "ssh", :group_description => "ssh group")

      puts "Authorizing ssh security group...."
      pp ec2.authorize_security_group_ingress(:group_name => "ssh", :ip_protocol => "tcp", :from_port => "22", :to_port => "22", :cidr_ip => "0.0.0.0/0")
    end
    
    desc "Delete the SSH security group."
    task :delete_ssh_security_group do
      puts "Deleting ssh security group...."
      pp ec2.delete_security_group(:group_name => "ssh")
    end
    
    desc "Launch a new ec2 instance."
    task :new_instance do

        options = {
          :image_id => ami_id, 
          :min_count => '1', 
          :max_count => '1', 
          :instance_type => 'm1.large', 
          :availability_zone => availability_zone, 
          :group_id => ["default", "ssh"],          
          :key_name => "steve"
        }
  
        puts "Creating a new EC2 instance..."
        ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
        response = ec2.run_instances(options)
        pp response
        instance_id = response.instancesSet.item[0].instanceId
        instance_state = response.instancesSet.item[0].instanceState.name
        dns_name = response.instancesSet.item[0].dnsName
        puts "instance_id => #{instance_id}"
        puts "instance_state => #{instance_state}"

        # TODO - Move to seperate task
        puts "Creating a new volume...."
        until instance_state == 'running' do
          response = ec2.describe_instances(:instance_id => instance_id)
          instance_state = response.reservationSet.item[0].instancesSet.item[0].instanceState.name
          sleep 5
          puts "Waiting for new instance to enter 'running' state..."
        end

        response = ec2.create_volume(:availability_zone => availability_zone, :size => "10")
        volume_id = response.volumeId

        puts "Attaching volume #{volume_id} to instance #{instance_id}"
        ec2.attach_volume(:volume_id => volume_id, :instance_id => instance_id, :device => "/dev/sdh")

        puts "dns_name => #{dns_name}"
        set :domain, dns_name
          
        puts "Done"
    end
    
    desc "Terminate an ec2 instance"
    task :terminate_instance do

      ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
      response = ec2.terminate_instances(:instance_id => instance_id)
      pp response
      
    end

end

namespace :bootstrap do
    
    desc "Bootstrap an AMEE appserver instance."
    task :default do
      install_packages
    end
    
    task :install_packages do
      packages.each do |package|
        run "apt-get install #{package} -y"
      end
    end

    task :install_gems do
      gems.each do |gem|
        run "gem i #{gem} -y --source http://gems.rubyforge.org/ --no-ri --no-rdoc"
      end
    end

    task :cp_amazon_keys do
      run <<-CMD
        echo 'export ACCESS_KEY_ID=\"#{access_key_id}\"' > $HOME/.amazon_keys &&
        echo 'export SECRET_ACCESS_KEY=\"#{secret_access_key}\"' >> $HOME/.amazon_keys &&
        echo 'export ACCOUNT_ID=\"#{account_id}\"' >> $HOME/.amazon_keys
      CMD
    end

    task :cp_db_utils do
      dbbackup = File.open(File.dirname(__FILE__) + '/../util/database_backup.rb', 'r').path
      backup_script = File.open(File.dirname(__FILE__) + '/../util/backup_db.rb', 'r').path
      restore_script = File.open(File.dirname(__FILE__) + '/../util/restore_db.rb', 'r').path
      run "mkdir #{rwdir}"
      system "scp -i #{keypair_full_path} #{dbbackup} #{username}@#{instance_url}:/#{rwdir}/database_backup.rb"
      system "scp -i #{keypair_full_path} #{backup_script} #{username}@#{instance_url}:/#{rwdir}/backup_db"
      system "scp -i #{keypair_full_path} #{restore_script} #{username}@#{instance_url}:/#{rwdir}/restore_db"
      run <<-CMD
        chmod 755 #{rwdir}/backup_db &&
        chmod 755 #{rwdir}/restore_db
      CMD
    end

    task :setup_vhost do
      run <<-CMD 
        a2enmod proxy_http && 
        a2enmod proxy && 
        a2enmod rewrite && 
        /etc/init.d/apache2 reload
      CMD
    end

    task :setup_hosts do
      run "echo '127.0.0.1  localhost' > /etc/hosts"
    end

    task :create_database do
      run "echo 'create database if not exists #{application}_production;' | mysql"
    end

    desc "Remotely login to the running instance"
    task :ssh do
      system "ssh -i #{keypair_full_path} #{username}@#{instance_url}"
    end
    
end

