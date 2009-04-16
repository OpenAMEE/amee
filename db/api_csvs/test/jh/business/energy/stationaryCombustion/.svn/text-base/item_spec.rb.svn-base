AMEE_SVN_ROOT="/home/jamespjh/Private/amee/svn.amee.com"
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon by energy" do
    check_calculation(folder,
                      "fuel=Waste+oils&context=Forestry",
                      (7.33*10**4+3*10**2*21+4*310)*2.0,
                      :NRG => 2.0)
  end
  it "should give the right amount of carbon by mass" do
    check_calculation(folder,
                      "fuel=Waste+oils&context=Forestry",
                      (7.33*10**4+3*10**2*21+4*310)*2.0*40.2,
                      :mass => 2.0,:name=>"byMass")
  end
  it "should give the right amount of carbon by volume" do
    check_calculation(folder,
                      "fuel=Naphtha&context=Energy",
                      (7.33*10**4+3*21+0.6*310)*2.0*770*44.5,
                      :volume => 2.0)
  end
end
