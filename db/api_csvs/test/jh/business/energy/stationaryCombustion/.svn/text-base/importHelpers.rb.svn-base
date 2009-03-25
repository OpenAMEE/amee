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
  
  def csv_subtable_to_hash(filename,keycols,top,left,bottom,right,headers)
    top=unspreadsheet(top)
    bottom=unspreadsheet(bottom)
    left=unspreadsheet(left)
    right=unspreadsheet(right)
    if keycols.class==Array
      keycols=keycols.map! {|x| unspreadsheet(x)}
    else
      keycols=unspreadsheet(keycols)
    end
    csv=CSV.read(filename)
    hash={}
    csv[top..bottom].each do |line|
      row={}
      if keycols.class==Array
        key=""
        keycols.each do |col|
          entry=line[col]
          key+=entry if entry
        end
      else
        key=line[keycols]
      end
      key.gsub!(/\n/,'/')
      (left..right).each do |index|
        row[headers[index-left]]=line[index] ? line[index].data : nil
      end
      hash[key]=row
    end
    hash
  end

  

  def import_table_group(sheet,indexcol,starts,lefts,ends,
                         rights,labels,sectors,result)
    # import several tables with the same columns
    (0..sectors.length-1).each do |index|
      table=csv_subtable_to_hash(sheet,indexcol[index],starts[index],lefts[index],ends[index],rights[index],labels)
      result[sectors[index]]={} if !result[sectors[index]]
      safeMerge result[sectors[index]],table,false
    end
  end

  def safeMerge(hash,table,verbose)
    # hash and table are hashes of hashes
    # where outer key is same
    # if inner key is different, add it
    # if inner key already in inner hash, check value is consistent
    table.keys.each do |key|
      if hash.has_key? key 
        table[key].keys.each do |inner_key|
          if hash[key].has_key? inner_key
            if !close(hash[key][inner_key].to_f, table[key][inner_key].to_f)
              print "Inconsistent values (overwriting): #{key}/#{inner_key} #{hash[key][inner_key]} , #{table[key][inner_key]}! \n" if verbose
              hash[key][inner_key]=table[key][inner_key]
            else
              # print "val ok"
            end
          else
            hash[key][inner_key]=table[key][inner_key]
          end
        end
      else 
        print "New key: '#{key}'\n" if verbose
        hash[key]=table[key].dup
      end
    end
  end

  def mapColumn(table,col)
    table.keys.each do |row|
     table[row][col]=yield(table[row][col])  if table[row][col] 
    end
  end

  def replaceKey(table,oldkey,newkey)
    table[newkey]=table.delete(oldkey) {|x| raise "Wrong key: #{x}"}
  end

  def replaceKeys(table,subs)
    subs.each do |sub|
      replaceKey(table,sub[0],sub[1])
    end
  end

  def close(val1,val2)
    # plan to implement based on significant figures
    return true if val1==val2 
    (2*(val1-val2)/(val1+val2)).abs<0.01
  end

  def dumpTable(table,keys,fname)
    writer = CSV.open(fname, 'w')
    writer << keys
    table.values.each do |row|
      row.default='-1.0'
      texts=row.values_at(*keys)
      writer << texts
    end
    writer.close

  end

end
