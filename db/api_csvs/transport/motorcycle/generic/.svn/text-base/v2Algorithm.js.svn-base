//Get the fuel factor, kgCO2 per litre
//All motorcycles are petrol
fuelFac = dataFinder.getDataItemValue('home/energy/quantity', 'type=petrol', 'kgCO2PerLitre');

// now calculate the monthly kgCO2 emissions
fac=0.;
if(ownFuelConsumption!=null && ownFuelConsumption>0){
    fac=fuelFac/ownFuelConsumption;
}
else if(fuelConsumption!=null && fuelConsumption>0){
    fac=1.15*fuelFac/fuelConsumption;
}
else {
    fac= kgCO2PerKm;
}

if(useTypicalDistance=='true'){
  distance=typicalDistance;
}

if(occupants>0)
    fac * distance/occupants;
else //unlike generic cars, 1.15 factor already applied
    fac * distance;
