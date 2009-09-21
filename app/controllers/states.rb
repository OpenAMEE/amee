class States < Application

  def index
    us_data = DataStore.view_by :geocode_country, "US" 
    
    data = Filter.new.aggregate_by :geocode_state, us_data
    @us_data = OutputFormatter.new.to_gdt data, [:geocode_state, :count, :watts, :co2, :emissions_factors]
    
    data = Filter.new.aggregate_by :city, us_data
    @us_map_data = OutputFormatter.new.to_gdt data, [:lat, :lng, :co2, :city]
    
    render
  end
  
  def show id
    @state_name = Codes.us_state_to_name id
    data = DataStore.view_by :geocode_state, id
    if data.empty?
      @table_data, @timeline_data = "{}", "{}"
    else
      @table_data = OutputFormatter.new.to_gdt data, (Entry.display_fields - [:geocode_state])
    
      @timeline_data = Filter.new.aggregate_by :time, data
      @timeline_data = @timeline_data.sort { |a,b| a.time <=> b.time }
      @timeline_data = TimelineMetadata.merge @timeline_data
      @timeline_data = OutputFormatter.new.to_gdt @timeline_data, [:time, :co2, :co2_title, :watts, :watts_title]
    end
    
    render
  end
  
end
