class Time
  
  def start_of_day
    a = to_a
    a[0], a[1], a[2] = 0, 0, 0
    Time.utc *a
  end  
  
  def to_js_i
    to_i * 1000
  end
  
end

class Object
  def to_openstruct
    self
  end
end

class Array
  def to_openstruct
    map{ |el| el.to_openstruct }
  end
end

class Hash
  def to_openstruct
    mapped = {}
    each{ |key,value| mapped[key] = value.to_openstruct }
    OpenStruct.new(mapped)
  end
end

