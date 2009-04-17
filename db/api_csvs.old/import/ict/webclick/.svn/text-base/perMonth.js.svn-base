//throws an error if country undeclared
try { 
  var c=serverCountry;
}
catch(err){
  country='';
}
// if car's country value isn't set, get country
// from metadata
if (country == ''){
  country = profileFinder.getProfileItemValue('metadata', 'country');
}

if ((country == null) || (country == '')) {
	country = 'United States';
}

// get electricity value based on country
elecValue = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');

if(elecValue==null){//try ISO code
  elecValue = dataFinder.getDataItemValue('home/energy/electricityiso', 'country=' + country, 
'kgCO2PerKWh');
}

elecValue*numberOfClicks*kWhPerClick;
