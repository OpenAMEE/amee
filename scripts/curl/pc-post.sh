#! /bin/sh
. auth.sh

. curl.conf

data="dataItemUid=9B1BDFE5E16D&distance=1000"
amee_url="profiles/${amee_profile}/transport/car/generic"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
