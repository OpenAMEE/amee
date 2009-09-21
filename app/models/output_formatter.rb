class OutputFormatter
  
  #
  # Output to Google Data Table
  #
  def to_gdt arr, fields = arr[0].class.fields
    js = ""
    add_cols js, fields
    add_rows js, fields, arr
  end
  
  def add_cols js, fields
    cols = []
    fields.each do |f|
      f = f[0] if f.is_a? Array
      
      t = types[f] || "string"      
      f = f.to_s.split("_").map { |s| s.capitalize }.reject { |s| s == "Geocode" }.join " " 
      
      f = "tCO2" if f == "Co2"
      f = "Power (Watts)" if f == "Watts"
      f = "Emissions Factors (kgCO2/kWh)" if f == "Emissions Factors"
      f = "Product" if f == "Region Network"
      f = "Asset" if f == "Product Name"
      f = "Asset" if f == "Product Id"
      f = "Facility Name" if f == "Data Center Location"
      f = "Facility Name" if f == "Data Center Id"
      f = "Date" if f == "Time"
      
      cols << "\t{type: '#{t}', label: '#{f}'}"
    end
    js << "{\ncols: [\n#{cols.join ",\n"}\n], \n"
  end
  
  def add_rows js, fields, arr
    rows = []
    arr.each do |data_point|
      cells = []
      fields.each do |f|
        if f.is_a? Array
          val = data_point.send f[0]
          label = f[1].is_a?(Proc) ? f[1].call(data_point) : data_point.send(f[1])
          val = convert_val val
          cells << "{v: #{val}, f: '#{label}'}"
        else
          val = f.is_a?(Proc) ? f.call(data_point) : data_point.send(f)
          val = convert_val val
          if val.is_a? Numeric
            cells << "{v: #{val}, f: #{format_num(val)}}"
          else
            cells << "{v: #{val}}"
          end
        end
      end
      rows << "\t{c:[#{cells.join ","}]}"
    end
    
    js << "rows: [\n#{rows.join ",\n"}\n]\n}\n"
  end
  
  def type field, val
    # extract it from this
  end
  
  def types
    {
      "time" => "date",
      "count" => "number",
      "watts" => "number",
      "co2" => "number",
      "emissions_factors" => "number",
      "lat" => "number",
      "lng" => "number",
      "intensity" => "number",
    }.to_mash
  end  
  
  def convert_val val
    if val.is_a? String
      %Q{"#{val}"}
    elsif val.is_a? Time 
      ta = val.to_a
      # Ruby : Jan == Month 1, JavaScript : Jan == Month 0
      "new Date(#{ta[5]}, #{ta[4] - 1}, #{ta[3]})"
    elsif val.nil?
      "null"
    else
      val
    end
  end
  
  def format_num val
    if val < 1
      %Q{"#{"%0.3f" % val}"}
    else
      %Q{"#{commas val}"}
    end
  end
  
  def commas val
    str = val.floor.to_s.reverse
    str.gsub!(/([0-9]{3})/, "\\1,")
    str.gsub(/,$/, "").reverse
  end
  
  #
  # To jQuery autocomplete
  #
  def to_jq_ac arr
    arr.map { |i| i.to_json }.join "\n"  
  end
  
  
end
