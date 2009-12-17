#! /bin/sh
. auth.sh

# Notes:
# dataItemUid is the DataItem id NOT the ITEM_DEFINITION id.
# ValidFrom defaults to 1st day of current month.
. curl.conf
data="numberOfPeople=2&representation=full&returnUnit=oz"
type="xml"
#amee_url="/profiles/${amee_profile}/home/appliances/computers/generic/056CC619C7D6?method=put"
amee_url="/profiles/247D8A1820CC/business/buildings/hotel/generic/BD1C37F93B9D?method=put"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
