class Index < Application

  before :ensure_data

  def index
    render
  end
  
  def table
    render
  end
  
  def all_data
    data = DataStore.data
    fmtr = OutputFormatter.new
    @data = fmtr.to_gdt data, Entry.display_fields
    
    render :tablev2
  end
  
  def tablev2
    data = DataStore.data
    fmtr = OutputFormatter.new
    @data = fmtr.to_gdt data, Entry.display_fields
    
    render
  end    
  
  def motion
    # optionally constrain data
    data = DataStore.data
    fields = ["data_center_id", "time", "watts"]
    fmtr = OutputFormatter.new
    @data = fmtr.to_gdt data, fields
    
    render
  end
  
  def dashboard_old
    f = Filter.new
    data = f.aggregate_by :data_center_id, DataStore.data
    data = f.presentation_filter data
    
    @data = OutputFormatter.new.to_gdt data, [["data_center_id", "data_center_location"], "co2"]
    render    
  end
  
  def product_aggregation
    f = Filter.new
    data = f.aggregate_by :product_id, DataStore.data
    data = f.presentation_filter data
    
    @data = OutputFormatter.new.to_gdt data, [["product_id", "product_name"], "co2"]
    render
  end
  
  def dashboard start_time = nil, end_time = nil
    provides :js
    
    raw_data = DataStore.data
    if start_time && end_time
      start_time, end_time = Time.at(start_time.to_i), Time.at(end_time.to_i)
      raw_data = raw_data.select { |e| e.time >= start_time && e.time <= end_time} 
    end
     
    f = Filter.new
    data = f.aggregate_by :data_center_id, raw_data
    data = f.presentation_filter data    
    trunc_name = lambda { |dp| dp.data_center_location.size < 19 ? dp.data_center_location : dp.data_center_location[0,18] + "..." }
    co2_rebased = lambda { |dp| dp.co2 / 1000.0 }
    @dc_data = OutputFormatter.new.to_gdt data, [["data_center_id", trunc_name], co2_rebased]
    
    f = Filter.new
    data = f.aggregate_by :product_id, raw_data
    data = f.presentation_filter data
    trunc_name = lambda { |dp| dp.product_name.size < 19 ? dp.product_name : dp.product_name[0,18] + "..." }
    co2_rebased = lambda { |dp| dp.co2 / 1000.0 }
    @prod_data = OutputFormatter.new.to_gdt data, [["product_id", trunc_name], co2_rebased]
    
    data = Filter.new.aggregate_by :geocode_country, raw_data
    @ww_map_data = OutputFormatter.new.to_gdt data, ["geocode_country", "co2"]
    
    data = raw_data.select { |e| e.geocode_country == "US" }
    data = Filter.new.aggregate_by :city, data
    @us_map_data = OutputFormatter.new.to_gdt data, [:lat, :lng, :co2, :city]
    
    sorted_by_date = raw_data.sort { |a, b| a.time <=> b.time }
    @min_date, @max_date = sorted_by_date[0].time, sorted_by_date[-1].time
        
    data_agg_by_time = Filter.new.aggregate_by :time, raw_data    
    @emissions_data = OutputFormatter.new.to_gdt data_agg_by_time, [:time, :co2]
    
    render    
  end
    
  def map
    data = Filter.new.aggregate_by :geocode_country, DataStore.data
    @data = OutputFormatter.new.to_gdt data, ["geocode_country", "co2"]
    render
  end
  
  def map_of_us
    data = DataStore.data.select { |e| e.geocode_country == "US" }
    data = Filter.new.aggregate_by :city, data
    @data = OutputFormatter.new.to_gdt data, ["city", "co2"]
    render    
  end
  
end
