require 'fileutils'

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
      puts "ERROR: Abandoning install - #{package_dir} does not exist. Please clone the deployment Git repository before continuing."
      exit
    end
    
    # Ensure a release tag has been supplied 
    @tag_name = ENV['TAG']
    unless @tag_name
      puts "You must specify a tag for this release using TAG=name"
      exit
    end
    
    # Check that local repository contains no local modifications
    unless `git status` =~ /working directory clean/
      puts "Must have clean working directory - #{src_dir} contains local modifications."
      exit
    end

  end
  
  task :prepare do
    # Switch the the correct branch and update from origin
    @pwd = Dir.pwd
    Dir.chdir(package_dir)
    `git checkout master`
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

    # Create munin
    puts "Creating new deployment munin directory #{package_dir}/munin"
    FileUtils.mkdir_p("#{package_dir}/munin")
    FileUtils.cp_r "#{src_dir}/server/amee-engine/munin/.","#{package_dir}/munin"

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
    FileUtils.cp_r "#{src_dir}/server/amee-engine/lib/wrapper","#{package_dir}/lib"  

    # Create db
    puts "Creating new deployment db directory #{package_dir}/db"
    FileUtils.mkdir_p("#{package_dir}/db")
    FileUtils.cp_r "#{src_dir}/db/.","#{package_dir}/db"

    # Create webapps
    puts "Creating new deployment webapps directory #{package_dir}/webapps"
    FileUtils.mkdir_p("#{package_dir}/webapps")
    FileUtils.cp_r "#{src_dir}/webapps/.","#{package_dir}/webapps"

    # Create scripts
    puts "Creating new deployment scripts directory #{package_dir}/scripts"
    FileUtils.mkdir_p("#{package_dir}/scripts")
    FileUtils.cp_r "#{src_dir}/scripts/.","#{package_dir}/scripts"

  end

  desc "Send the deployment package to Git repository"
  task :deploy_to_git do
    `git add .`
    `git commit -a -m 'Install from capistrano on #{Time.now}'`
    `git push -v`
  end
  
  desc "Tag the src and deployment repositories"
  task :tag do
    
    unless @tag_name
      puts "You must specify a tag for this release using TAG=name"
      exit
    end
    
    # Write out the tag to file for packaging with the deployment repo.
    system("echo #{@tag_name} > VERSION.txt")
    `git add VERSION.txt`
    `git commit -a -m 'Install from capistrano on #{Time.now}'`
    `git push -v`
        
    # Tag the deployment repository
    `git tag -f -a "#{@tag_name}" -m "#{@tag_name}"`
    `git push --tag -v`
    Dir.chdir(@pwd)
    
    # Tag the src repository
    `git tag -f -a "#{@tag_name}" -m "#{@tag_name}"`
    `git push --tag -v`
  end
  
end