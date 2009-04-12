namespace :configuration do
  desc "Copy initial templates for server configuration"
  task :initialize_from_templates do
    if File.exist?("config/server")
      puts "[skip] files that exist in config/server/ are not overwritten"
    else
      puts "[add] adding all configuration files to config/server/"
      system "mkdir config/server"
    end
    system "cp -R -n #{File.dirname(__FILE__)}/../../config/server/ config/server/"
  end
  
  desc "svn export or upload recursively all files from config/server"
  task :upload do
    if File.exist?('config/server/.svn')
      # export to temp_dir to avoid uploading .svn directories
      temp_dir = "/tmp/rw-ec2-#{Time.now.strftime("%Y%m%d%H%M%S")}"
      system "svn export config/server #{temp_dir}"
      system "scp -r -p -i #{keypair_full_path} #{temp_dir}/* #{user}@#{instance_url}:/"
      system "rm -rf #{temp_dir}"
    else
      system "scp -r -p -i #{keypair_full_path} config/server/* #{user}@#{instance_url}:/"
    end
    run "chmod 600 /etc/{monit/monitrc,rails/monit.conf}"
    monit::restart
  end
  
  desc "Download some configuration files from the server and overwrite local versions"
  task :download do
    %w(mysql monit rails).each do |dir|
      system "mkdir config/server/etc/#{dir}"
    end
    %w(mysql/my.cnf monit/monitrc rails/monit.conf rails/haproxy.conf rails/mongrel_3002.config).each do |file|
      system "scp -i #{keypair_full_path} #{user}@#{instance_url}:/etc/#{file} config/server/etc/#{file}"
    end
  end
end
