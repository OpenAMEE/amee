Merb.logger.info("Loaded DEVELOPMENT Environment...")
Merb::Config.use { |c|
  c[:exception_details] = true
  c[:reload_templates] = true
  c[:reload_classes] = true
  c[:reload_time] = 0.5
  c[:ignore_tampered_cookies] = true
  c[:log_auto_flush ] = true
  c[:log_level] = :debug

  c[:log_stream] = STDOUT
  c[:log_file]   = nil
  # Or redirect logging into a file:
  # c[:log_file]  = Merb.root / "log" / "development.log"
}

Merb::BootLoader.after_app_loads do
  # This will get executed after your app's classes have been loaded.
  
  ww_data = JSON.parse(File.read("data/country.json"))
  us_data = JSON.parse(File.read("data/state.json"))
  loader = DataLoader.new ww_data, us_data 
  
  ["data/generic_data_2009_08.csv"].each do |fn|
    loader.load fn
  end
  # Uncomment to create sample data seeded from the uploaded data
  loader.create_random_data
      
end
