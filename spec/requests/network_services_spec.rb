require File.join(File.dirname(__FILE__), '..', 'spec_helper.rb')

describe "/network_services" do
  before(:each) do
    @response = request("/network_services")
  end
end