#!/usr/bin/env ruby

codes = File.readlines "data/iso_codes"
codes = codes.map do |line|
  code, md = line.split(/\s/, 2)
  md =~ /\d{4}/
  %Q{"#{code}" => "#{$`.strip}"}
end

puts "{\n\t#{codes.join(",\n\t")} \n}"
