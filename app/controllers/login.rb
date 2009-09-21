class Login < Merb::Controller

  def index
    if logged_in
      redirect "/"
    else
      render
    end
  end
  
  def login
    if params[:password].downcase == "lithium"
      session["username"] = params[:username]
      redirect "/"
    else 
      redirect "/login", :message => {:feedback => "Invalid Password"}
    end
  end

  def show
    redirect "/"
  end
  
  def logout
    session.delete "username"
    redirect "/"
  end
  
end
