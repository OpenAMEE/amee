class Filter
  
  def aggregate_by field, data
    buckets = data.inject({}) do |h, e|
      key = e.send field
      existing = h[key]
      if existing
        existing.watts += e.watts
        existing.count += e.count
        existing.co2 += e.co2
      else
        h[key] = e.dup
      end
      h
    end
    buckets.values
  end
  
  def presentation_filter data
    data = data.sort { |a,b| b.co2 <=> a.co2 }

    median_co2 = data[data.length / 2].co2
    median = Entry.create :co2 => median_co2,
      :product_id => "", :product_name => "Median across all", 
      :data_center_id => "", :data_center_location => "Median across all"
    
    avg_co2 = data.inject(0) { |s, e| s += e.co2 } / data.length
    avg = Entry.create :co2 => avg_co2,
      :product_id => "", :product_name => "Average across all", 
      :data_center_id => "", :data_center_location => "Average across all"
    
    data = data[0, 10]

    data << avg << median
    data = data.sort { |a,b| b.co2 <=> a.co2 }
  end
  
end
