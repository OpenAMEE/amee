#! /bin/sh
. ./curl.conf

type="atom+xml"

amee_url="profiles/${profileUid}/home/energy/electricity/C61C589B8893"

curl http://${amee_host}/${amee_url} \
  -X DELETE \
  -H "Accept: application/${type}" \
  -u admin:r41n80w \
  -v \
  -o delete-profile-item-response.xml
	
mate delete-profile-item-response.xml 
