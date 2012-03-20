#! /bin/sh
. auth.sh

. curl.conf

amee_url="/profiles/${amee_profile}/home/water/B8463F21A9FB/litresPerMonth"
curl -H "Accept:application/xml" \
	-H "ItemsPerPage:100" \
	-b .cookies \
	http://${amee_host}/${amee_url}
