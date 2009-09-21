require File.join(File.dirname(__FILE__), '..', 'spec_helper.rb')

describe "/data_centers" do
  before(:each) do
    @response = request("/data_centers")
  end
end