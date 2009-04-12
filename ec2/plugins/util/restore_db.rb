#!/usr/bin/env ruby
require File.dirname(__FILE__) + "/database_backup"
DatabaseBackup.restore! ARGV[0]