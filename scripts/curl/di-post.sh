#! /bin/sh
. auth.sh

. curl.conf

startDate="2009-08-09T11:00:00%2B0000"
value="100.5"

data="valueDefinitionUid=7B8149D9ADE7&value=${value}&startDate=${startDate}"
amee_url="/data/home/appliances/computers/generic/B32624F8CD5F"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}