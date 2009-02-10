if(type=='electricity'){
  // get country from metadata
  country = profileFinder.getProfileItemValue('metadata', 'country');
  if ((country == null) || (country == '')) {
	country = 'United Kingdom';
  }
  // get electricity value based on country
  kgCO2PerKWh = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');
}

if(kWhReadingCurrent>0 || kWhReadingLast>0){
  if(kWhReadingCurrent>=0 && kWhReadingLast>=0) {
    (kWhReadingCurrent-kWhReadingLast) * kgCO2PerKWh;
  }
  else {
    0.;
  }
} else if (kWhPerMonth != 0) {
  kWhPerMonth * kgCO2PerKWh;
} else if (litresPerMonth != 0) {
  litresPerMonth * kgCO2PerLitre;
} else if (kgPerMonth != 0) {
  kgPerMonth * kgCO2PerKg;
} else {
  0;
}