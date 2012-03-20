#!/bin/bash
# Calculate the storage space used up by all tables in a given MySQL database
# Ben Dowling - www.coderholic.com
host=$1
database=$2
username=$3
password=$4

if [ ${#database} -eq 0 ]
then
 echo "Usage: $0 <host> <database> [username [password]]"
 exit
fi

if [ "$password" ]
  then
  password="-p$password"
fi

mysql="mysql -h $host -u $username $password $database"

$mysql -se "USE $database";

tables=$($mysql -se "SHOW TABLES")

totalData=0
totalIndex=0
totalTables=0

for table in $tables
 do
    output=$($mysql -se "SHOW TABLE STATUS LIKE \"$table\"\G")
    data=$(echo "$output" | grep Data_length | awk -F': ' '{print $2}')
    dataMegs=$(echo "scale=2;$data/1048576" | bc)
    index=$(echo "$output" | grep Index_length | awk -F': ' '{print $2}')
    indexMegs=$(echo "scale=2;$index/1048576" | bc)
    total=$(($index+$data))
    totalMegs=$(echo "scale=2;$total/1048576" | bc)

    echo "$table Data: ${dataMegs}MB Indexes: ${indexMegs}MB Total: ${totalMegs}MB"

    totalData=$(($totalData+$data))
    totalIndex=$(($totalIndex+$index))
    totalTables=$(($totalTables+1))
done

dataMegs=$(echo "scale=2;$totalData/1048576" | bc)
indexMegs=$(echo "scale=2;$totalIndex/1048576" | bc)
total=$(($totalIndex+$totalData))
totalMegs=$(echo "scale=2;$total/1048576" | bc)

echo "*** $totalTables Tables | Data: ${dataMegs}MB Indexes: ${indexMegs}MB Total: ${totalMegs}MB ***"
