#! /bin/sh
. auth.sh

. curl.conf

data="dataItemUid=9A9E8852220B&validFrom=20090901&distanceKmPerMonth=428.62872985519&name=trip_754537_start&end=false"
#data="dataItemUid=9A9E8852220B&distanceKmPerMonth=428.62872985519&name=trip_754537_start&end=false"
#data="dataItemUid=5CF2E792CFEA&validFrom=20090310&long2=101.7&name=trip_511961_finish&lat1=3.16667;end=true;long1=101.7;lat2=3.16667

amee_url="profiles/${amee_profile}/transport/car/generic"

type="xml"

curl -H "Accept:application/${type}" \
	 -b .cookies \
	 -v \
	 -o pc-post.xml \
	 -d ${data} \
	 http://${amee_host}/${amee_url}