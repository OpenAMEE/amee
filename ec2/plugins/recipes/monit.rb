namespace :monit do
  desc "Start monit"
  task :start do
    #run "/usr/bin/sv up monit"
  end
  
  desc "Stop monit"
  task :stop do
    #run "/usr/bin/sv down monit"
  end
  
  desc "Restart monit"
  task :restart do
    stop
    start
  end
  
  desc "Display status of monitored services"
  task :status do
    run "monit status"
  end
end
