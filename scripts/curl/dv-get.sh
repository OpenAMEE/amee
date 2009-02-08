#! /bin/sh
. auth.sh

. curl.conf

#amee_url="data/transport/plane/generic/E98F73AE9B40/kgCO2PerPassengerJourney"
amee_url="/data/transport/plane/generic/AD63A83B4D41/kgCO2PerPassengerJourney"
curl -H "Accept:application/xml" \
	-H "ItemsPerPage:100" \
	-b .cookies \
	http://${amee_host}/${amee_url}
