//kgCO2PerKmPassenger * distance
//throws an error if country undeclared
try { 
  var c=country;
}
catch(err){
  country='';
}
// if country value isn't set, get country
// from metadata
if (country == ''){
  country = profileFinder.getProfileItemValue('metadata', 'country');
}

if ((country == null) || (country == '')) {
	country = 'United Kingdom';
}
// now calculate the monthly kgCO2 emissions
fac=1.;
if (country == 'Ireland' || country == 'IE') {
    fac = kgCO2PerPassengerKmIE;
} else {
    fac= kgCO2PerKmPassenger;
}

fac * distance;
