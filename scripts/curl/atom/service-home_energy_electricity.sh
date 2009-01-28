#! /bin/sh
. ./curl.conf

type="atomsvc+xml"
amee_url="profiles/${profileUid}/service/home/energy/electricity"

curl http://${amee_host}/${amee_url} \
  -H "Accept:application/${type}" \
	-u admin:r41n80w \
	-v \
	-o service-home_energy-electricity-response.xml
	
mate service-home_energy-electricity-response.xml 
