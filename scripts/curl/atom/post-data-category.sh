#! /bin/sh
. ./curl.conf

type="atom+xml"
amee_url="profiles/${profileUid}/home/energy/electricity"

curl http://${amee_host}/${amee_url} \
  -X POST \
  -H "Accept:application/${type}" \
  -H "Content-type: application/${type}" \
	-u admin:r41n80w \
	-v \
	--data @post-data-category.atom \
	-o post-data-category-response.xml
	
mate post-data-category-response.xml 
