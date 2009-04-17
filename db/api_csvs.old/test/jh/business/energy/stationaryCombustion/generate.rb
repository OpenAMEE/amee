require '/home/jamespjh/devel/amee/svn.amee.com/internal/projects/apitools/ameem/importHelpers'
require 'pp'
require 'set'

include AMEEImportHelpers
Stationary="StationaryCombustionWorksheet"
StationaryCO2=Stationary+"CO2EFs.csv"
StationaryCH4=Stationary+"CH4EFs.csv"
StationaryN2O=Stationary+"N20EFs.csv"
Iron="IronAndSteel"
IronCO2=Iron+"CO2EFs.csv"
IronNonCO2=Iron+"NonCO2EFs.csv"
PulpAndPaperEFs="pulpAndPaperEFs.csv"
PulpAndPaperHVs="pulpAndPaperHVs.csv"
General=Stationary+"General.csv"
Cement="cementSustainabilityEFs.csv"

LHV="LHV"
HHV="HHV"
NRG="NRG"
CO2="CO2"
CO2e="CO2e"
CH4="CH4"
N2O="N2O"
EF="EF"
Content="Content"
Mass="Mass"
Vol="Vol"
Density="density"
Fuel="fuel"
Basis="Basis"
Context="context"
Sectors=['Energy','Manufacturing','Construction','Commercial','Institutional',
         'Residential','Agriculture','Forestry','Fisheries']



def importSectorTables(sheet,gas)

  numsectors=Sectors.length

  masterTablelefts=['B','D','F','H','J','L','N','P','R']
  masterTablerights=['C','E','G','I','K','M','O','Q','S']

  sectorTableStarts=[71,87,102,117,132,147,162,177,192]
  sectorTableEnds=sectorTableStarts.map {|x| x+10}

  gasTableStarts=[210,217,224,231,238,245,252,259,266]
  gasTableEnds=gasTableStarts.map {|x| x+2}

  keys=[Fuel,EF+LHV+NRG+gas,LHV,EF+LHV+Mass+gas,
        Density,EF+LHV+Density+gas]

  result={}
  import_table_group(sheet,masterTablelefts,[6]*numsectors,
                     masterTablelefts,[59]*numsectors,
                     masterTablerights,
                     [Fuel,EF+LHV+NRG+gas],Sectors,result)

  import_table_group(sheet,['B']*numsectors,sectorTableStarts.map{|x| x-1},
                     ['B']*numsectors,
                     sectorTableEnds.map{|x| x-1},['G']*numsectors,
                     keys,Sectors,
                     result
                     )

  # the liquid fuels have densities in litres not m^3,
  # so we need tom map through.
  result.values.each do |subtable|
    mapColumn(subtable,Density) {|x| x.to_f*1000}
    mapColumn(subtable,EF+LHV+Density+gas) {|x| x.to_f*1000}
  end

  import_table_group(sheet,['B']*numsectors,gasTableStarts.map{|x| x-1},
                     ['B']*numsectors,
                     gasTableEnds.map{|x| x-1},['G']*numsectors,
                     keys,Sectors,
                     result
                     )
  result
end

stationaryCO2=
  csv_subtable_to_hash(
                       StationaryCO2,
                       'C',
                       6,'C',
                       59,'F',
                       [Fuel,'Biomass',EF+LHV+NRG+CO2,LHV])

stationaryHHVConversion=
  csv_subtable_to_hash(
                       General,'B',
                       202,'B',
                       253,'C',
                       [Fuel,'HHVConversion']
                       )


stationaryCH4=importSectorTables(StationaryCH4,CH4)
stationaryN2O=importSectorTables(StationaryN2O,N2O)

stationaryCO2['Lubricants'][EF+LHV+NRG+CO2].to_f==73300 or raise
stationaryCH4['Fisheries']['Crude oil'][EF+LHV+Mass+CH4].to_f==423 or raise
stationaryCH4['Fisheries']['Landfill gas'][EF+LHV+Mass+CH4].to_f==252 or raise
stationaryN2O['Fisheries']['Ethane'][EF+LHV+NRG+N2O].to_f==0.1 or raise

ironCO2=csv_subtable_to_hash(IronCO2,'C',9,'C',61,'H',
                             [Fuel,Basis,EF+LHV+NRG+CO2,
                              EF+HHV+NRG+CO2,HHV,LHV])
