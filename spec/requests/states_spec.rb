require File.join(File.dirname(__FILE__), '..', 'spec_helper.rb')

describe "/state" do
  before(:each) do
    @response = request("/state")
  end
end