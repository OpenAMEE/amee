#! /bin/sh
. ./curl.conf

type="atom+xml"
amee_url="profiles/${profileUid}/home/energy/electricity/A3BE0064FA9B"

curl http://${amee_host}/${amee_url} \
  -X PUT \
  -H "Accept: application/${type}" \
  -H "Content-type: application/${type}" \
	-u admin:r41n80w \
	-v \
	--data @post-data-category.atom \
	-o put-profile-item-response.xml
	
mate put-profile-item-response.xml 
