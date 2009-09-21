require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe Filter do

  describe "aggregate by" do
    
    before(:each) do
      @filter = Filter.new
    end

    it "should collapse multiple entries into a single val" do
      data = [3, 3, 4].map do |i|
        Entry.stock :data_center_id => i, :watts => i, :count => (i*2), :co2 => (i*3)
      end
      data = @filter.aggregate_by :data_center_id, data

      data.size.should == 2
      dc3 = data.find { |e| e.data_center_id == 3 }
      dc3.watts.should == 6
      dc3.count.should == 12
      dc3.co2.should == 18
      data.find { |e| e.data_center_id == 4 }.watts.should == 4
    end

  end

end