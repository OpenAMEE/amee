require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe DataLoader do
  
  before(:each) do
    @loader = DataLoader.new nil, nil
  end

  it "should not extract the city from non US entries" do
    e = Entry.create :data_center_location => "Odaiba data center", :geocode_country => "JP"
    @loader.extract_city(e).should be_nil
  end

  it "should not extract the city from US entries" do
    e = Entry.create :data_center_location => "Ashburn, VA (Building F - DC2)", :geocode_country => "US"
    @loader.extract_city(e).should == "Ashburn"
    
    e = Entry.create :data_center_location => "Dallas, TX", :geocode_country => "US"
    @loader.extract_city(e).should == "Dallas"

    e = Entry.create :data_center_location => "Los Angeles, CA/1 Wilshire", :geocode_country => "US"
    @loader.extract_city(e).should == "Los Angeles"

    # Really? Test with map and confirm
    e = Entry.create :data_center_location => "Seattle - Westin", :geocode_country => "US"
    @loader.extract_city(e).should == "Seattle - Westin"

    e = Entry.create :data_center_location => "New York, New York", :geocode_country => "US"
    @loader.extract_city(e).should == "New York"
  end

end