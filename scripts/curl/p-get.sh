#! /bin/sh
. auth.sh

. curl.conf
amee_url="/profiles/${amee_profile}"

curl -H "Accept:application/xml" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
