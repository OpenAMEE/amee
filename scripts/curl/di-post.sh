#! /bin/bash

. auth.sh

. curl.conf

startDate="2009-08-09T18:00:00%2B0000"
data="kWhPerYear=200&startDate=${startDate}"
amee_url="/data/home/appliances/computers/generic/B32624F8CD5F"

type="xml"
curl -H "Accept:application/${type}" \
	-b .cookies \
	-H "Host: ${host_header}" \
	-d ${data} \
	-v \
	http://${amee_host}/${amee_url}

