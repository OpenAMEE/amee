#! /bin/sh
. curl.conf
echo ${amee_host}
amee_url="/auth/signIn?method=put"

type="xml"

curl -d next=/auth -d username=admin -d password=r41n80w \
	-H "Accept:application/${type}" \
	--dump-header .headers \
	--verbose \
	-c .cookies \
	http://${amee_host}/${amee_url}
