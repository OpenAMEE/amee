load 'deploy' if respond_to?(:namespace) # cap2 differentiator
Dir['deploy/plugins/*/recipes/*.rb'].each { |plugin| load(plugin) }
load 'deploy/deploy'

require 'capistrano/ext/multistage'
require 'capistrano/ext/monitor'

