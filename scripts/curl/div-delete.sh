#! /bin/sh
. auth.sh

. curl.conf

amee_url="/data/business/buildings/hotel/generic/980051122E8C/67E1D5F86C0E"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-X DELETE \
	-H "Host: ${host_header}" \
	http://${amee_host}/${amee_url}