# The columns labelled EF were actually carbon content - map them through by
# stoichiometry 44/12

mapColumn(ironCO2,EF+LHV+NRG+CO2) {|x| x.to_f*44*1000/12}
mapColumn(ironCO2,EF+HHV+NRG+CO2) {|x| x.to_f*44*1000/12}
mapColumn(ironCO2,HHV) {|x| x.to_f*2.326} # from BTU per lb

ironCO2.has_key?('Crude oil') or raise "No crude oil in iron"
ironCO2.has_key?('Municipal wastes (biomass fraction)') or raise "No biomass fraction"
ironNonCO2=csv_subtable_to_hash(IronNonCO2,'C',9,'C',61,'K',
                                [Fuel,
                                 EF+LHV+NRG+CH4,
                                 EF+LHV+NRG+N2O,
                                 EF+HHV+Mass+CH4,
                                 EF+HHV+Mass+N2O,
                                 EF+HHV+NRG+CH4,
                                 EF+HHV+NRG+N2O,
                                 EF+LHV+Mass+CH4,
                                 EF+LHV+Mass+N2O])
# note some of the columns in this table have been labelled the wrong way round
# the LHV basis mass factor is labelled as HHV and vice versa
# (Note the HHV is the bigger Energy/Mass value.)


mapColumn(ironNonCO2,EF+LHV+Mass+N2O) {|x| x.to_f*1000}
mapColumn(ironNonCO2,EF+LHV+Mass+CH4) {|x| x.to_f*1000}

def renameFuels(table)
  replaceKeys(table,[
                     ["Municipal wastes (biomass fraction)",
                      "Municipal wastes (Biomass fraction)"],
                     ["Municipal wastes (non-biomass fraction)",
                      "Municipal waste (Non biomass fraction)"],
                     ["Aviation Gasoline",
                      "Aviation gasoline"],
                     ["Motor Gasoline",
                      "Motor gasoline"],
                     ["Refinery Gas",
                      "Refinery gas"],
                     ["Gas/.Diesel oil",
                      "Gas/Diesel oil"],
                     ["Residual Fuel oil",
                      "Residual fuel oil"],
                     ["White Spirit & SBP",
                      "White Spirit/SBP"],
                     ["Jet Gasoline",
                      "Jet gasoline"],
                     ["Wood/Wood waste",
                      "Wood or Wood waste"],
                     ["Sub-bituminous coal",
                      "Sub bituminous coal"],
                     ["Other Kerosene",
                      "Other kerosene"],
                     ["Jet Kerosene",
                      "Jet kerosene"],
                     ["Natural Gas ",
                      "Natural gas"]])
end

renameFuels(ironCO2)
replaceKey(ironCO2,'Coke oven coke / lignite coke / coke breeze',
           'Coke oven coke')
renameFuels(ironNonCO2)
replaceKey(ironNonCO2,'Coke oven coke & lignite coke',
           'Coke oven coke')

# merge the manufacturing subtables into one table, checking for consistency where values
# have same identifier
# joining on the key
# 

manufacturingTable={}
safeMerge manufacturingTable,stationaryCO2,false

print "\nMerge N2O, manufacturing\n"
safeMerge manufacturingTable,stationaryN2O['Manufacturing'],true


print "\nMerge CH4, manufacturing\n"
safeMerge manufacturingTable,stationaryCH4['Manufacturing'],true

print "\nMerge CO2, iron\n"
safeMerge manufacturingTable,ironCO2,true

print "\nMerge nonCO2, iron\n"
safeMerge manufacturingTable,ironNonCO2,true

print "\nMerge HHV\n"
safeMerge manufacturingTable,stationaryHHVConversion,true
#manufacturingTable.values.each {|row|
#  p "Wrong HHV comparison #{row[Fuel]}: #{row[HHV].to_f/row["HHVConversion"].to_f/row[LHV].to_f}"
#}
# The above test is the one that suggests something is wrong with the BTU values in the iron data
# seems to have used the standard conversions and then another factor of 1.11.
# we do not trust this value
# but recover the GHG behaviour using the
# same 'rule of thumb' value they use.

