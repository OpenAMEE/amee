#!/usr/bin/env ruby
require 'rubygems'
require 'fastercsv'
require 'extlib'

# Assume csv will not contain date info so pre process to insert it
def pre_process_headers headers
  headers.unshift "date"
end

def add_headers js, headers
  types = {
    "date" => "date",
    "count" => "number",
    "watts" => "number",
  }
  
  js << "cols: [\n"
  
  type_arr = []

  headers.each do |h|
    t = types[h] || "string"
    type_arr << t
    js << "\t{type: '#{t}', label: '#{h}'},\n"
  end
  js << "], \n"
  type_arr
end

# Assume csv will not contain date info so pre process to insert it
def pre_process_rows rows, time
  # convert time objs into the format required by google vis api
  ta = time.to_a
  date = "new Date(#{ta[5]}, #{ta[4]}, #{ta[3]})"
  rows.each { |row| row.unshift date }
end

def add_rows js, rows, type_arr
  js << "rows: [\n"
  
  rows.each do |cells|
    js << "\t{c:["
    cells.each_index do |i|
      val = cells[i]
      val = %Q{"#{val}"} if type_arr[i] == "string" && !val.nil?
      val = "null" if val.nil?
      js << "{v: #{val}},"
    end
    js << "]},\n"
  end
  
  js << "]\n"
end

def extract_time s
  s =~ /\d{4}_\d{2}/
  Time.parse $&.sub("_", "-")
end

def convert
  time = extract_time ARGV[0]
  js = "{\n"

  csv = FasterCSV.read ARGV[0]

  headers = pre_process_headers csv.shift
  type_arr = add_headers js, headers

  rows = pre_process_rows csv, time
  add_rows js, rows, type_arr
  
  js << "}\n"
end

puts convert
