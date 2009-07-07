#! /bin/sh
. auth.sh

. curl.conf

data="valueDefinitionUid=23EB7F546969&value=100.0&startDate=2009-08-09T11:29:00%2B0000"
amee_url="/data/business/buildings/hotel/generic/980051122E8C"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}