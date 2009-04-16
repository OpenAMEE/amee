AMEE_SVN_ROOT="/home/jamespjh/Private/amee/svn.amee.com"
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon" do
    check_calculation(folder,
                      "",
                      2.0*0.5*0.72*21*0.25*0.9/0.75,
                      :collected => 2.0)
  end
end
