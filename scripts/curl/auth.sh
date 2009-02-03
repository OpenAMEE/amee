#! /bin/sh
. curl.conf
echo ${amee_host}
amee_url="/auth/signIn?method=put"

curl -d next=/auth -d username=admin -d password=r41n80w \
	--dump-header .headers \
	--verbose \
	-c .cookies \
	http://${amee_host}/${amee_url}
