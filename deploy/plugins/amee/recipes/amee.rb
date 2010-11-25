namespace :amee do
  [:stop, :start, :restart, :status].each do |action|
    desc "#{action.to_s.capitalize} AMEE"
    task action, :roles => :app do
      sudo "/etc/init.d/amee #{action.to_s}", :via => run_method
    end
  end
  
  desc "Ls current install"
  task :check_current, roles => :app do
    stream "ls -ld #{current_release}/.."
  end
  
  desc "Tail AMEE logfile"
  task :tail_log, roles => :app do
    stream "tail -f #{current_release}/log/wrapper.log"
  end
   
  desc "Create symlink to amee start script"
  task :init_d, roles => :app do
    sudo "ln -s #{current_path}/bin/amee /etc/init.d/amee && chmod 777 /etc/init.d/amee"
  end

  desc "Grep for 40* and 500 status in logs"
  task :grep_status, roles => :app do
    stream "egrep 'S:500|S:40' #{current_release}/log/wrapper.log"
  end
  
  task :munin_symlink do
    sudo "ln -s #{current_path}/munin/jmx_ /etc/munin/plugins/jmx_amee_data_source && chmod +x /etc/munin/plugins/jmx_amee_data_source"
    sudo "ln -s #{current_path}/munin/jmx_ /etc/munin/plugins/jmx_amee_profiles && chmod +x /etc/munin/plugins/jmx_amee_profiles"
    sudo "ln -s #{current_path}/munin/jmx_ /etc/munin/plugins/jmx_java_cpu && chmod +x /etc/munin/plugins/jmx_java_cpu"
    sudo "ln -s #{current_path}/munin/jmx_ /etc/munin/plugins/jmx_java_hibernate_entity && chmod +x /etc/munin/plugins/jmx_java_hibernate_entity"
    sudo "ln -s #{current_path}/munin/jmx_ /etc/munin/plugins/jmx_java_threads && chmod +x /etc/munin/plugins/jmx_java_threads"
    sudo "ln -s #{current_path}/munin/jstat__gccount /etc/munin/plugins/jstat_gccount && chmod +x /etc/munin/plugins/jstat_gccount"
    sudo "ln -s #{current_path}/munin/jstat__gctime /etc/munin/plugins/jstat_gctime && chmod +x /etc/munin/plugins/jstat_gctime"
    sudo "ln -s #{current_path}/munin/jstat__heap /etc/munin/plugins/jstat_heap && chmod +x /etc/munin/plugins/jstat_heap"
  end
  

  after "deploy", "amee:munin_symlink"   

end
