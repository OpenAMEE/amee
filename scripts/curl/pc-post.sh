#! /bin/sh
. auth.sh

. curl.conf

startDate="2009-08-09T23:29:00%2B0000"
distance="24.0"
tyres="false"
occupants="2"

data="dataItemUid=9A9E8852220B&distance=${distance}&occupants=${occupants}&tyresUnderinflated=${tyres}&startDate=${startDate}"
amee_url="profiles/E097EBAE86BC/transport/car/generic"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
