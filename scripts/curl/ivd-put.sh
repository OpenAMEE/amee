#! /bin/sh
. auth.sh

. curl.conf

data="name_fr=taille"
#live amee_url="admin/itemDefinitions/123C4A18B5D6/itemValueDefinitions/C6AA71697CCF?method=put"
amee_url="admin/itemDefinitions/CF344E20E9AC/itemValueDefinitions/976FA4FB9B3D?method=put"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}
