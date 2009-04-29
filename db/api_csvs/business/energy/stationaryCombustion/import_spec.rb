require 'importHelpers'
require 'pp'
describe AMEEImportHelpers do
  include AMEEImportHelpers
  it 'should interpret spreadsheet columns ok' do
    unspreadsheet('A').should eql 0
    unspreadsheet('B').should eql 1
    unspreadsheet('b').should eql 1
    lambda{unspreadsheet('AA')}.should raise_error
    unspreadsheet('5').should be 5
    unspreadsheet(5).should be 5
  end
  it 'should build a table hash from a csv file when given coords' do
    headers=['Fuel',
             'LHVCH4','LHVN20','LHVCH4Mass','LHVN20Mass',
             'HHVCH4','HHVN20','HHVCH4Mass','HHVN20Mass']
    result=csv_subtable_to_hash('IronAndSteelNonCO2EFs.csv','c',10,
                                'c',31,'k',headers)
    result['Ethane']['LHVCH4'].to_f.should eql 1.0
  end
end

