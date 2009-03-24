function adjustedFloorArea(totalFloorArea) {
	var adjustedtfa = 0.0;

	if (totalFloorArea > 420) {
		adjustedtfa = 8.0;
	}
	else 
	{
		adjustedtfa = 0.035*totalFloorArea-0.000038*(totalFloorArea*totalFloorArea);
	}
	
	return adjustedtfa;
}

function hotWaterUsage(adjustedTotalFloorArea) {
	var hWUse = 0.0;
	
	hWUse=(25*adjustedTotalFloorArea)+38;
		
	return hWUse;
}

function hotWaterEnergyContent(adjustedTotalFloorArea) {
	var hWEC = 0.0;
	
	hWEC=((61*adjustedTotalFloorArea)+92)*0.85*8.76;
		
	return hWEC;
}

function distributionLosses(adjustedTotalFloorArea) {
	var hWEC = 0.0;
	
	hWEC=((61*adjustedTotalFloorArea)+92)*0.15*8.76;
		
	return hWEC;
}

try{
profileFinder.setProfileItemValue("error","error");

totalFloorAreaValue = tfa;
adjustedtfa=adjustedFloorArea(tfa);

waterusage=hotWaterUsage(tfa);
waterenergyusage=hotWaterEnergyContent(tfa);
distributionloss=distributionLosses(tfa);

profileFinder.setProfileItemValue("waterusage",waterusage);
profileFinder.setProfileItemValue("waterenergyusage",waterenergyusage);
profileFinder.setProfileItemValue("distributionloss",distributionloss);

profileFinder.setProfileItemValue("error","valid");

result=0;
}
catch(err) {
profileFinder.setProfileItemValue("error","exception");
}





