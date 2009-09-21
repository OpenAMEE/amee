class Search < Application
  
  def index q, limit
    res = DataStore.search q
    res = res[0, limit.to_i]
    res = OutputFormatter.new.to_jq_ac res
    render res, :layout => false    
  end
  
end
