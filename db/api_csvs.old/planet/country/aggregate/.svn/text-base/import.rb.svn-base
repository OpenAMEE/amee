require 'rubygems'
require 'csv'
require 'pp'
require 'set'

def csv_to_hash(filename,keycol)
  csv=CSV.read(filename)
  hash={}
  csv.each do |line|
    hash[line[keycol]]=line
  end
  hash
end



def keycum(hashes)
  keyset=Set.new
  hashes.each do |hash|
    keyset+=hash.keys
  end
  keyset
end

def fix_commas(hash,col)
# fix up the silly , based string in the cumulative data
  hash.each_key do |key|
    data=hash[key]
    data[col]=CSV::Cell.new(data[col].gsub(/,/,"").to_f.to_s) if data[col]
  end
end

change=csv_to_hash('change.csv',1)
cumulative=csv_to_hash('cumulative.csv',1)
historic=csv_to_hash('historic.csv',1)
percapita=csv_to_hash('percapita.csv',1)
rank=csv_to_hash('rank.csv',2)

hashes=[change,cumulative,historic,percapita,rank]
neededcols=[2,2,28,2,0]
headers=['country','change','cumulative','annual2005','perCapita','rank', 'units', 'source']

fix_commas(cumulative,2)
fix_commas(historic,28)

allkeys=keycum(hashes)


CSV.open('data.csv','w') do |writer|
  writer << headers
  
  allkeys.each do |country|
    resultline=[]
    resultline << country
    (0..hashes.length-1).each do |index|
      hash=hashes[index]
      neededcol=neededcols[index]
      row=hash[country]
      if row && row[neededcol]
        resultline << row[neededcol]
      else
        resultline << '-'
      end

    end
    resultline << 'Dummy' << 'http://www.eia.doe.gov/environment.html'
    writer << resultline
  end
  
end
