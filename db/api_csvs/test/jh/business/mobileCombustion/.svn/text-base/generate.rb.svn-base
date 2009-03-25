require '/home/jamespjh/devel/amee/svn.amee.com/internal/projects/apitools/ameem/importHelpers'
require 'pp'
require 'set'
include AMEEImportHelpers
Distance="businessTravelDistance.csv"
Fuels="businessTravelFuels.csv"
Employee="employee.csv"
Paper="paper.csv"
EF="EF"
Volume="Volume"
Mass="Mass"
NRG="NRG"
Fuel="Fuel"

liquid_fuel_eia=
  csv_subtable_to_hash(
                       Fuels,'C',
                       7,'C',
                       15,'F',
                       [Fuel,'Imp1','Imp2',EF+Volume]
                       )

liquid_fuel_defra=
  csv_subtable_to_hash(Fuels,'C',
                       22,'C',
                       27,'D',
                       [Fuel,EF+Volume])


# units of vol here are 1000 ft^3
# units of energy are therm. FFS.
gas_fuel_eia1=
  csv_subtable_to_hash(Fuels,'C',
                       34,'C',
                       36,'F',
                       [Fuel,'Imp1',EF+Volume,EF+NRG])

gas_fuel_eia2=
  csv_subtable_to_hash(Fuels,'C',
                       40,'C',
                       40,'E',
                       [Fuel,EF+NRG,EF+Volume])

gas_fuel_defra=
  csv_subtable_to_hash(Fuels,'C',
                       45,'C',
                       45,'E',
                       [Fuel,EF+NRG,EF+Volume])
coal_defra=
  csv_subtable_to_hash(Fuels,'C',
                       62,'C',
                       63,'D',
                       [Fuel,EF+Mass])

coal_eia=
  csv_subtable_to_hash(Fuels,'C',
                       52,'C',
                       55,'G',
                       [Fuel,'Imp1','Imp2','Imp3',EF+Mass])

