#! /bin/bash

. auth.sh

. curl.conf

data="newObjectType=DI&subregion=DDDD&massCO2PerEnergy=111&massN2OPerEnergy=222&massCH4PerEnergy=0.1&source=Diggory"
amee_url="/data/business/energy/us/subregion"

type="xml"
curl -H "Accept:application/${type}" \
	-b .cookies \
	-H "Host: ${host_header}" \
	-d ${data} \
	-v \
	http://${amee_host}/${amee_url}

