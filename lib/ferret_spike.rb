require 'ferret'

index = Ferret::Index::Index.new

DataStore.data.each do |e|
  index << {:url => "/data_centers/#{e.data_center_id}", :content => e.data_center_location}
  index << {:url => "/data_centers/#{e.data_center_id}", :content => e.data_center_id}
end

index.search_each('content:d*') do |id, score|
  # puts "Document #{id} found with a score of #{score}"
  doc = index[id].load 
  puts doc.class
  puts doc.inspect
end
