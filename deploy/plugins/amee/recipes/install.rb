namespace :install do
  
  desc "Package AMEE and install into the SCM"
  task :default do
    check
    prepare
    package
    deploy_to_git
    tag
  end
  
  desc "Check install dependencies"
  task :check do
    
    # Check package directory has been checked-out from Git repository
    if !File.directory?(package_dir)
      puts "ERROR: Abandoning install - #{package_dir} does not exist. Please clone the deployment Git respository before continuing."
      exit
    end
    
    # Ensure a release tag has been supplied 
    @tag_name = ENV['TAG']
    puts "You must specify a tag for this release using TAG=name" unless @tag_name
    exit
    
    # Check that local repository contains no local modifications
    return if `git status` =~ /working directory clean/
    puts "Must have clean working directory - #{src_dir} contains local modifications."
    exit

  end
  
  task :prepare do
    # Switch the the correct branch
    @pwd = Dir.pwd
    Dir.chdir(package_dir)
    `git checkout #{branch}`
    `git fetch`
    
    # Remove the previous install artifacts
    FileUtils.rm_r Dir.glob("#{package_dir}/*")
    
  end
  
  desc "Build the AMEE deployment package"  
  task :package do

    # Create bin
    puts "Creating new deployment bin directory #{package_dir}/bin"
    FileUtils.mkdir_p("#{package_dir}/bin")
    FileUtils.cp_r "#{src_dir}/server/amee-engine/bin/.","#{package_dir}/bin"  
    
    # Create conf
    puts "Creating new deployment conf directory #{package_dir}/conf"
    FileUtils.mkdir_p("#{package_dir}/conf")
    FileUtils.cp_r "#{src_dir}/server/amee-engine/conf/.","#{package_dir}/conf"  

    # Create htdocs
    puts "Creating new deployment htdocs directory #{package_dir}/htdocs"
    FileUtils.mkdir_p("#{package_dir}/htdocs")
    FileUtils.cp_r "#{src_dir}/htdocs/.","#{package_dir}/htdocs"  

    # Create skins
    puts "Creating new deployment skins directory #{package_dir}/skins"
    FileUtils.mkdir_p("#{package_dir}/skins")
    FileUtils.cp_r "#{src_dir}/server/amee-skins/.","#{package_dir}/skins"  

    # Create lib
    puts "Creating new deployment lib directory #{package_dir}/lib"
    FileUtils.mkdir_p("#{package_dir}/lib")
    FileUtils.cp_r Dir.glob("#{src_dir}/server/target/dependency/*.jar"), "#{package_dir}/lib"  
    FileUtils.cp_r Dir.glob("#{src_dir}/server/*/target/dependency/*.jar"), "#{package_dir}/lib"  
    FileUtils.cp_r Dir.glob("#{src_dir}/server/*/target/*.jar"), "#{package_dir}/lib"  

    # Create db
    puts "Creating new deployment db directory #{package_dir}/db"
    FileUtils.mkdir_p("#{package_dir}/db")
    FileUtils.cp_r "#{src_dir}/db/.","#{package_dir}/db"
    
  end

  desc "Send the deployment package to Git repository"
  task :deploy_to_git do
    `git add .`
    `git commit -m 'Install from capistrano on #{Time.now}'`
    `git push`
  end
  
  desc "Tag the src and deployment repositories"
  task :tag do
    unless @tag_name
      puts "You must specify a tag for this release using TAG=name"
      exit
    end
    
    # Tag the deployment repository
    `git tag -a "#{@tag_name}" -m "#{@tag_name}"`
    Dir.chdir(@pwd)
    # Tag the src repository
    `git tag -a "#{@tag_name}" -m "#{@tag_name}"`
  end
  
end