#! /bin/sh
. auth.sh

# Notes:
# dataItemUid is the DataItem id NOT the ITEM_DEFINITION id.
# ValidFrom defaults to 1st day of current month.
. curl.conf
type="atom+xml"
amee_url="/profiles/${amee_profile}/transport/car/generic/65EF5B4592AC"
#amee_url="/profiles/${amee_profile}"

curl -H "Accept:application/${type}" \
	-b .cookies \
	--verbose \
	http://${amee_host}/${amee_url}
