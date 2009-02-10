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

if(long1>=-180)
  1.09*greatCircle()* kgCO2PerPassengerKm;
else if (kgCO2PerPassengerKm != 0) {
  (distance * kgCO2PerPassengerKm) / 12;
} else if (kgCO2PerPassengerJourney != 0) {
     (journeys * kgCO2PerPassengerJourney) / 12;
} else {
  0;
}
