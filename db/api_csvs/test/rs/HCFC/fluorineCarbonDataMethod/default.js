// default or user value for carbon content factor
try{
  CCF = carbonContFact
}

catch (error){
  CCF = defCarbonContFact
}

// default or user value for fluorine content factor
try{
  FCF = fluorineContFact
}

catch (error){
  FCF = defFluorineContFact
}

// default or user value for loss of HCFC-22 production efficiency
try{
  loss = lossHCFC22
}

catch (error){
  loss = defLossHCFC22
}

// calculate HFC-23 emissions 

value = ((((100-carbonBalEff)/100)*CCF*loss)+(((100-fluorineBalEff)/100)*FCF*loss))/2
value = value * quantityHCFC22 * timeHFC23

// give CO2 equivalent value
value = value * 11700
