require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe DataStore do
  
  before(:each) do
    DataStore.reset
  end

  it "entries should be stored" do
    e = Entry.create :data_center_location => "foo"
    DataStore.add e
    DataStore.data.should == [e]
  end
  
  it "entries should be searchable by exact match" do
    e = Entry.create :data_center_location => "Los Angeles, CA/1 Wilshire"
    DataStore.add e
    res = DataStore.search "Los Angeles, CA/1 Wilshire"
    res.size.should == 1
    res[0][:content].should == "Los Angeles, CA/1 Wilshire"
  end
  
  it "entries should be searchable by partial match" do
    e = Entry.create :data_center_location => "Los Angeles, CA/1 Wilshire"
    DataStore.add e
    
    DataStore.search("ang")[0][:content].should == "Los Angeles, CA/1 Wilshire"
    DataStore.search("los")[0][:content].should == "Los Angeles, CA/1 Wilshire"
    DataStore.search("wil")[0][:content].should == "Los Angeles, CA/1 Wilshire"
  end
  
  describe "entries should be searchable by" do
    it "data center location" do
      e = Entry.create :data_center_location => "Los Angeles, CA/1 Wilshire"
      DataStore.add e
      DataStore.search("ang")[0][:content].should == "Los Angeles, CA/1 Wilshire"
    end
    
    it "state" do      
      e = Entry.create :geocode_country => "US", :geocode_state => "CA"
      DataStore.add e
      DataStore.search("cal")[0][:content].should == "California"      
    end
    
    it "country" do      
      e = Entry.create :geocode_country => "US"
      DataStore.add e
      DataStore.search("u")[0][:content].should == "United States"      
    end
    
    it "region network" do
      e = Entry.create :region_network => "FreeFlow"
      DataStore.add e
      DataStore.search("free")[0][:content].should == "FreeFlow"
    end
    
    it "product name" do      
      e = Entry.create :product_name => "ZT Group 2x8-G4 Opteron Server"
      DataStore.add e
      DataStore.search("zt")[0][:content].should == "ZT Group 2x8-G4 Opteron Server"
    end
    
  end
  
  describe "search" do
    it "should not store duplicates" do
      e = Entry.create :data_center_location => "Los Angeles, CA/1 Wilshire"
      DataStore.add e
      DataStore.add e
      res = DataStore.search "Los Angeles, CA/1 Wilshire"
      res.size.should == 1
    end
  end
  
end