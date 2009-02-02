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
  after "deploy:setup", "amee:chmod"   
end