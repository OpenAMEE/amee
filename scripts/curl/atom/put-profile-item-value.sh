#! /bin/sh
. ./curl.conf

type="atom+xml"
amee_url="profiles/${profileUid}/home/energy/electricity/A3BE0064FA9B/KWh"

curl http://${amee_host}/${amee_url} \
  -X PUT \
  -H "Accept: application/${type}" \
  -H "Content-type: application/${type}" \
	-u admin:r41n80w \
	-v \
	--data @put-profile-item-value.atom \
	-o put-profile-item-value-response.xml
	
mate put-profile-item-value-response.xml 
