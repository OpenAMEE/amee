#! /bin/sh
. curl.conf

amee_url="/auth/signIn?method=put"

curl -d next=/auth -d username=admin -d password=r41n80w \
#curl -d next=/auth -d username=demo -d password=am33d3m0 \
	--dump-header .headers \
	-c .cookies \
	http://${amee_host}/${amee_url}
