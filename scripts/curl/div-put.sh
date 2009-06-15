#! /bin/sh
. auth.sh

. curl.conf

amee_url="/data/business/buildings/hotel/generic/980051122E8C/E10F929766F7"
data="startDate=2020-06-09T11:30:00%2B0000"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-X PUT \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}