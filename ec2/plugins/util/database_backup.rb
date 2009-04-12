require "rubygems"
require "aws/s3"
require "fileutils"
require "ezcrypto"

class DatabaseBackup
  class << self
    TMP_BACKUP_FILE = "/tmp/mysql-backup-#{Time.now.to_i}"
    BACKUP_BUCKET = "#{ENV['ACCOUNT_ID']}-rwec2-mysql-backups"
    
    AWS::S3::Base.establish_connection!(
      :access_key_id => ENV['ACCESS_KEY_ID'],
      :secret_access_key => ENV['SECRET_ACCESS_KEY']
    )

    def backup!
      unencrypted_contents = `mysqldump --all-databases`
      key = EzCrypto::Key.with_password ENV['SECRET_ACCESS_KEY'], 'saltoftheearth'
      encrypted_contents = key.encrypt unencrypted_contents
      File.open(TMP_BACKUP_FILE, 'w') { |f| f << encrypted_contents}
      begin
        AWS::S3::Bucket.find BACKUP_BUCKET
      rescue AWS::S3::NoSuchBucket
        AWS::S3::Bucket.create BACKUP_BUCKET
      end
      AWS::S3::S3Object.store(TMP_BACKUP_FILE, open(TMP_BACKUP_FILE), BACKUP_BUCKET)
    ensure
      FileUtils.rm_f TMP_BACKUP_FILE if File.exists? TMP_BACKUP_FILE
    end
    
    def restore!(version)
      begin
        all_objects = AWS::S3::Bucket.objects(BACKUP_BUCKET)
        latest = if version
          all_objects.detect { |o| o.key =~ /^.*(#{latest}).*$/ }
        else
          all_objects.sort_by { |o| Date.parse(o.about[:last_modified]) }.last
        end
        if latest
          encrypted_contents = latest.value
          key = EzCrypto::Key.with_password ENV['SECRET_ACCESS_KEY'], 'saltoftheearth'
          unencrypted_contents = key.decrypt encrypted_contents
          File.open(TMP_BACKUP_FILE, 'w') { |f| f << unencrypted_contents }
          %x(mysql < #{TMP_BACKUP_FILE})
        end
      rescue AWS::S3::NoSuchBucket
        STDERR.puts "Nothing to restore"
      ensure
        FileUtils.rm_f TMP_BACKUP_FILE if File.exists? TMP_BACKUP_FILE
      end
    end    
  end
end
