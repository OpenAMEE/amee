#! /bin/sh
. auth.sh

. curl.conf

data="name=AADigID"
amee_url="admin/itemDefinitions"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
