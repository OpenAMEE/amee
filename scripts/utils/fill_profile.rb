#!/usr/bin/env ruby
require 'rubygems'
require 'amee'

# fill_profile.rb <server> <username> <password> <profile_uid> <category_path> <dataItemUid>
SERVER = ARGV[0]
USERNAME = ARGV[1]
PASSWORD = ARGV[2]
PROFILE = ARGV[3]
CATEGORY = ARGV[4]
UID = ARGV[5]

amee = AMEE::Connection.new(SERVER, USERNAME, PASSWORD)

time = Time.now

i = 0
while true do
  AMEE::Profile::Item.create_without_category(amee, "/profiles/#{PROFILE}#{CATEGORY}", UID, :start_date => time - i.minutes, :get_item => false)
  i += 1
  puts "#{i} items created"
end
