function greatCircle() {
         d2r=0.01745329252;
         long1*=d2r;
         lat1*=d2r;
         long2*=d2r;
         lat2*=d2r;
         dlat=0.5*(lat2-lat1);
         dlat=Math.sin(dlat);
         dlat=dlat*dlat;
         dlong=0.5*(long2-long1);
         dlong=Math.sin(dlong);
         dlong=dlong*dlong;
         res=dlat;
         res+=Math.cos(lat1)*Math.cos(lat2)*dlong;
         res=Math.sqrt(res);
         return 12745.59*Math.asin(res);
}

function getEFForClass(atype){
	fac = kgCO2PerPassengerKm;
	try {
        if(passengerClass=='first' && atype=='short haul'){
           passengerClass="business";//no first class
        }
	fac = dataFinder.getDataItemValue('transport/plane/generic/passengerclass', 'type='+atype+',passengerClass='+passengerClass,'kgCO2PerPassengerKm');
	}
	catch(err){}//do nothing

        if(fac<=0){//i.e. auto
fac=dataFinder.getDataItemValue('transport/plane/generic', 'type='+atype+',size=-','kgCO2PerPassengerKm');
        }
	return fac;
}

try {
  var i1=IATACode1;
  var i2=IATACode2;
  long1 = dataFinder.getDataItemValue('transport/plane/generic/airports/codes','IATACode='+i1, 'longitude');
  lat1 = dataFinder.getDataItemValue('transport/plane/generic/airports/codes','IATACode='+i1, 'latitude');
  long2 = dataFinder.getDataItemValue('transport/plane/generic/airports/codes','IATACode='+i2, 'longitude');
  lat2 = dataFinder.getDataItemValue('transport/plane/generic/airports/codes','IATACode='+i2, 'latitude');
}
catch(err){
  //do nothing
}

try {
  var n=numberOfPassengers;
}
catch(err){
  numberOfPassengers=1;
}

if(long1>=-180){
  dist=1.09*greatCircle();
  atype=type;
  if(type=='auto'){
     if(dist<1200.){
       atype='domestic';
     } else if(dist<3700.){
       atype='short haul';
     } else {
       atype='long haul';
     }

     if(size=='return'){
       dist=2.*dist;
     }
  }
  numberOfPassengers * dist * getEFForClass(atype);
} else if (kgCO2PerPassengerKm != 0) {
  (numberOfPassengers * distanceKmPerYear * getEFForClass(type)) / 12;
} else if (kgCO2PerPassengerJourney != 0) {
     (numberOfPassengers * journeysPerYear * kgCO2PerPassengerJourney) / 12;
} else {
  0;
}