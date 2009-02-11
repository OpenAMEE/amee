#! /bin/sh
. curl.conf
echo ${amee_host}
amee_url="/auth/signIn?method=put"

type="json"

curl -d next=/auth -d username=load -d password=l04d \
	-H "Accept:application/${type}" \
	--dump-header .headers \
	--verbose \
	-c .cookies \
	http://${amee_host}/${amee_url}
