class Countries < Application

  def index
    data = Filter.new.aggregate_by :geocode_country, DataStore.data
    normalize_data data
    @ww_map_data = OutputFormatter.new.to_gdt data, ["geocode_country", "co2"]
    
    render
  end
  
  def show id
    @country_name = Codes.country_code_to_name id
    data = DataStore.view_by :geocode_country, id
    if data.empty?
      @table_data, @timeline_data = "{}", "{}"
    else
      @table_data = OutputFormatter.new.to_gdt data, (Entry.display_fields - [:geocode_country])
    
      @timeline_data = Filter.new.aggregate_by :time, data
      @timeline_data = @timeline_data.sort { |a,b| a.time <=> b.time }
      @timeline_data = TimelineMetadata.merge @timeline_data
      @timeline_data = OutputFormatter.new.to_gdt @timeline_data, [:time, :co2, :co2_title, :watts, :watts_title]
    end
    
    render
  end
  
  def normalize_data data
    data.sort! { |a,b| a.co2 <=> b.co2 }
    data.map! { |e| e.dup }
    data.each_with_index { |e, i| e.co2 = i }
  end
  
  
end