manufacturingTable.values.each {|row|
  if row.has_key?(EF+LHV+Density+CH4)
    ratio=row[EF+LHV+Density+CH4].to_f/(row[EF+LHV+NRG+CH4].to_f*row[Density].to_f*row[LHV].to_f)
    close ratio,1e-9 or p "Out of sync CH4 vol val for #{row[Fuel]}: #{ratio}"
    ratio=row[EF+LHV+Mass+CH4].to_f/(row[EF+LHV+NRG+CH4].to_f*row[LHV].to_f)
    close ratio,1 or p "Out of sync CH4 mass val for #{row[Fuel]}: #{ratio}"
    ratio=row[EF+LHV+Density+N2O].to_f/(row[EF+LHV+NRG+N2O].to_f*row[Density].to_f*row[LHV].to_f)
    close ratio,1e-9 or p "Out of sync N2O vol val for #{row[Fuel]}: #{ratio}"
    ratio=row[EF+LHV+Mass+N2O].to_f/(row[EF+LHV+NRG+N2O].to_f*row[LHV].to_f)
    close ratio,1 or p "Out of sync nN2O Massval for #{row[Fuel]}: #{ratio}"
  end
}

# this generated no warnings, so we can ignore the additional data in the iron tables.
# now, merge all sector tables into a master table
# note the iron tables have HHV data, but it's 'rule of thumb' based and appears to just be a flat multiple of the LHV values.



masterTable={}
Sectors.each do |sector|
  stationaryCO2.keys.each do |fuel|
    masterTable[fuel+sector]=stationaryCO2[fuel].dup
    masterTable[fuel+sector].merge! stationaryN2O[sector][fuel]
    masterTable[fuel+sector].merge! stationaryCH4[sector][fuel]
    if !stationaryHHVConversion[fuel]
      p "No HHV Conversion for #{fuel}"
    else
      masterTable[fuel+sector].merge! stationaryHHVConversion[fuel].dup
    end
    masterTable[fuel+sector][Context]=sector
  end
end

# now, get equipment specific tables
# from Iron:

ironEquipmentSpecific=
  csv_subtable_to_hash(
                       IronNonCO2,['C','D'],71,'C',88,'H',
                       ['Technology','Configuration',
                        EF+LHV+NRG+CH4,
                        EF+LHV+NRG+N2O,
                        EF+HHV+NRG+CH4,
                        EF+HHV+NRG+N2O])

# and from pulp and paper:

ironEquipmentSpecific['Residual fuel oil/ Shale oil boilers'][EF+LHV+NRG+CH4].to_f==3 or raise 'Iron fuel oil wrong'
ironEquipmentSpecific['Wood/wood waste boilers'][EF+HHV+NRG+N2O].to_f==6.65 or raise 'Iron wood wrong'
ironEquipmentSpecific['Other bituminous/sub-bituminous fluidised bed combustorBubbling bed'][EF+LHV+NRG+N2O].to_f==61.0 or raise "Iron bubbling wrong: #{ironEquipmentSpecific['Other bituminous/sub-bituminous fluidised bed combustorBubbling bed'][EF+LHV+NRG+N2O]}"

paper=
  csv_subtable_to_hash(
                       PulpAndPaperEFs,'C',48,'C',93,'G',
                       [Fuel,EF+LHV+NRG+CO2,
                        EF+LHV+NRG+CH4,
                        EF+LHV+NRG+N2O,
                        EF+LHV+NRG+CO2e])
paper['Kerosene'][EF+LHV+NRG+CO2].to_f==71.9 or raise 'Paper kerosene wrong'
paper['Biogas used in boilers or kilns'][EF+LHV+NRG+CO2e].to_f==0.0675 or raise 'Paper biogas wrong'

mapColumn(paper,EF+LHV+NRG+CO2) {|x| x.to_f*1000}
mapColumn(paper,EF+LHV+NRG+CH4) {|x| x.to_f*1000}
mapColumn(paper,EF+LHV+NRG+N2O) {|x| x.to_f*1000}
mapColumn(paper,EF+LHV+NRG+CO2e) {|x| x.to_f*1000}

paperHVs=
  csv_subtable_to_hash(
                       PulpAndPaperHVs,'B',10,'B',29,'F',
                       [Fuel,HHV+Vol,
                        LHV+Vol,
                        'Carbon %',
                        Density])
paperHVs['Gasoline / petrol'][HHV+Vol].to_f==0.0362 or raise 'Paper petrol wrong'
paperHVs['Wood (0% H2O)**'][LHV+Vol]=='0.0200 GJ / kg' or raise "Pwood wrong: #{paperHVs['Wood (0% H2O)**'][LHV+Vol]}"
paperHVs['Distillate fuel oil No.2'][Density].to_f==0.845 or raise "Paper density wrong #{paperHVs['Distillate fuel oil No.2'][Density]}"

