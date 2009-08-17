#! /bin/sh
. auth.sh

. curl.conf

data="name_fr=taille"
amee_url="environments/5F5887BCF726/itemDefinitions/123C4A18B5D6/itemValueDefinitions/C6AA71697CCF?method=put"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
