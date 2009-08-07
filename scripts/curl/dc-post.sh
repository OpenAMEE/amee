#! /bin/sh
. auth.sh

. curl.conf
amee_url="/data/business/energy"
data="aliasedTo=E71CA2FCFFEA&name=symlink2elec&path=symlink2elec2&newObjectType=DC"
type="json"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-H "Host: ${host_header}" \
	-d ${data} \
	http://${amee_host}/${amee_url}