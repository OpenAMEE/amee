#! /bin/sh
. auth.sh

. curl.conf
amee_url="/data/transport/car/generic?method=put"
data="name_fr=generique"
type="json"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}