# rekey these tables into our preferred indices
# [[old key, [new key, new secondary]]]

ironEquipmentRekey=
  [
   ['Wood/wood waste boilers',
    ['Wood or Wood waste','Boilers']],
   ['Other bituminous spreader stokers',
    ['Other bituminous coal','Spreader stokers']],
   ['Gas/Diesel oil boilers',
    ['Gas/Diesel oil','Boilers']],
   ['Large stationary diesel oil engines >600hp (447kW)',
    ['Gas/Diesel oil','Large stationary diesel oil engines']],
   ['Other bituminous /Sub-bituminous underfeed stoker boilers',
    [['Other bituminous coal','Sub bituminous coal'],'Underfeed stoker boilers']],
   ['Other bituminous /Sub-bituminous overfeed stoker boilers',
    [['Other bituminous coal','Sub bituminous coal'],'Overfeed stoker boilers']],
   ['Natural gas-fired reciprocating engines4-Stroke lean burn',
    ['Natural gas','Reciprocating engine, 4-stroke lean burn']],
   ['Other bituminous/sub-bituminous fluidised bed combustorCirculating bed',
    [['Other bituminous coal','Sub bituminous coal'],'Fluidised circulating bed combustor']],
   ['Residual fuel oil/ Shale oil boilers',
    [['Residual fuel oil','Shale oil'],'boilers']],
   ['Other bituminous/sub-bituminous fluidised bed combustorBubbling bed',
    [['Other bituminous coal','Sub bituminous coal'],'Fluidised bubbling bed combustor']],
   ['Liquified Petroleum Gases (LPG) boilers',
    ['Liquified Petroleum Gases','Boilers']],
   ['Gas-fired gas turbines >3MW',
    ['Natural gas','Gas fired turbines >3MW']],
   ['Other bituminous/sub-bituminous pulverisedDry bottom, tangentially fired',
    [['Other bituminous coal','Sub bituminous coal'],'Pulverised dry bottom, tangentially fired']],
   ['Natural gas-fired reciprocating engines4-Stroke rich burn',
    ['Natural gas','Reciprocating engines, 4-stroke rich burn']],
   ['Boilers',
    ['Natural gas','Boilers']],
   ['Natural gas-fired reciprocating engines2-Stroke lean burn',
    ['Natural gas','Reciprocating engines, 2-stroke lean burn']],
   ['Other bituminous/sub-bituminous pulverisedDry bottom, wall fired',
    [['Other bituminous coal','Sub bituminous coal'],'Pulverised dry bottom, wall fired']],
   ['Other bituminous/sub-bituminous pulverisedWet bottom',
    [['Other bituminous coal','Sub bituminous coal'],'Pulverised wet bottom']]
  ]


newIronEquipmentSpecific={}

ironEquipmentRekey.each do |rekey|
  oldkey=rekey[0]
  newkeys=rekey[1][0]
  newkeys=[newkeys] if !(newkeys.class == Array)
  note=rekey[1][1]
  newkeys.each do |newkey|
    newIronEquipmentSpecific[newkey+note]=ironEquipmentSpecific[oldkey].dup
    newIronEquipmentSpecific[newkey+note][Context]=note
    newIronEquipmentSpecific[newkey+note][Fuel]=newkey
  end
end

ironEquipmentSpecific['Other bituminous/sub-bituminous fluidised bed combustorBubbling bed'][EF+LHV+NRG+N2O].to_f==61.0 or raise "Iron bubbling wrong: #{ironEquipmentSpecific['Other bituminous/sub-bituminous fluidised bed combustorBubbling bed'][EF+LHV+NRG+N2O]}"
newIronEquipmentSpecific['Other bituminous coalFluidised bubbling bed combustor'][EF+LHV+NRG+N2O].to_f==61.0 or raise "New Iron Bubbling wrong: #{masterTable['Other bituminous coalFluidised bubbling bed combustor'][EF+LHV+NRG+N2O]}"

# drop these additional equipment specific data into the master table
# they only have CH4,N2O data, so copy in the CO2 data
newIronEquipmentSpecific.values.each do |val|
  masterTable[val[Fuel]+val[Context]]=val.dup
  masterTable[val[Fuel]+val[Context]].merge! stationaryCO2[val[Fuel]]
