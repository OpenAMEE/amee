#! /bin/sh
. ./curl.conf

type="atom+xml"

amee_url="profiles/${profileUid}/home/energy/electricity/B002B3D1A4A0/kWh"

curl http://${amee_host}/${amee_url} \
  -X DELETE \
  -H "Accept: application/${type}" \
	-u admin:r41n80w \
	-v \
	-o delete-profile-item-value-response.xml
	
mate delete-profile-item-value-response.xml