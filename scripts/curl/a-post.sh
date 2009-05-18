#! /bin/sh
. auth.sh

. curl.conf

data="name=AADigA"
amee_url="environments/5F5887BCF726/itemDefinitions/EACC85C156EA/algorithms"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
