require 'rubygems'
require 'spec'

describe 'default.js' do
  it 'should run without errors with no context' do
    `rhino default.js`.should eql ""
  end
  
end
