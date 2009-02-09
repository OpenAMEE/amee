#! /bin/sh
. auth.sh

# Notes:
# dataItemUid is the DataItem id NOT the ITEM_DEFINITION id.
# ValidFrom defaults to 1st day of current month.
. curl.conf
type="xml"
amee_url="/profiles/${amee_profile}/home/water/B8463F21A9FB"
#amee_url="/profiles/${amee_profile}"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
