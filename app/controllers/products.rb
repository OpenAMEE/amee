class Products < Application

  def index
    data = Filter.new.aggregate_by :product_id, DataStore.data
    data = data.sort { |a,b| b.co2 <=> a.co2 }

    fields = [[:product_id, :product_name], :count, :watts, :co2]
    @data = OutputFormatter.new.to_gdt data, fields
    render        
  end
    
  def show id
    data = DataStore.view_by :product_id, id
    if data.empty?
      @table_data, @timeline_data = "{}", "{}"
    else
      @product_name = data[0].product_name
      
      @table_data = OutputFormatter.new.to_gdt data, (Entry.display_fields - [:product_name])

      @timeline_data = Filter.new.aggregate_by :time, data
      @timeline_data = @timeline_data.sort { |a,b| a.time <=> b.time }
      @timeline_data = TimelineMetadata.merge @timeline_data
      @timeline_data = OutputFormatter.new.to_gdt @timeline_data, [:time, :co2, :co2_title, :watts, :watts_title]
    end

    render
  end    
  
end
