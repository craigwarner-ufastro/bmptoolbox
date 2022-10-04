#!/usr/bin/python
### Script to look back at the daily CSV files for the past 500 days and ###
### update the weather files with any new data.  Is used after ###
### convertFawn.py and appendFawnReport.py and before ###
### interpolateFawnData.py if rebuilding weather files to update them with ###
### data from 2/1/09 up to the current date.  Also run nightly at 10:35pm ###
### after checkMissingData.py is run for 500 days but checkMissingData.py ###
### should itself update the weather file so it should never do anything ###
### and is just a safeguard that things are not really messed up. ###
import time, os, sys, glob
from datetime import datetime
from datetime import timedelta

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

#Read station info
f = open('FAWN_STATIONS.csv','rb')
stations = f.read().split('\n')
f.close()
removeEmpty(stations)

#argument is number of previous days to look at
if (len(sys.argv) > 1):
  ndays = int(sys.argv[1])
else:
  #Default time period is last 500 days
  ndays = 500
now = datetime.today()
t = timedelta(days=1)
t500 = timedelta(days=ndays)

#Loop over stations
for j in range(1, len(stations)):
  currStation = stations[j].split(',')
  name = currStation[0]
  #print name
  id = currStation[1]
  #Find and open weather file for this station and read data.
  f = open(name.replace(' ','_')+'.wth','rb')
  oldData = f.read().split('\n')
  f.close()
  removeEmpty(oldData)
  d500 = now - t500
  foundDate = False
  n = len(oldData)
  #Look for line in weather file corresponding to 500 days ago and start here
  while(not foundDate):
    n-=1
    temp = oldData[n].split()
    d = datetime(*(time.strptime(temp[0],'%Y%j')[0:6]))
    if (d <= d500):
      foundDate = True
      idx = n
  #Open weather file for writing.  Write all data before 500 days ago back
  #to wth file.
  f = open(name.replace(' ','_')+'.wth','wb')
  for l in range(idx+1):
    f.write(oldData[l]+'\n')
    if (l == 0):
      f.write('\n')
  idx += 1
  #Loop over data from 500 days ago until now.
  while (d.strftime("%Y%j") != now.strftime("%Y%j")):
    d += t
    hasData = False
    #Check that a CSV file exists for this day
    #print d.strftime("%m%d%y"), d.strftime("%Y%j"), now.strftime("%Y%j")
    currFile = 'fawnData/fawn-' + d.strftime("%m%d%y") + '.csv'
    if (os.access(currFile, os.F_OK)):
      #If so, open it and read its data.
      #print currFile
      daily = open(currFile,'rb')
      data = daily.read().split('\n')
      daily.close()
      removeEmpty(data)
      headers = data[0].split(',')
      for i in range(len(headers)):
        if (headers[i] == 't60cm_min'):
          minid = i
        if (headers[i] == 't60cm_max'):
          maxid = i
        if (headers[i] == 'rain_sum'):
          rainid = i
        if (headers[i] == 'trf'):
          solarid = i
      #Find the line corresponding to this station.
      for i in range(1,len(data)):
	row = data[i].split(',')
	if (row[0] != id):
          continue
	if (row[2] == '0'):
	  #interpolated data
	  continue
        try:
          mint = float(row[minid])
          maxt = float(row[maxid])
          rain = float(row[rainid])
          solar = float(row[solarid])
	  hasData = True
	except Exception:
	  print "ERROR"
	  continue
	newLine = '%7s %5.1f  %4.1f  %4.1f  %4.1f\n' % (d.strftime("%Y%j"), solar, maxt, mint, rain)
    if (idx < len(oldData)):
      if (not hasData):
        newLine = oldData[idx]+'\n'
      log = open('fawnData/fawnUpdates.log','ab')
      #log.write(name+'\t'+d.strftime("%Y%j")+'\t'+str(hasData)+'\t'+str(idx)+'\t'+str(len(oldData))+'\n')
      temp = oldData[idx].split()
      if (temp[0] == d.strftime("%Y%j")):
	if (oldData[idx]+'\n' == newLine):
	  #No CSV file (pre 2/1/09) or data from CSV file matches
	  #weather file 
	  f.write(oldData[idx]+'\n')
	else:
	  #CSV file has newer data than weather file.  Update.
	  f.write(newLine)
	  log.write(str(datetime.today())+': updateFawnDailyData.py1> updated '+name+' for '+d.strftime("%m-%d-%y")+'\n')
	idx += 1
      elif (hasData):
	#This is a missing line in the weather file.  Should not happen anymore!
	f.write(newLine)
	log.write(str(datetime.today())+': updateFawnDailyData.py2> updated '+name+' for '+d.strftime("%m-%d-%y")+'\n')
      log.close()
    elif (hasData):
      #This day is newer than the latest day in the .wth file.  Add a line.
      f.write(newLine)
      log = open('fawnData/fawnUpdates.log','ab')
      #log.write(name+'\t'+d.strftime("%Y%j")+'\t'+str(hasData)+'\t'+str(idx)+'\t'+str(len(oldData))+'\n')
      log.write(str(datetime.today())+': updateFawnDailyData.py3> updated '+name+' for '+d.strftime("%m-%d-%y")+'\n')
      log.close()
  f.close()
