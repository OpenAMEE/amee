#! /bin/sh
. auth.sh

. curl.conf

data="dataItemUid=5275ECC6B97F"
amee_url="profiles/${amee_profile}/home/appliances/computers/generic"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
