class Entry
  
  def self.stock params = nil
    e = Entry.new
    params.each { |k, v| e.send "#{k}=", v }
    e
  end
  
end
