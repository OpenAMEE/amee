#! /bin/sh
. auth.sh

. curl.conf
#amee_url="/data/business/buildings/hotel/generic"
amee_url="/data/business/buildings/hotel/generic?startDate=2020-06-09T11:29:59%2B0000"

type="json"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-X GET \
	-H "Host: ${host_header}" \
	http://${amee_host}/${amee_url}