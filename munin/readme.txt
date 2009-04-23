*** AMEE Munin Node Extensions ***

This directory contains a directory structure and files reflecting AMEE extensions for a Munin node install on Debian.

* Steps To Install *

1) Copy across files from: /usr/share/munin/plugins

2) Update config in: /etc/munin/plugin-conf.d/munin-node

3) Do the following symlinks:

ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_data_source
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_errors
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_profile_item_values
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_profile_items
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_profiles
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_amee_transactions
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_java_cpu
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_java_hibernate_entity
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_java_process_memory
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_java_threads
ln -s /opt/local/lib/munin/plugins/jstat__gccount /etc/munin/plugins/jstat_gccount
ln -s /opt/local/lib/munin/plugins/jstat__gctime /etc/munin/plugins/jstat_gctime
ln -s /opt/local/lib/munin/plugins/jstat__heap /etc/munin/plugins/jstat_heap

4) Restart Munin node: /etc/init.d/munin-node restart

5) Check Munin node: telnet localhost 4949


