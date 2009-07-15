include Java
require '/Development/repository/org/jscience/jscience/4.3.1/jscience-4.3.1.jar'

Unit = javax.measure.unit.Unit
Amount = org.jscience.physics.amount.Amount

class Numeric
  def method_missing(sym)
    Amount.valueOf(self, Unit.valueOf(sym.to_s))
  end
end

class Amount

   def *(other)
     if other.instance_of?(Amount)
       self.times(other)
     else
       self.times(Amount.valueOf(other, Unit::ONE))
     end
   end

   def +(other)
     if other.instance_of?(Amount)
       self.plus(other)
     else
       self.plus(Amount.valueOf(other, Unit::ONE))
     end
   end

   def -(other)
     if other.instance_of?(Amount)
       self.minus(other)
     else
       self.minus(Amount.valueOf(other, Unit::ONE))
     end
   end

   def /(other)
     if other.instance_of?(Amount)
       self.divide(other)
     else
       self.divide(Amount.valueOf(other, Unit::ONE))
     end
   end

   def **(pwr)
      self.pow(pwr)
   end
   
   alias orig_to to
   def to(other)
     if other.instance_of?(Amount)
       to(other.unit)
     else
       orig_to(other)
     end
   end
   
end


class Object

  def self.const_missing(name)
    Object.to_amount(name.to_s)
  end 

  def method_missing(sym, *args)
    Object.to_amount(sym.to_s)
  end 

  def self.to_amount(s)
    Amount.valueOf(1, Unit.valueOf(s))
  end

end