#!/usr/bin/env ruby

codes = File.readlines "data/us_states"
codes = codes.map do |line|
  code, state = line.split(/\s/, 2)
  state = state.strip.split(" ").map { |s| s.capitalize }.join(" ")
  %Q{"#{code}" => "#{state}"}
end

puts "{\n\t#{codes.join(",\n\t")} \n}"
