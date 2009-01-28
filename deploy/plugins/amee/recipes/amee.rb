namespace :amee do
  [:stop, :start, :restart, :reload].each do |action|
    desc "#{action.to_s.capitalize} AMEE"
    task action, :roles => :app do
      sudo "/etc/init.d/amee #{action.to_s}", :via => run_method
    end
  end
  
  desc "Tail AMEE logfile"
  task :tail_log, roles => :app do
    stream "tail -f #{current_release}/log/wrapper.log"
  end
    
end