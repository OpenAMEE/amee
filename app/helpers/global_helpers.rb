module Merb
  module GlobalHelpers
    
    def ensure_logged_in
      unless logged_in
        throw :halt, redirect("/login")
      end
    end
    
    def logged_in
      session["username"] != nil
    end
    
    def co2
      "CO<sub>2</sub>"
    end
    
    def ensure_data
      if DataStore.data.empty?
        throw :halt, redirect("/admin/index")
      end
    end

  end
end
