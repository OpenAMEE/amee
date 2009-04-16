kgCO2PerLitre = dataFinder.getDataItemValue('home/water', 'type=cold', 'kgCO2PerLitre');

try {
  var upd = usesPerDay;
  litresPerDay=litresPerUse*usesPerDay;
}
catch(err){}

try {
  var mpd = minutesPerDay;
  litresPerDay=litresPerMinute*minutesPerDay;
}
catch(err){}

//if neither uses or minutes given, then data item value
//for litresPerDay which gives typical usage
30.42 * litresPerDay * kgCO2PerLitre;

