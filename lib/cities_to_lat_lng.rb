# Grab the list of cities from merb
require 'open-uri'
cities = DataStore.data.select { |e| e.geocode_country == "US" }.map { |e| e.city }.uniq.sort

puts "{"
cities.each do |c|
  res = ""
  q = URI.escape "#{c} US"
  url = "http://maps.google.com/maps/geo?output=json&oe=utf-8&q=#{q}&gl=US&key=ABQIAAAAW_tqT0xj2IdAv-D6n9p4TBSJ96pU6mbroTsW1oFFdHcOPyoZbBQyCgZgbQ0-kEtwKqxvSKAhggtGiw&mapclient=jsapi"
  open(url) { |f| res = f.read }
  res = JSON.parse res
  res = res.to_openstruct
  coords = res.Placemark[0].Point.coordinates[0,2]
  puts %Q{"#{c}" => #{coords.inspect},}
end
puts "}"
