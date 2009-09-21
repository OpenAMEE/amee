require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe TimelineMetadata do

  describe "merge" do
    
    before(:each) do
      @e1 = Entry.create :time => Time.parse("2009/04/01"), :watts => 5, :co2 => 12 
      @e2 = Entry.create :time => Time.parse("2009/06/20"), :watts => 11, :co2 => 18 
      
      @md1 = ["2009/03/01", {}]
      @md2 = ["2009/05/01", {:watts => "foo", :co2 => "bar"}]
      @md3 = ["2009/07/01", {}]
    end
    
    it "should not modify its arg" do
      src = [@e1, @e2]
      merged = TimelineMetadata.merge src, [@md1, @md2, @md3]
      src.size.should == 2
    end
    
    it "should not merge when metadata preceds data" do
      data = TimelineMetadata.merge [@e1], [@md1]
      data.should == [@e1]
    end
    
    it "should not merge when metadata follows data" do
      data = TimelineMetadata.merge [@e2], [@md3]
      data.should == [@e2]
    end
    
    it "should use the average when metadata is bordered by data" do
      data = TimelineMetadata.merge [@e1, @e2], [@md1, @md2, @md3]
      data.size.should == 3
      
      data[1].watts.should == 8
      data[1].co2.should == 15
      data[1].watts_title.should == "foo"
      data[1].co2_title.should == "bar"
    end
    
  end

end