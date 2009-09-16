class DataStore

  @data = []
  @index = Ferret::Index::Index.new
  @uploads = []

  def self.reset
    @data = []
    @index = Ferret::Index::Index.new    
    @uploads = []
  end

  def self.add obj
    if new_key? :data_center_location, obj.data_center_location
      @index << {:url => "/facilities/#{obj.data_center_id}", :content => obj.data_center_location}
    end
    
    if obj.geocode_country == "US" && new_key?(:geocode_state, obj.geocode_state)
      @index << {:url => "/states/show/#{obj.geocode_state}", :content => Codes.us_state_to_name(obj.geocode_state)}
    end
    
    if new_key?(:geocode_country, obj.geocode_country)
      @index << {:url => "/countries/show/#{obj.geocode_country}", :content => Codes.country_code_to_name(obj.geocode_country)}
    end

    if new_key?(:region_network, obj.region_network) && !obj.region_network.blank?
      @index << {:url => "/products/#{URI.encode obj.region_network}", :content => obj.region_network}
    end
    
    if new_key?(:product_name, obj.product_name)
      @index << {:url => "/assets/#{obj.product_id}", :content => obj.product_name}
    end    
    
    @data << obj
  end
  
  def self.data
    @data
  end
  
  def self.index
    @index
  end

  def self.data= d
    @data = d
  end
  
  def self.view_by att, id
    data.select { |e| e.send(att) == id }
  end
  
  def self.search qs
    res = []
    @index.search_each("content:#{qs}*") do |id, score|
      res << @index[id].load 
    end
    res
  end
  
  def self.match qs
    res = []
    @index.search_each(%Q{content:"#{qs}"}) do |id, score|
      res << @index[id].load 
    end
    !res.empty?
  end
  
  def self.new_key? att, val
    !@data.find { |e| e.send(att) == val }
  end
  
  def self.uploads
    @uploads
  end
  
end
