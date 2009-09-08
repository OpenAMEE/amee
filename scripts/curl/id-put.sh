#! /bin/sh
. auth.sh

. curl.conf

data="name_fr=voiture generique"
amee_url="environments/5F5887BCF726/itemDefinitions/CF344E20E9AC?method=put"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
