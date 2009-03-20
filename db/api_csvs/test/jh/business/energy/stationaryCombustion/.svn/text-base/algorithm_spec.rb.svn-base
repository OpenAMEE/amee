require 'rubygems'
require 'spec'

describe 'algorithm.js' do
  it 'should run without errors with no context' do
    `rhino algorithm.js`.should eql ""
  end
  it 'should run with a basic context' do
    context="1.0,0.0,0.0,true,false,false"
    `rhino -f algorithm.js -e 'print(carbon(#{context}))'`.should eql "1.0\n"
  end
end
