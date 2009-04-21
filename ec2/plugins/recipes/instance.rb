namespace :instance do
  
  desc "Start and configure a basic AMEE EC2 instance."
  task :start do
    new_instance
    describe_instances
    boostrap
  end
  
  task :stop do
    system "ec2-terminate-instances #{instance_id}"
  end
  
  desc "Show all ec2 instances."
  task :describe do 
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
        :group_id => ["default", "ssh", "web"],          
        :key_name => "steve"
      }

      puts "Creating a new EC2 instance..."
      ec2 = EC2::Base.new(:access_key_id => ENV['AMAZON_ACCESS_KEY_ID'], :secret_access_key => ENV['AMAZON_SECRET_ACCESS_KEY']  )
      response = ec2.run_instances(options)
      pp response
      instance_id = response.instancesSet.item[0].instanceId
      instance_state = response.instancesSet.item[0].instanceState.name
      instance_url = response.instancesSet.item[0].dnsName

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

      set :instance_url, instance_url
      set :instance_id, instance_id

      # TODO
      #server instance_url, :web, :app, :db
      
      puts "Done"
  end
  
  desc "Bootstrap an AMEE appserver instance."
  task :bootstrap do
    #install_packages
    #install_jruby
    #install_gems
    #cp_amazon_keys 
    #cp_db_utils
    #upload_config
    #setup_hosts 
    #create_database
    #create_amee_user 
    create_deploy_user
  end

  task :install_packages do
    run "apt-get update"
    packages.each do |package|
      run "export DEBIAN_FRONTEND=noninteractive && apt-get -y -q install #{package}"
    end
  end

  task :install_jruby do
    run <<-END
      wget http://dist.codehaus.org/jruby/jruby-bin-1.1.3.tar.gz &&
      tar zxvf jruby-bin-1.1.3.tar.gz &&
      rm jruby-bin-1.1.3.tar.gz && 
      mv jruby-1.1.3 /usr/lib &&
      ln -s /usr/lib/jruby-1.1.3/bin/jruby /usr/bin/jruby &&
      ln -s /usr/lib/jruby-1.1.3/bin/jirb /usr/bin/jirb
    END
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
    run "mkdir -p #{ec2_utils_dir}"
    system "scp -i #{keypair_full_path} #{dbbackup} #{user}@#{instance_url}:/#{ec2_utils_dir}/database_backup.rb"
    system "scp -i #{keypair_full_path} #{backup_script} #{user}@#{instance_url}:/#{ec2_utils_dir}/backup_db"
    system "scp -i #{keypair_full_path} #{restore_script} #{user}@#{instance_url}:/#{ec2_utils_dir}/restore_db"
    run <<-CMD
      chmod 755 #{ec2_utils_dir}/backup_db &&
      chmod 755 #{ec2_utils_dir}/restore_db
    CMD
  end

  task :setup_hosts do
    run "echo '127.0.0.1  localhost' > /etc/hosts"
  end

  task :create_database do
    run "echo 'create database if not exists #{application};' | mysql"
  end

  desc "svn export or upload recursively all files from config/server"
  task :upload_config do
    if File.exist?('config/server/.svn')
      # export to temp_dir to avoid uploading .svn directories
      temp_dir = "/tmp/rw-ec2-#{Time.now.strftime("%Y%m%d%H%M%S")}"
      system "svn export config/server #{temp_dir}"
      system "scp -r -p -i #{keypair_full_path} #{temp_dir}/* #{user}@#{instance_url}:/"
      system "rm -rf #{temp_dir}"
    else
      system "scp -r -p -i #{keypair_full_path} config/server/* #{user}@#{instance_url}:/"
    end
    run "chmod 600 /etc/monit/monitrc"
    monit::restart
  end

  desc "Create amee process user"
  task :create_amee_user do
    run "groupadd amee && useradd amee -g amee"
  end

  # TODO - A better way to do the sudoers change?
  desc "Create deploy process user"
  task :create_deploy_user do
    run <<-END
      groupadd deploy && 
      useradd deploy -g deploy -d /home/deploy &&
      mkdir -p /home/deploy/.ssh &&
      chown -R deploy:deploy /home/deploy && 
      mkdir -p /var/www/apps &&
      chown -R deploy:deploy /var/www/apps && 
      echo "deploy ALL = /etc/init.d/amee" >> /etc/sudoers
    END
    id_rsa = File.open(File.dirname(__FILE__) + '/../../config/keys/id_rsa', 'r').path
    id_rsa_pub = File.open(File.dirname(__FILE__) + '/../../config/keys/id_rsa.pub', 'r').path
    authorized_keys = File.open(File.dirname(__FILE__) + '/../../config/keys/authorized_keys', 'r').path
    system "scp -i #{keypair_full_path} #{id_rsa} #{user}@#{instance_url}:/home/deploy/.ssh/"
    system "scp -i #{keypair_full_path} #{id_rsa_pub} #{user}@#{instance_url}:/home/deploy/.ssh/"
    system "scp -i #{keypair_full_path} #{authorized_keys} #{user}@#{instance_url}:/home/deploy/.ssh/"
  end
      
  desc "Remotely login to the running instance"
  task :ssh do
    system "ssh -i #{keypair_full_path} #{user}@#{instance_url}"
  end
  
  desc "Copy public key from instance to $HOME/.ec2"
  task :cp_public_key do
    system "scp -i #{keypair_full_path} #{user}@#{instance_url}:/mnt/openssh_id.pub #{ec2_dir}/id_rsa-#{keypair}.pub"
  end
  
  task :install_gems do
    gems.each do |gem|
      run "gem i #{gem} -y --source http://gems.rubyforge.org/ --no-ri --no-rdoc"
    end
  end

end

