namespace :db do
  %w(start stop restart).each do |cmd|
    desc "#{cmd.capitalize} mysql-server on the instance"
    task cmd.to_sym do
      run "/etc/init.d/mysql #{cmd}"
    end
  end
  
  desc "Back up the instance's database to S3"
  task :backup do
    run "source $HOME/.amazon_keys && /usr/lib/site_ruby/rubyworks/backup_db"
  end
  
  desc <<-DESC
  Restore the instance's database to the latest copy from S3, 
  or specify the version by setting the VERSION parameter to 
  the desired timestamp, e.g 'cap db:restore VERSION=<timestamp>'
  DESC
  task :restore do
    version = ENV['VERSION']
    run "source $HOME/.amazon_keys && /usr/lib/site_ruby/rubyworks/restore_db #{version}"
  end
  
  desc "Display a list of database backups from S3"
  task(:list_backups) { S3.list_db_backups access_key_id, secret_access_key, account_id }
  
  desc <<-DESC
  Remove Debian's default InnoDB files, start MySQL (creates new InnoDB files)
  and restore to the latest database back up from S3
  DESC
  task :setup do
    stop
    run <<-CMD
      rm -rf /var/lib/mysql/ib*
    CMD
    start
    restore
  end
end
