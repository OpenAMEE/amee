// get country from metadata
country = profileFinder.getProfileItemValue('metadata', 'country');
if ((country == null) || (country == '')) {
	country = 'United Kingdom';
}

// get electricity value based on country
countryElecValue = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');

if(countryElecValue==null){//try ISO code
  countryElecValue = dataFinder.getDataItemValue('home/energy/electricityiso', 'country=' + country, 'kgCO2PerKWh');
}

if(device=='standby'){//standby is special!
        standbyCO2=0;
	profileItems = profileFinder.getProfileItems();
	for (i = 0; i < profileItems.size(); i++) {
		item = profileItems.get(i);
		itemValues = item.getItemValuesMap();
                try {//handle where name is undefined
                  name=item.getName();
                }
                catch(err){
                  name='';
                }
                if(name!="standby") {
                  try {//in case amount per month undef
  standbyCO2+=parseFloat(item.getAmountPerMonth());
                  }
                  catch(err){}//do nothing
                }
	}
        if (onStandby=="never"){
          -0.05*standbyCO2;
        } else if (onStandby=="sometimes"){
          0.0*standbyCO2;
        } else if (onStandby=="mostly"){
          0.05*standbyCO2;
        } else if (onStandby=="always"){
          0.08*standbyCO2;
        }
} //calculate the monthly kgCO2 emissions
else if (countryElecValue != null) {
    numberOwned * kWhPerYear * countryElecValue;
}
else {
	0;
}
