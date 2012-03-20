#! /bin/sh
. auth.sh

. curl.conf
amee_url="/data/business/buildings/hotel/generic/980051122E8C?select=all"
#amee_url="/data/business/buildings/hotel/generic/980051122E8C?startDate=2020-06-09T11:29:59%2B0000"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-X GET \
	-H "Host: ${host_header}" \
	http://${amee_host}/${amee_url}