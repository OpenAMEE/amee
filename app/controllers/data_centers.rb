class DataCenters < Application

  def index
    data = DataStore.data
    
    dc_data = Filter.new.aggregate_by :data_center_id, data
    dc_data = dc_data.sort { |a,b| b.co2 <=> a.co2 }
    
    fmtr = OutputFormatter.new
    @data = fmtr.to_gdt dc_data, [["data_center_id", "data_center_location"], "co2", "watts", "count"]
    render    
  end
  
  def show id
    data = DataStore.view_by :data_center_id, id
    @data_center = data[0]
    
    @table_data = OutputFormatter.new.to_gdt data, (Entry.display_fields - [:data_center_location])
    
    @timeline_data = Filter.new.aggregate_by :time, data
    @timeline_data = @timeline_data.sort { |a,b| a.time <=> b.time }
    @timeline_data = TimelineMetadata.merge @timeline_data
    @timeline_data = OutputFormatter.new.to_gdt @timeline_data, [:time, :co2, :co2_title, :watts, :watts_title]
    render
  end
  
end
