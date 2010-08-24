#!/bin/bash
for i in {1..1}
do
   echo "**** Iteration $i ****"
   time groovy -cp "/Development/repository/mysql/mysql-connector-java/5.1.10/mysql-connector-java-5.1.10.jar:/Development/repository/com/amee/amee-base/1.0.4-SNAPSHOT/amee-base-1.0.4-SNAPSHOT.jar:/Development/repository/net/sf/opencsv/opencsv/2.1/opencsv-2.1.jar:/Development/repository/joda-time/joda-time/1.6/joda-time-1.6.jar" generate_new_design.groovy -p HhGCXdfbU2xA6i > /dev/null
   time mysql amee_new_design < import_csv_new_design.sql
done
echo "You got this email because the simulated LCA data loading on db3 is complete." | mail -s "LCA data loading complete" "dig@amee.cc"