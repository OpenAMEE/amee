class NetworkServices < Application

  def index
    data = Filter.new.aggregate_by :region_network, DataStore.data
    @data = OutputFormatter.new.to_gdt data, [:region_network, :count, :watts, :co2, :emissions_factors]

    render
  end

  def show id
    @service_name = URI.unescape id
    data = DataStore.view_by :region_network, @service_name
    if data.empty?
      @table_data, @timeline_data = "{}", "{}"
    else
      @table_data = OutputFormatter.new.to_gdt data, (Entry.display_fields - [:region_network])

      @timeline_data = Filter.new.aggregate_by :time, data
      @timeline_data = @timeline_data.sort { |a,b| a.time <=> b.time }
      @timeline_data = TimelineMetadata.merge @timeline_data
      @timeline_data = OutputFormatter.new.to_gdt @timeline_data, [:time, :co2, :co2_title, :watts, :watts_title]
    end

    render
  end

end
