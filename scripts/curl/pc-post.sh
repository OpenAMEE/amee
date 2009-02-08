#! /bin/sh
. auth.sh

. curl.conf

data="dataItemUid=4F6CBCEE95F7&distanceKmPerMonth=1000&validFrom=20081215"
amee_url="profiles/${amee_profile}/transport/car/generic"

data="dataItemUid=313B3A8A50F8&litresPerMonth=100000&name=digisthebest17"
amee_url="profiles/${amee_profile}/home/water"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
