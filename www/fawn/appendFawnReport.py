#!/usr/bin/python
### Script to read FAWN report .csv files and append data to existing ###
### weather files (that presumably were created from yearly csv files) ###
### Used for January 2009 only ###
import time, os, sys, glob
from datetime import datetime 

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

#Read station info
f = open('FAWN_STATIONS.csv','rb')
stations = f.read().split('\n')
f.close()
removeEmpty(stations)

#Argument is file/files to be appended
#Sort to maintain chronological order
csvs = glob.glob(sys.argv[1])
csvs.sort()

#Loop over stations
for j in range(1, len(stations)):
  currStation = stations[j].split(',')
  name = currStation[0]
  print name
  id = currStation[1]
  latt = currStation[2]
  longt = currStation[3]
  elev = currStation[7]
  #Open weather file for each station for appending data 
  f = open(name.replace(' ','_')+'.wth','ab')
  #Loop over csv files
  for l in range(len(csvs)):
    #Read csv file
    fcsv = open(csvs[l], 'rb')
    currcsv = fcsv.read().split('\n')
    fcsv.close()
    removeEmpty(currcsv)
    headers = currcsv[0].replace('"','').split(',')
    ncol = len(headers)
    #Find column numbers for relevant quantities
    for i in range(len(headers)):
      if (headers[i] == '60cm T min (F)'):
	minid = i
      if (headers[i] == '60cm T max (F)'):
	maxid = i
      if (headers[i] == '2m Rain tot (in)'):
	rainid = i
      if (headers[i] == 'SolRad avg 2m (w/m^2)'):
	solarid = i
    #Loop over the rows of the csv file 
    for i in range(1,len(currcsv)):
      row = currcsv[i].replace('"','').split(',')
      if (row[0] != name):
	#Not the current station
	continue
      #Parse the date
      d = datetime(*(time.strptime(row[1],'%d %b %Y')[0:6]))
      try:
	#convert F to C
        mint = (float(row[minid])-32)/1.8
        maxt = (float(row[maxid])-32)/1.8
        #convert inches to mm
        rain = float(row[rainid])*25.4
	#convert W/m^2 to MJ/m^2
        #FAWN reports only give rfd_avg not trf.
        solar = float(row[solarid])*86400/1000000.
      except Exception:
	#Don't write line if any of these 4 quantities is missing
	continue
      #Write new line
      f.write('%7s %5.1f  %4.1f  %4.1f  %4.1f\n' % (d.strftime("%Y%j"), solar, maxt, mint, rain))
  f.close()
