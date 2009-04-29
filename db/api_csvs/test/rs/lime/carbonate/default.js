// Is emission factor supplied by user
try{
  EF = EF
}

catch (error){
  EF = defEF
}

// Is calcination fraction supplied by user
try{
  CF = calcinationFrac
}

catch (error){
  CF = defCalcFrac
}
// Is LKD calcination fraction supplied by user
try{
  LKDF = lkdCalcFrac
}

catch (error){
  LKDF = defLkdCalcFrac
}

// Weight fraction calculation
try{
  WF = weightLkd
}

catch{
  WF = carbonateQuantity / lkdQuantity
}

(EF*carbonateQuantity*CF)-(lkdQuantity*WF(1-LKDF)*EF)
