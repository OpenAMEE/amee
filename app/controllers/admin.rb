class Admin < Application

  def index
    render
  end
  
  def data_upload
    attachment = params[:attachment]
    
    File.makedirs "public/uploads/"
    FileUtils.mv attachment[:tempfile].path, "public/uploads/#{attachment[:filename]}"
        
    ww_data = JSON.parse(File.read("data/country.json"))
    us_data = JSON.parse(File.read("data/state.json"))
    loader = DataLoader.new ww_data, us_data 
    loader.load "public/uploads/#{attachment[:filename]}"
    
    redirect "/admin"
  end
  
  def add_sample_data
    ww_data = JSON.parse(File.read("data/country.json"))
    us_data = JSON.parse(File.read("data/state.json"))
    loader = DataLoader.new ww_data, us_data 
    loader.create_random_data    
    
    redirect "/admin"
  end
  
end
