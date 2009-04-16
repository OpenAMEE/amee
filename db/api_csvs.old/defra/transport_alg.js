function sanitiseString(theString) {
  var noSpacesString = "";
  theString = '' + theString;
  splitString = theString.split(" ");
  for(i = 0; i < splitString.length; i++)
    noSpacesString += splitString[i];
  
  var finalString = noSpacesString.toLowerCase();
  return finalString;
}

if (transportKgCO2PerKm == 0 &&     transportKgCO2PerPassengerJourney == 0) {
  if (sanitiseString(transportSize) == "fuelconsumption") {
    if (sanitiseString(transportFuel) == "diesel") {
      (2.63 * transportDistance) / transportKmPerLitre;
    } else if (sanitiseString(transportFuel) == "petrol"){
      (2.32 * transportDistance) / transportKmPerLitre;
    } else if (sanitiseString(transportFuel) == "lpg"){
      (1.495 * transportDistance) / transportKmPerLitre;      
    }
  } else {
    if (sanitiseString(transportFuel) == "diesel") {
      1.15 * (2.63 * transportDistance) / transportKmPerLitre;
    } else if (sanitiseString(transportFuel) == "petrol"){
      1.15 * (2.32 * transportDistance) / transportKmPerLitre;
    } else if (sanitiseString(transportFuel) == "lpg"){
      1.15 * (1.495 * transportDistance) / transportKmPerLitre;      
    }
  }
} else if (transportKgCO2PerKm != 0) {
  var multiplier = 0;
  if (transportTyresUnderInflated == "true") {
    multiplier = multiplier + 1;
  }
  if (transportAirConAverage == "true") {
    multiplier = multiplier + 5;
  }
  if (transportAirConFull == "true") {
    multiplier = multiplier + 25;
  }
  if (transportEcoDriving == "true") {
    multiplier = multiplier - 10;
  }
  if (transportAdhereToSpeedLimit == "true") {
    multiplier = multiplier - 25;
  }

  if (multiplier == 0) {
    multiplier = 1;
  } else if (multiplier > 0) {
    multiplier = 1 + (multiplier * 0.01);
  } else if (multiplier < 0) {
    multiplier = 1 - (multiplier * -0.01);
  }
  1.15*transportKgCO2PerKm * transportDistance * multiplier;
} else if (transportKgCO2PerPassengerJourney != 0) {
  transportKgCO2PerPassengerJourney * transportNumberOfJourneys;
}