#! /bin/sh
. auth.sh

. curl.conf
#amee_url="/data/transport/car/generic/4F6CBCEE95F7"
amee_url="/data/transport/car/generic?startDate=20060101T0000"

curl -H "Accept:application/xml" \
	-H "ItemsPerPage:100" \
	--verbose \
	-b .cookies \
	http://${amee_host}/${amee_url}
