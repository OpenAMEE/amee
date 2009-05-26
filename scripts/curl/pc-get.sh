#! /bin/sh
. auth.sh

. curl.conf
#amee_url="/profiles/${amee_profile}/transport/car/generic?profileDate=200811"
#amee_url="/profiles/${amee_profile}/transport/car/generic?profileDate=20081102"
#amee_url="/profiles/${amee_profile}/transport/car/generic?startDate=20081129T0000&endDate=20081130T0000&v=2.0&mode=prorata"
#amee_url="/profiles/${amee_profile}/transport/car/generic?v=2.0&startDate=20081117T1354&endDate=20081202T1354&selectBy=start"
#amee_url="/profiles/${amee_profile}/transport/car/generic/?v=2.0&startDate=20081124T1507&endDate=20081209T1507"
#amee_url="profiles/${amee_profile}/home/appliances/computers/generic?profileDate=201004"

amee_url="profiles/${amee_profile}/home/energy/quantity?itemsPerPage=100"

type="atom+xml"

curl http://${amee_host}/${amee_url} \
  -H "Accept:application/${type}" \
	-b .cookies \
	-v \
  -o get-data-category-response.xml  