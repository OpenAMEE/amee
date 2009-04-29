// Find out if user has specified a mass, a volume, an energy

var isMass
var isVol
var isNRG
var massAmount
var energyAmount
var volumeAmount

try {
    massAmount=mass;
    isMass=true;
} catch (error)
{
    massAmount=0.0;
    isMass=false ;
}

try {
    energyAmount=NRG;
    isEnergy=true;
} catch (error)
{
    energyAmount=0.0;
    isEnergy=false;
}

try {
    volumeAmount=volume;
    isVolume=true;
} catch (error)
{
    volumeAmount=0.0;
    isVolume=false;
}

carbon(massAmount,volumeAmount,energyAmount,isMass,isVolume,isEnergy);
//print(carbon(0.0,0.0,1.0,false,false,true))

function carbon(massAmount,volumeAmount,energyAmount,isMass,isVolume,isEnergy) {

// report error code as zero carbon amount if multiple values asserted
    if (isMass&&isVolume) return 0;
    if (isVolume&&isEnergy) return 0;
    if (isEnergy&&isMass) return 0;
    if (!isEnergy&&!isMass&&!isVolume) return 0;

    var CO2;

// calculate the amount of gas

    CO2=calcGHG(massAmount,volumeAmount,energyAmount,
		isMass,isVolume,isEnergy);
// weighted sum
    return CO2;
}

function calcGHG(massAmount,volumeAmount,energyAmount,
		 isMass,isVolume,isEnergy) {

    if (isVolume)	energyAmount=volumeAmount*HVVolume;
    if (isMass) 	energyAmount=massAmount*HVMass;
    return EFNRGCO2*energyAmount;
}