end

masterTable['Other bituminous coalFluidised bubbling bed combustor'][EF+LHV+NRG+N2O].to_f==61.0 or raise "Bubbling wrong: #{masterTable['Other bituminous coalFluidised bubbling bed combustor'][EF+LHV+NRG+N2O]}"

# Now, the output table will be

# for each fuel, for each context, the values
# with '-' for n/a
# ignoring the 'rule of thumb' hhv values

columns=[Fuel,Context,Density,LHV,EF+LHV+NRG+CO2,EF+LHV+NRG+CH4,EF+LHV+NRG+N2O,'HHVConversion','units','source']



paperRekey=
  [
   ['Biogas used in boilers or kilns',
    ['Biogas',['Boilers','Kilns']]],
   ['Sub-bituminous coal - fluidized bed/Circulating or bubbling',
    ['Sub bituminous coal',
     ['Fluidised circulating bed combustor',
      'Fluidised bubbling bed combustor']]],
   ['Bituminous coal - fluidized bed/Circulating or bubbling',
    ['Other bituminous coal',['Fluidised circulating bed combustor','Fluidised bubbling bed combustor']]],
   ['Bituminous coal - Pulverized/Wet bottom',
    ['Other bituminous coal','Pulverised wet bottom']],
   ['Natural gas (dry) - boilers and IR dryers',
    ['Natural gas','Boilers']],
   ['Natural gas (dry) - Int. Comb. Engine/2 - cycle lean burn',
    ['Natural gas','Reciprocating engines, 2-stroke lean burn']],
   ['Natural gas (dry) - turbines > 3 MW',
    ['Natural gas','Gas fired turbines >3MW']],
   ['Distillate fuel /(No.1, No.2, No.4 fuel oil and diesel)/Calciners',
    ['Gas/Diesel oil','Calciners']],
   ['Distillate fuel /(No.1, No.2, No.4 fuel oil and diesel)/Lime kilns',
    ['Gas/Diesel oil','Lime kilns']],
   ['Bituminous coal/Overfeed stoker boiler',
    ['Other bituminous coal','Overfeed stoker boilers']],
   ['Natural gas (dry) - Int. Comb. Engine/4 - cycle rich burn',
    ['Natural gas','Reciprocating engines, 4-stroke rich burn']],
   ['Distillate fuel /(No.1, No.2, No.4 fuel oil and diesel)/Stationary sources except lime kilns and calciners',
    ['Gas/Diesel oil','Other']],
   ['Kerosene',
    ['Other kerosene','Manufacturing']],
   ['Waste Oil',
    ['Waste oils','Manufacturing']],
   ['Sub-bituminous coal/Underfeed stoker boiler',
    ['Sub bituminous coal','Underfeed stoker boilers']],
   ['Bituminous coal - Pulverized/Dry bottom, tangential firing',
    ['Other bituminous coal','Pulverised dry bottom, tangentially fired']],
   ['Bituminous coal/Underfeed stoker boiler',
    ['Other bituminous coal','Underfeed stoker boilers']],
   ['Gas coke',
    ['Gas coke','Manufacturing']],
   ['Lignite',
    ['Lignite','Manufacturing']],
   ['Natural gas (dry) - calciners',
    ['Natural gas','Calciners']],
   ['LPG',
    ['Liquified Petroleum Gases','Boilers']],
   ['Sub-bituminous coal/Overfeed stoker boiler',
    ['Sub bituminous coal','Overfeed stoker boilers']],
   ['Natural gas (dry) - lime kiln',
    ['Natural gas','Lime kilns']],
   ['Petroleum coke',
    ['Petroleum coke','Manufacturing']],
   ['Sub-bituminous coal - Pulverized/Dry bottom, wall fired',
    ['Sub bituminous coal','Pulverised dry bottom, wall fired']],
   ['Bituminous coal/Spreader stoker boiler',
    ['Other bituminous coal','Spreader stokers']],
   ['Natural gas (dry) - Int. Comb. Engine/4 - cycle lean burn',
    ['Natural gas','Reciprocating engine, 4-stroke lean burn']],
   ['Residual fuel oil (No.5, No.6 fuel oil)/Calciners',
    ['Residual fuel oil','Calciners']],
   ['Peat ',
    ['Peat','Manufacturing']],
   ['Residual fuel oil (No.5, No.6 fuel oil)/Lime kilns',
    ['Residual fuel oil','Lime kilns']],
   ['Residual fuel oil (No.5, No.6 fuel oil)',
    ['Residual fuel oil','Other']],
   ['Wood and wood waste',
    ['Wood or Wood waste','Pulp and paper']],
   ['Pulping liquors',
    ['Pulping liquors','Manufacturing']],
   ['Municipal solid waste (non biomass fraction)',
    ['Municipal waste (Non biomass fraction)','Manufacturing']],
   ['Tires and tire derived fuel',
    ['Tires and tire derived fuel','General']],
   ['Coal Tar',
    ['Coal tar','Manufacturing']],
   ['Lubricants',
    ['Lubricants','Manufacturing']],
   ['Bituminous coal - Pulverized/Dry bottom, wall fired',
    ['Other bituminous coal','Pulverised dry bottom, wall fired']],
   ['Anthracite',
    ['Anthracite','Manufacturing']],
   ['Coke oven',
    ['Coke oven coke','Manufacturing']]
  ]

