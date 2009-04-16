AMEE_SVN_ROOT="/home/jamespjh/Private/amee/svn.amee.com"
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon by energy" do
    check_calculation(folder,
                      "fuel=#{CGI.escape 'Gasoline / petrol'}&version=#{CGI.escape 'IPCC 99'}",
                      2.0*69.25,
                      "NRG" => 2.0)
  end
  it "should give the right amount of carbon by volume" do
    check_calculation(folder,
                      "fuel=#{CGI.escape 'Gasoline / petrol'}&version=#{CGI.escape 'IPCC 99'}",
                      34.4*2.0*69.25,
                      :volume => 2.0, :name=>"byVolume")
  end
  it "should give the right amount of carbon by mass" do
    check_calculation(folder,
                      "fuel=Coal&version=UK",
                      0.027005*2.0*94.53,
                      :mass => 2.0, :name=>"byMass")
  end
end
