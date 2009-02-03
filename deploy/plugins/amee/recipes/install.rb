namespace :install do
  
  desc "Package AMEE and install into the SCM"
  task :default do
    check
    begin_install
    package
    git
    end_install
  end
  
  desc "Check install dependencies"
  task :check do
    
    # Check package directory has been checked-out from Git repository
    if !File.directory?(package_dir)
      puts "ERROR: Abandoning install - #{package_dir} does not exist. Please clone the release Git respository before continuing."
      exit
    end

    # Check that local repository contains no local modifications
    status = `git status | tail -1`
    if !status.include?("nothing to commit")
      puts "ERROR: Abandoning install - #{src_dir} contains local modifications."
      exit
    end
    
    
  end
  
  task :begin_install do
    # Switch the the correct branch
    @pwd = Dir.pwd
    Dir.chdir(package_dir)
    system("git checkout #{branch}")
    
    # Remove the previous install artifacts
    FileUtils.rm_r Dir.glob("#{package_dir}/*")
    
    # Copy amee intellij-built libs to src lib folder
    puts "Copying amee intellij-built libs to src lib directory #{src_dir}/lib"
    FileUtils.cp_r "#{src_dir}/output/lib/.","#{src_dir}/lib/amee"  
     
  end
  
  desc "Build the AMEE deployment package"  
  task :package do

    # Create bin
    puts "Creating new deployment bin directory #{package_dir}/bin"
    FileUtils.mkdir_p("#{package_dir}/bin")
    FileUtils.cp_r "#{src_dir}/server/co2-engine/bin/.","#{package_dir}/bin"  
    
    # Create conf
    puts "Creating new deployment conf directory #{package_dir}/conf"
    FileUtils.mkdir_p("#{package_dir}/conf")
    FileUtils.cp_r "#{src_dir}/server/co2-engine/conf/.","#{package_dir}/conf"  

    # Create htdocs
    puts "Creating new deployment htdocs directory #{package_dir}/htdocs"
    FileUtils.mkdir_p("#{package_dir}/htdocs")
    FileUtils.cp_r "#{src_dir}/htdocs/.","#{package_dir}/htdocs"  

    # Create skins
    puts "Creating new deployment skins directory #{package_dir}/skins"
    FileUtils.mkdir_p("#{package_dir}/skins")
    FileUtils.cp_r "#{src_dir}/server/skins/.","#{package_dir}/skins"  

    # Create lib
    puts "Creating new deployment lib directory #{package_dir}/lib"
    FileUtils.mkdir_p("#{package_dir}/lib")
    FileUtils.cp_r "#{src_dir}/lib/.","#{package_dir}/lib"  

    # Create db
    puts "Creating new deployment db directory #{package_dir}/db"
    FileUtils.mkdir_p("#{package_dir}/db")
    FileUtils.cp_r "#{src_dir}/db/.","#{package_dir}/db"
    
  end

  desc "Send the package to Git repository"
  task :git do
    system("git add .")
    system("git commit -m 'Capistrano install on #{Time.now}'")
    system("git push")
  end

  task :end_install do
    Dir.chdir(@pwd)
  end
  
end