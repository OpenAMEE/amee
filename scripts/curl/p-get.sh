#! /bin/sh
. auth.sh

. curl.conf
amee_url="/profiles/4B63A2E1B1E4"

curl -H "Accept:application/xml" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
