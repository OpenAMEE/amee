#! /bin/sh
. auth.sh
. curl.conf

type="json"
amee_url="profiles/actions"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
