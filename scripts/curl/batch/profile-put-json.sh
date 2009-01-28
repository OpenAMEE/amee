#! /bin/sh
curl -H "authToken:cym2T105l2B0rpuwXPApfJEgyTWZhIkwVSx0+G+p+/aBShv8P4tIOSM7p9tD18TjIB+srwUQVnPP54NSbZDt+ZjP2xKPfFmU7IaxNksfUu8=" \
	-H "Accept:application/json" \
	-H "Content-Type:application/json" \
	http://local.stage.co2.dgen.net/profiles/1BF853529B71?method=put \
	-d @profile-put.json
	

