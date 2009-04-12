module S3
  def list_db_backups akid, sak, account_id
    AWS::S3::Base.establish_connection!(:access_key_id => akid, :secret_access_key => sak)
    AWS::S3::Bucket.find("#{account_id}-rwec2-mysql-backups").objects.each { |o| STDOUT.puts o.key }
  end
  module_function :list_db_backups
end