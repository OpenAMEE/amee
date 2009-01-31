#! /bin/sh
. auth.sh

# Notes:
. curl.conf
amee_url="/data/hom/heating"
type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	http://${amee_host}/${amee_url}
