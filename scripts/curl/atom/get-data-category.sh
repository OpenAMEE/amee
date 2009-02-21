#! /bin/sh
. ./curl.conf

type="atom+xml"
amee_url="profiles/${profileUid}/transport/car/generic?startDate=2008-01-01T00:00%2B0000"

curl http://${amee_host}/${amee_url} \
	-u admin:r41n80w \
	-v \
	-o get-data-category-response.xml
	
mate get-data-category-response.xml 
