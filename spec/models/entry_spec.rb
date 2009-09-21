require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe Entry do

  it "watts should be a number" do
    e = Entry.new
    e.watts = "56"
    e.watts.should == 56
  end

end