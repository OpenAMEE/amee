#! /bin/sh
. auth.sh

. curl.conf

#data="dataItemUid=D462562FCB92&validFrom=20091202T0000&kWhPerMonth=1000"
#data="dataItemUid=4F6CBCEE95F7&validFrom=20100401&end=true"
#data="dataItemUid=4F6CBCEE95F7&startDate=20081125T0100&endDate=20081126T0115&v=2.0"
#data="dataItemUid=4F6CBCEE95F7&distanceKmPerMonth=1000&name=test&validFrom=20081215"
#data="dataItemUid=4F6CBCEE95F7&validFrom=20091202&name=1&distance=1000"
#amee_url="/profiles/B74EC806243F/home/appliances/computers/generic"
#stage_amee_url="/profiles/6A51FBF3EFD0/transport/car/generic"
#data="dataItemUid=4F6CBCEE95F7&startDate=20100401T1430&distance=1000"
#amee_url="/profiles/B74EC806243F/transport/car/generic?v=2.0"
#amee_url="/profiles/B74EC806243F/home/energy/electricity/?v=2.0&returnPerUnit=year"
#amee_url="/profiles/B74EC806243F/home/energy/electricity/"

data="dataItemUid=D462562FCB92&kWh=200.234&name=test3"

#dev profile
#amee_url="/profiles/1E7D3E107BD5/transport/car/generic"

#local profile
amee_url="profiles/B74EC806243F/home/energy/electricity"

type="xml"

curl -H "Accept:application/${type}" \
	-b .cookies \
	-u admin:r41n80w \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
