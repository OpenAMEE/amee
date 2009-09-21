class Application < Merb::Controller

  before :ensure_logged_in

end