newPaper={}

paperRekey.each do |rekey|
  oldkey=rekey[0]
  newkey=rekey[1][0]
  notes=rekey[1][1]
  notes=[notes] if !(notes.class == Array)
  notes.each do |note|
    newPaper[newkey+note]=paper[oldkey].dup
    newPaper[newkey+note][Context]=note
    newPaper[newkey+note][Fuel]=newkey
    newPaper[newkey+note][LHV]=stationaryCO2[newkey][LHV] if stationaryCO2[newkey]
    newPaper[newkey+note]['HHVConversion']=stationaryHHVConversion[newkey]['HHVConversion'] if stationaryHHVConversion[newkey]
    newPaper[newkey+note][Density]=manufacturingTable[newkey][Density] if manufacturingTable[newkey]
  end
end

safeMerge masterTable,newPaper,true
masterTable['Natural gasCalciners'][LHV].to_f==48 or raise "Wrong LHV for natural gas in calciners: #{masterTable['Natural gasCalciners'][LHV]}" 

# now try as far as possible to generate factors from the 'custom EFs' table in pulp and paper.

paperHVs.values.each do |row|
  # there are 3 classes of row
  # ones without a %carbon - we can't do anything with them
  next if row['Carbon %'].to_f==0.0 
  # ones with a density
  if row[Density]=~/./
    row[Density].gsub!(/liquid/,'') if row[Density]=~/liquid/
    if row[Density]=~/kg \/ m3/
      row[Density].gsub!(/kg \/ m3/,'') 
      row[LHV+Vol].gsub!(/GJ \/ m3/,'')
      row[HHV+Vol].gsub!(/GJ \/ m3/,'')
    else
      row[LHV+Vol]=row[LHV+Vol].to_f*1000
      row[HHV+Vol]=row[HHV+Vol].to_f*1000
      row[Density]=row[Density].to_f*1000 
    end
    row[LHV]=1000*row[LHV+Vol].to_f/row[Density].to_f
    row[HHV]=1000*row[HHV+Vol].to_f/row[Density].to_f
  else
    # ones with a mass HV
    row[LHV]=1000*row[LHV+Vol].gsub(/GJ \/ kg/,'').to_f
    row[HHV]=1000*row[LHV+Vol].gsub(/GJ \/ kg/,'').to_f
  end
  row[EF+LHV+NRG+CO2]=1000*1000*row['Carbon %'].to_f*44.0/(100*12.0*row[LHV].to_f)
  row['HHVConversion']=row[HHV].to_f/row[LHV].to_f
  row[EF+LHV+NRG+N2O]=0.0
  row[EF+LHV+NRG+CH4]=0.0
  row[Context]='General'
end


# The above additional factors duplicate, less reliably, data we already have in many cases
# so just insert the most interesting ones
['Butane','Propane','Distillate fuel oil No.1','Distillate fuel oil No.2',
 'Residual fuel oil No.4','Residual fuel oil No.5','Residual fuel oil No.6'].each do |fuel|
  masterTable[fuel+'General']=paperHVs[fuel].dup
end

cement= csv_subtable_to_hash(
                       Cement,'C',6,'C',30,'E',
                       [Fuel,EF+LHV+'a',
                        EF+LHV+'b'
                        ])

masterTable.keys.each do |row|
  masterTable[row]['source']="GHGP"
end

dumpTable(masterTable,columns,'data.csv')
