#! /bin/sh
. curl.conf
echo ${amee_host}
amee_url="auth/signIn?method=put"

type="xml"

curl -d next=/auth -d username=${user} -d password=${pswd} \
	-H "Accept:application/${type}" \
	--dump-header .headers \
	-H "Host: ${host_header}" \
	-c .cookies \
	http://${amee_host}/${amee_url}
