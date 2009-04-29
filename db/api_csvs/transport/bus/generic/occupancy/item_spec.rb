AMEE_SVN_ROOT=ENV['AMEE_SVN']
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon" do
    check_calculation(folder,
                      "context=urban",
                      2*2.6417*8/(4*1.7),
                      :accountedPassengers => 2,
                      :occupancy => 4,
                      :distance => 8
                      )
  end
  it "should give the right value for JB's occupancy and EF" do
    check_calculation(folder,
                      "context=NAEI%20coach",
                      0.67046*2,
                      :accountedPassengers => 100,
                      :occupancy => 50,
                      :distance => 1
                      )
  end
end
