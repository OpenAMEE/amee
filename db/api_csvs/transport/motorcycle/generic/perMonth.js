//Get the fuel factor, kgCO2 per litre
//All motorcycles are petrol
fuelFac = dataFinder.getDataItemValue('home/energy/quantity', 'type=petrol', 'kgCO2PerLitre');

// now calculate the monthly kgCO2 emissions
fac=0.;
if(kmPerLitreOwn!=null && kmPerLitreOwn>0){
    fac=fuelFac/kmPerLitreOwn;
}
else if(kmPerLitre!=null && kmPerLitre>0){
    fac=1.15*fuelFac/kmPerLitre;
}
else {
    fac= kgCO2PerKm;
}
if(occupants>0)
    fac * distanceKmPerMonth/occupants;
else //unlike generic cars, 1.15 factor already applied
    fac * distanceKmPerMonth;