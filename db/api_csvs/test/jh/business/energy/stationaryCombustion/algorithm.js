// Find out if user has specified a mass, a volume, an energy

var isMass
var isVol
var isNRG
var massAmount
var energyAmount
var volumeAmount

gwpCH4=21;
gwpN2O=310;
// normalize the input

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

// report error code as negative carbon amount if multiple values asserted
    if (isMass&&isVolume) return -1;
    if (isVolume&&isEnergy) return -2;
    if (isEnergy&&isMass) return -3;
    if (!isEnergy&&!isMass&&!isVolume) return -4;

    var CH4;
    var N2O;
    var CO2;

// calculate the amount of each gas
    CH4=calcGHG('CH4',massAmount,volumeAmount,energyAmount,
		isMass,isVolume,isEnergy);
    N2O=calcGHG('N2O',massAmount,volumeAmount,energyAmount,
		isMass,isVolume,isEnergy);
    CO2=calcGHG('CO2',massAmount,volumeAmount,energyAmount,
		isMass,isVolume,isEnergy);
// weighted sum
    return CH4*gwpCH4+N2O*gwpN2O+CO2;
}

function calcGHG(gaslabel,massAmount,volumeAmount,energyAmount,
		 isMass,isVolume,isEnergy) {

    if (isVolume)	energyAmount=volumeAmount*density*LHV;
    if (isMass) 	energyAmount=massAmount*LHV;
    return energyfactor(gaslabel)*energyAmount;
}


function energyfactor(gaslabel) {
    var conv;
    if (useHHV) {
	conv=HHVConversion;
    }
    else {
	conv=1.0;
    }
    if (gaslabel=='CH4')
	return EFLHVNRGCH4;
    if (gaslabel=='N2O')
	return EFLHVNRGN2O;
    if (gaslabel=='CO2')
	return EFLHVNRGCO2;
    return 0.0;
}