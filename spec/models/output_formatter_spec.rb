require File.join( File.dirname(__FILE__), '..', "spec_helper" )

describe OutputFormatter do

  it "should have specs" do
    exp = %Q<{"text":"Link A","url":"\/page1"}
{"text":"Link A","url":"\/page1"}>

    data = [ {:text => 'Link A', :url => '/page1'}, {:text => 'Link B', :url => '/page2'} ]
    res = OutputFormatter.new.to_jq_ac data
    res.should == exp
  end

end