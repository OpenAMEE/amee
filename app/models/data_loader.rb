class DataLoader
  
  def initialize ww_data, us_data
    @ww_data = ww_data
    @us_data = us_data
  end
  
  def load fn
    DataStore.uploads << fn
    
    time = extract_time fn
    rows = FasterCSV.read fn

    # potentially parameterize an entry class from the header
    headers = rows.shift

    add_data headers, rows, time
  end
  
  def add_data headers, rows, time
    rows.each do |cells|
      entry = Entry.new
      entry.time = time
  
      cells.each_index do |i|
        name = headers[i]
        val = cells[i]
        val = convert_val name, val
        entry.send("#{name}=", val)
      end
      
      entry.data_center_id = "FCN#{entry.data_center_location.hash}"
      entry.product_id = "AST#{entry.product_name.hash}"      
    
      entry.emissions_factors = emissions_factors entry
      entry.co2 = (entry.watts * entry.emissions_factors * (24 * 30 / 1000.00)).floor
      entry.city = extract_city entry
      if entry.city
        lng, lat = Codes.us_city_to_lng_lat entry.city
        entry.lat = lat
        entry.lng = lng
      end
      
      DataStore.add entry
    end
  end
  
  def extract_time s
    s =~ /\d{4}_\d{2}/
    Time.parse $&.sub("_", "-")
  end
  
  def convert_val name, val
    to_f = lambda { |s| s.to_f }
    lda = {
      "watts" => to_f,
      "count" => to_f,
    }[name]
    lda ? lda.call(val) : val
  end
  
  def extract_city entry
    val = entry.data_center_location
    val = val.split(",")[0]
    entry.geocode_country == "US" ? val : nil
  end
  
  def randomize_val val, r
    val.is_a?(Numeric) ? (val * (1 + r/10.0)) : val
  end
  
  def emissions_factors entry
    ef = if entry.geocode_country == "US"
      @us_data.find { |d| d["state"] == entry.geocode_state }["kgCO2PerkWh"]
    else  
      @ww_data.find { |d| d["country"] == entry.geocode_country }["kgCO2PerKWh"]
    end
    ef.to_f
  end
  
  def create_random_data    
    random_seeds = [
      ["2008/09/01", {:wf => 1.0, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2008/10/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.95 : 0.99}} ],
      ["2008/11/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2008/12/01", {:wf => 1.2, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/01/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/01/20", {:wf => 1.5, :eff => lambda { |e| e.geocode_state == "CA" ? 0.93 : 0.99}} ],
      ["2009/02/01", {:wf => 1.3, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/03/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.8 : 0.8}} ],
      ["2009/04/01", {:wf => 1.2, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/05/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/05/24", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/05/25", {:wf => 1.25, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/06/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/07/01", {:wf => 1.05, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
      ["2009/08/01", {:wf => 1.1, :eff => lambda { |e| e.geocode_state == "CA" ? 0.99 : 0.99}} ],
    ]
    
    random_data = []
    data = DataStore.data
    random_seeds.each do |seed|
      time = Time.parse seed[0]
      
      data.each do |entry|
        e = entry.dup
        e.time = time
        e.watts = (e.watts * seed[1][:wf]).floor
        e.emissions_factors *= seed[1][:eff].call(e)
        e.co2 = (e.watts * e.emissions_factors * (24 * 30 / 1000.00)).floor
        random_data << e
      end
    end
    
    DataStore.uploads << "Sample Data"
    
    DataStore.data = random_data
  end

end
