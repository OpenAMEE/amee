#! /bin/sh
. auth.sh

# Notes:
# dataItemUid is the DataItem id NOT the ITEM_DEFINITION id.
# ValidFrom defaults to 1st day of current month.
. curl.conf
data="numberOwned=2"
type="xml"
#amee_url="/profiles/B74EC806243F/home/appliances/computers/generic/056CC619C7D6?method=put"
amee_url="/profiles/B74EC806243F/home/energy/electricity/F8D1BED7C9A8?method=put"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	-d ${data} \
	http://${amee_host}/${amee_url}
