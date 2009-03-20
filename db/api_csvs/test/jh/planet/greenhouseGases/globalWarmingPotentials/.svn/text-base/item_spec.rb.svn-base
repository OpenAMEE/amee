AMEE_SVN_ROOT="/home/jamespjh/devel/amee/svn.amee.com"
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "XML profile"

  it "should generate 42kg of CO2e from 2 kg CH4" do
    check_calculation(folder,
                      "gas=CH4",
                      21*2,
                      :emissionRate => 2.0)
  end
  it "should generate 11700*2 kg of CO2e from 2 kg HFC-23" do
    check_calculation(folder,
                      "gas=HFC-23",
                      11700*2,
                      :emissionRate => 2.0)
  end
end
