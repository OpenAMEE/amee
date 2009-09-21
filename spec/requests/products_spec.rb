require File.join(File.dirname(__FILE__), '..', 'spec_helper.rb')

describe "/products" do
  before(:each) do
    @response = request("/products")
  end
end