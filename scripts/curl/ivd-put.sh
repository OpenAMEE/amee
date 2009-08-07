#! /bin/sh
. auth.sh

. curl.conf

data="name_fr=french_value"
amee_url="environments/5F5887BCF726/itemDefinitions/125BAAD1DA2E/itemValueDefinitions/DE3E806B7DD4?method=put"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
