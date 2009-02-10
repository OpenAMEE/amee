function sanitiseString(theString) {
  var noSpacesString = "";
  theString = '' + theString;
  splitString = theString.split(" ");
  for(i = 0; i < splitString.length; i++)
    noSpacesString += splitString[i];
  
  var finalString = noSpacesString.toLowerCase();
  return finalString;
}

//if numberOfPeople isn't set, get peopleInHousehold from metadata
try { 
  var c=numberOfPeople;
}
catch(err){
  numberOfPeople=profileFinder.getProfileItemValue('metadata', 'peopleInHousehold');
}

// get country from metadata
country = profileFinder.getProfileItemValue('metadata', 'country');
if ((country == null) || (country == '')) {
	country = 'United Kingdom';
}

// get electricity value based on country
elecValue = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');

if(elecValue==null){//try ISO code
  elecValue = dataFinder.getDataItemValue('home/energy/electricityiso', 'country=' + country, 'kgCO2PerKWh');
}

gasValue = dataFinder.getDataItemValue('home/energy/quantity', 'type=gas', 'kgCO2PerKWh');

sanType = sanitiseString(type);

result=gasValue*(baseKWhPerYear+numberOfPeople*perPersonKWhPerYear);

result/12;
//original algorithm was just this line: kgCO2PerYear / 12;
