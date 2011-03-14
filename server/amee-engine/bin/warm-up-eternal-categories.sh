#! /bin/sh

# Make sure to set the AMEE_HOST environment variable to match the AMEE host you want to work against.
AMEE_HOST=${AMEE_HOST:-stage.amee.com}

# This list should match the URLs in the dataSheetService bean definition of /conf/applicationContext.xml. 
curl "http://$AMEE_HOST/data/home/heating" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/home/appliances/energystar/kitchen/refrigerators" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/home/appliances/energystar/kitchen/dishwashers" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/home/appliances/energystar/office/imageEquipment/multiFunctionDevices" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/home/appliances/energystar/office/computers/notebooksAndTablets" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/home/appliances/energystar/entertainment/televisionsAndCombinationUnits" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/transport/plane/generic/airports/all/countries" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/transport/plane/generic/airports/all/codes" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/transport/car/specific/us" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/transport/van/specific/uk" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/transport/train/route/stations/uk" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/planet/country/uk/aggregate/actonco2/peoplelikeme/home" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/business/agriculture/livestock/manure/ch4/cattleandswine" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/business/agriculture/livestock/manure/n2o/cattleandswine" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/business/agriculture/livestock/manure/n2o/other" -H "accept:application/xml" -u load:l04d > /dev/null
curl "http://$AMEE_HOST/data/business/energy/stationaryCombustion" -H "accept:application/xml" -u load:l04d > /dev/null