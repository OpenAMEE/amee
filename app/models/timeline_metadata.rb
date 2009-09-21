class TimelineMetadata
  
  def self.metadata 
    # <a href='http://www.nytimes.com/2009/06/27/us/politics/27climate.html?_r=1&hp'>Link</a>    

    md = [
      ["2009/01/20", {:watts => "Presidential Inauguration of Barack Obama"}],
      ["2009/02/01", {:watts => "Super Bowl XLIII"}],
      ["2009/05/25", {:watts => "Death of Michael Jackson"}],
      ["2009/06/26", {:co2 => "Waxman-Markey Bill Passed"}],
    ]
  end
  
  #
  # Both data and md must be sorted by date
  #
  def self.merge data, metadata = self.metadata
    # Make a copy so we don't modify the existing data
    data = data.dup

    metadata.each do |mdi|
      mdi_time = Time.parse mdi[0]

      prev_time = data[0].time
      prev_co2 = data[0].co2
      prev_watts = data[0].watts

      data.each_index do |i|
        di = data[i]
        
        if mdi_time > prev_time && mdi_time <= di.time
          if mdi_time == di.time
            co2, watts = di.co2, di.watts
          else
            co2, watts = (prev_co2 + di.co2) / 2, (prev_watts + di.watts) / 2
          end
          e = Entry.create :time => mdi_time, :co2 => co2, :watts => watts,
            :co2_title => mdi[1][:co2], :watts_title => mdi[1][:watts]
          
          data.insert i, e
          di = e
        end
        
        prev_time = di.time
        prev_co2 = di.co2
        prev_watts = di.watts
      end
    end
    
    data
  end
  
end
