class Entry
    
  attr_accessor :data_center_id, :data_center_location, :geocode_country, 
    :geocode_state, :city, :region_network, :product_id, :product_line, :product_name,
    :product_server_type, :count, :watts, :time, :co2, :emissions_factors, :lat, :lng    
    
  # qnd hack for adding timeline annotations
  attr_accessor :co2_title, :watts_title
  
  # yah (yet another hack)
  attr_accessor :intensity
  
  # Field name changes
  # product => asset 
  # region_network / network service => product
    
  # Generic field names
  # - original
  # facility_name,geocode_country,geocode_state,product,asset,asset_type,count,watts
  #
  # - updated
  # data_center_location,geocode_country,geocode_state,region_network,product_name,product_server_type,count,watts
  
  # SDN field names
  # - original
  # data_center_location,geocode_country,geocode_state,region_network,product_line,product_name,product_server_type,count,watts
  #
  # - without id
  # data_center_id,data_center_location,geocode_country,geocode_state,region_network,product_id,product_line,product_name,product_server_type,count,watts
      
  def self.fields
    methods = Entry.new.methods.sort - Object.new.methods.sort
    methods.reject { |n| n =~ /=$/ }
  end
  
  def self.display_fields
    [:time, :data_center_location, :region_network, :product_name, 
      :count, :watts, :co2, :emissions_factors]
  end
  
  def self.create params = nil
    e = new
    params.each { |k, v| e.send "#{k}=", v }
    e
  end    
    
  def inspect
    s = "#<#{self.class}:#{self.object_id}"
    self.class.fields.each do |name|
      val = instance_variable_get("@#{name}".to_sym)
      s << ", #{name}: #{val.inspect}" if val
    end
    s << ">"
  end
  
  alias_method :to_s, :inspect
  
end
