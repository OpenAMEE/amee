#! /bin/sh
. auth.sh

. curl.conf
amee_url="/data/home/heating/27B64C547280"
amee_url="data/transport/car/generic/drill"

curl -H "Accept:application/xml" \
	-H "ItemsPerPage:100" \
	--verbose \
	-b .cookies \
	http://${amee_host}/${amee_url}
