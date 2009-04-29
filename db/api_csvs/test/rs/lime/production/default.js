// Is caoMgoRatio given?
try{
  caoRatio = caoMgoRatio
}
 
catch (error){
  caoRatio = defCaoMgoRatio
}

// Is stoicRatio given?
try{
  SR = stoicRatio
}

catch (error){
  SR = defStoicRatio
}

// Is hydLime given?
try{
  HL = hydLime
}

catch (error){
  HL = defHydLime
}

// Is waterLime given?
try{
  WL = waterLime
}

catch (error){
  WL = defWaterLime
}

try{
  LKD = 1+ (lkdWeight / limeQuantity) * fracLkd * calcinationLKD
}

catch (error){
  LKD = defLkdCorrection
}

(caoRatio * SR) * limeQuantity * (1 - (HL * WL)) * LKD 
