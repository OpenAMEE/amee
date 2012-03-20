#! /bin/sh
. ./curl.conf

type="atomsvc+xml"
amee_url="profiles/${profileUid}/service"

curl http://${amee_host}/${amee_url} \
  -H "Accept:application/${type}" \
	-u admin:r41n80w \
	-v \
	-o service-root-response.xml
	
mate service-root-response.xml 
