//multiply quantity of acid by EF is given, or by default EF if no entry
try{
  value = adipicQuantity * emFact
}

catch (error){
  value = adipicQuantity * defEmFact
}

//whether given abatement factor or not
try{
  AF = abateFact
}

catch (error){
  AF = defAbateFact
} 

//whether destruction factor or not
try{
  DF = destrFact
}

catch (error){
  DF = defDestrFact
}

// compute nitrous oxide emissions (N20/yr)
value = value * (1 - (AF*DF))

// compute equivalent CO2
value * 310
