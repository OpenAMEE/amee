if(type=='electricity'){
  // get country from metadata
  country = profileFinder.getProfileItemValue('metadata', 'country');
  if ((country == null) || (country == '')) {
	country = 'United Kingdom';
  }
  // get electricity value based on country
  kgCO2PerKWh = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');
}

seasonFac=1.;
try {//see if season exists
  var s = season;
  if(type=='coal' || type=='coking coal' || type=='biomass') {
    fuel='solidFuel';
  }
  else if(type=='gas' || type=='lpg'){
    fuel='gas';
  }
  else if(type=='electricity'){
    fuel='electricity';
    if(includesHeating=='true'){
       fuel='electricityWithHeating';
    }
  }
  else {
    fuel='oil';
  }
  seasonFac = 0.25/dataFinder.getDataItemValue('home/energy/uk/seasonal', 'name='+season+',energy='+fuel, 'percentage');
}
catch(err){
  seasonFac=1.;
}


if(kWhReadingCurrent>0 || kWhReadingLast>0){
  if(kWhReadingCurrent>=0 && kWhReadingLast>=0) {
    ret=(kWhReadingCurrent-kWhReadingLast) * kgCO2PerKWh;
  }
  else {
    ret=0.;
  }
} else if (kWhPerMonth != 0) {
  ret=kWhPerMonth * kgCO2PerKWh;
} else if (litresPerMonth != 0) {
  ret=litresPerMonth * kgCO2PerLitre;
} else if (kgPerMonth != 0) {
  ret=kgPerMonth * kgCO2PerKg;
} else {
  ret=0;
}
ret*seasonFac;