
// if specific emission factor is given by user
try {
  value = (acidQuantity * specEmFact)
  value = value * (1-(destrFact*abateFact))
  }
// if no specific emission factor use default
catch (error) {
  value = (acidQuantity * defEmFact)
  }
// calculate equivalent CO2 value
value * 310

