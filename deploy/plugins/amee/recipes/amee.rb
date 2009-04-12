namespace :amee do
  [:stop, :start, :restart, :status].each do |action|
    desc "#{action.to_s.capitalize} AMEE"
    task action, :roles => :app do
      sudo "/etc/init.d/amee #{action.to_s}", :via => run_method
    end
  end
  
  desc "Tail AMEE logfile"
  task :tail_log, roles => :app do
    stream "tail -f #{current_release}/log/wrapper.log"
  end
   
  desc "Open log and pid directory persmissions for amee user"
  task :chmod, roles => :app do
    sudo "chmod a+w #{shared_path}/log #{shared_path}/pids"
  end

  desc "Create symlink to amee start script"
  task :init_d, roles => :app do
    sudo "lns -s #{current_path}/bin/amee /etc/init.d/amee && chmod 777 /etc/init.d/amee"
  end

  after "deploy:setup", "[amee:chmod, amee:init_d]"   
end