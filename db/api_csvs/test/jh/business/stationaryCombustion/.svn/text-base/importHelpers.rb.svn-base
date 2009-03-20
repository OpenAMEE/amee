require 'rubygems'
require 'csv'
module AMEEImportHelpers

  def unspreadsheet(arg)
    if arg=~/[a-zA-Z]+/
      arg.downcase!
      if arg.length==1
        return arg[0].-'a'[0] 
      else
        raise 'Not implemented double letters'
      end
    end
    return arg.to_i if arg.class==String
    arg
  end
  
  def csv_subtable_to_hash(filename,keycol,top,left,bottom,right,headers)
    top=unspreadsheet(top)
    bottom=unspreadsheet(bottom)
    left=unspreadsheet(left)
    right=unspreadsheet(right)
    keycol=unspreadsheet(keycol)
    csv=CSV.read(filename)
    hash={}
    csv[top..bottom].each do |line|
      row={}
      key=line[keycol]
      (left..right).each do |index|
        row[headers[index-left]]=line[index]
      end
      hash[key]=row
    end
    hash
  end

end
