require File.join(File.dirname(__FILE__), '..', 'spec_helper.rb')

describe "/countries" do
  before(:each) do
    @response = request("/countries")
  end
end