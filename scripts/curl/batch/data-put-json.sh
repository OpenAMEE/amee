#! /bin/sh
curl -H "authToken:cym2T105l2BdqEfZiTbvgHEdTgIhi6BWJkhuNCr+YHIA2YxTmB2w19KaQdhlr9L6l1wWYm33ni0lKmvNM+TKD7pbxAnysaxzVskWFqq7qqU=" \
	-H "Accept:application/json" \
	-H "Content-Type:application/json" \
	http://local.stage.co2.dgen.net/data/home/appliances/kitchen/generic?method=put \
	-d @data-put.json
	

	
	
