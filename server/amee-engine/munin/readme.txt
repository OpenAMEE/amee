*** Notes ***

The software in this directory was downloaded from http://exchange.munin-monitoring.org on 26/07/2010.

*** Create symlinks like this ***

export SCRIPT_PATH=/var/www/apps/amee-stage/current/munin
export INSTANCE_NAME=platform_stage
sudo ln -s "$SCRIPT_PATH"/jstat__gccount "$INSTANCE_NAME"_jstat_gccount
sudo ln -s "$SCRIPT_PATH"/jstat__gctime "$INSTANCE_NAME"_jstat_gctime
sudo ln -s "$SCRIPT_PATH"/jstat__heap "$INSTANCE_NAME"_jstat_heap
sudo ln -s "$SCRIPT_PATH"/jmx_ "$INSTANCE_NAME"_jmx_amee_data_source
sudo ln -s "$SCRIPT_PATH"/jmx_ "$INSTANCE_NAME"_jmx_amee_transactions
sudo ln -s "$SCRIPT_PATH"/jmx_ "$INSTANCE_NAME"_jmx_java_cpu
sudo ln -s "$SCRIPT_PATH"/jmx_ "$INSTANCE_NAME"_jmx_java_hibernate_entity
sudo ln -s "$SCRIPT_PATH"/jmx_ "$INSTANCE_NAME"_jmx_java_threads
