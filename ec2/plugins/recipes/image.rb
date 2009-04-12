namespace :image do
  desc "Back up and register an image of the running instance to S3"
  task :backup do
    cp_keypair
    clean_up
    bundle
    upload
    register
  end
  
  task :clean_up do
    run <<-CMD
      echo '' > $HOME/.bash_history &&
      echo '' > $HOME/.mysql_history &&
      echo '' > $HOME/.ssh/authorized_keys &&
      rm $HOME/.amazon_keys
    CMD
  end

  task :cp_keypair do
    system "scp -i #{keypair_full_path} #{ec2_dir}/#{pk} #{ec2_dir}/#{cert} #{user}@#{instance_url}:/mnt"
  end

  task :bundle do
    run <<-CMD
      export RUBYLIB=/usr/lib/site_ruby/ && 
      ec2-bundle-vol -d /mnt -k /mnt/#{pk} -c /mnt/#{cert} -u #{account_id} -s 1536 -p #{timestamp}
    CMD
  end

  task :upload do
    run <<-CMD
      export RUBYLIB=/usr/lib/site_ruby/ && 
      ec2-upload-bundle -b #{bucket} -m /mnt/#{timestamp}.manifest.xml -a #{access_key_id} -s #{secret_access_key}
    CMD
  end

  task :register do
    exec "ec2-register #{bucket}/#{timestamp}.manifest.xml"
  end
end
