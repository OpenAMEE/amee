namespace :haproxy do
  %w(start stop restart).each do |cmd|
    desc "#{cmd.capitalize} HAProxy"
    task cmd.to_sym do
      run "monit #{cmd} haproxy"
    end
  end
end
