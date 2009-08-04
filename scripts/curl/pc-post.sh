#! /bin/sh
. auth.sh

. curl.conf

startDate="2013-08-02T16:08:00%2B0000"
distance="24.0"
tyres="false"
occupants="2"

data="dataItemUid=9A9E8852220B&validFrom=20090701&distanceKmPerMonth=428.62872985519&name=trip_754537_start&end=false"
#data="dataItemUid=5CF2E792CFEA&validFrom=20090310&long2=101.7&name=trip_511961_finish&lat1=3.16667;end=true;long1=101.7;lat2=3.16667
amee_url="profiles/${amee_profile}/transport/car/generic"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
