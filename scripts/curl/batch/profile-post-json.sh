#! /bin/sh
curl -H "authToken:cym2T105l2AQ4C/EKLYG5CYatAZ+xiZupZKBPOS0GpU8iS2AS1mSoT8xsZIfhIB7Wv4vmoPY9U513pAyoRJwZO+kQVW96qyGQo4rA2CwXyg=" \
	-H "Accept:application/json" \
	-H "Content-Type:application/json" \
	http://local.stage.co2.dgen.net/profiles/1BF853529B71 \
	-d @profile-post.json
	
