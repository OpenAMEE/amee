#! /bin/sh
. auth.sh

# Notes:
. curl.conf
amee_url="/data/transport/car/generic"
type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	http://${amee_host}/${amee_url}
