require 'importHelpers'
require 'pp'
include AMEEImportHelpers
Stationary="StationaryCombustionWorksheet"
StationaryCO2=Stationary+"CO2EFs.csv"
StationaryCH4=Stationary+"CH4EFs.csv"
LHV="LHV"
NRG="NRG"
CO2="CO2"
CH4="CH4"
EF="EF"
Mass="Mass"
Density="Density"

stationaryCO2Main=
  csv_subtable_to_hash(
                       StationaryCO2,
                       'C',
                       7,'C',
                       60,'F',
                       ['Fuel','Biomass',EF+LHV+NRG+CO2,LHV+NRG])
stationaryCO2Main['Lubricants'][EF+LHV+NRG+CO2].to_f==73300 or raise

sectors=['Energy','Manufacturing','Construction','Commercial','Institutional',
         'Residential','Agriculture','Forestry','Fisheries']
sectorTableStarts=[71,87,102,117,132,147,162,177,192]
sectorTableEnds=[81,97,112,127,142,157,172,187,202]

stationaryCH4Main={}
(0..sectors.length-1).each do |index|
  p sectors[index]
  table=csv_subtable_to_hash(StationaryCH4,'B',sectorTableStarts[index]-1,'B',
                             sectorTableEnds[index]-1,'G',
                             ['Fuel',EF+LHV+NRG+CH4,LHV,EF+LHV+Mass+CH4,
                             Density,EF+LHV+Density+CH4]
                             )
  stationaryCH4Main[sectors[index]]=table
end

stationaryCH4Main['Fisheries']['Crude oil'][EF+LHV+Mass+CH4].to_f==423 or raise
