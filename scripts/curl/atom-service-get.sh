#! /bin/sh
. auth.sh
. curl.conf

type="atomsvc+xml"
amee_url="profiles/03B0CE0F42EC/home/appliances/service"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
