#!/usr/bin/python
### Daily FAWN update script to look at CSV files for previous n days, ###
### query FAWN report generator for that time period, and update both ###
### CSV and weather files with any new data.  In the case of interpolated ###
### data where nobs == 0, nobs must be at least 48 to supercede the ###
### interpolated values.  Run for past 7 days hourly from 4:30am until ###
### 10:30pm.  Run for past 365 days daily at 10:35pm.  ###
import time, os, sys, glob, socket
from datetime import datetime
from datetime import timedelta

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

f = open('FAWN_STATIONS.csv','rb')
stations = f.read().split('\n')
f.close()
removeEmpty(stations)

#argument is number of previous days to look at
if (len(sys.argv) > 1):
  ndays = int(sys.argv[1])
else:
  ndays = 50

#Read in station info
sids = []
names = []
for j in range(len(stations)):
  temp = stations[j].split(',')
  sids.append(temp[1])
  names.append(temp[0])

d = datetime.today()
t = timedelta(days=1)

now = datetime.today()
if (d.hour > 23 or d.hour < 4):
  log = open('fawnData/fawnUpdates.log','ab')
  log.write(str(now)+": Between 11pm and 4am.  Exiting.\n")
  log.close()
  sys.exit()

#Look at previous ndays days data, see if any reports missing
n = 0
for j in range(ndays):
  d -= t
  #Check if there is a CSV file for this day
  currFile = 'fawnData/fawn-' + d.strftime("%m%d%y") + '.csv'
  if (os.access(currFile, os.F_OK)):
    #If so, read the data and count any rows with nobs < 96.
    daily = open(currFile,'rb')
    data = daily.read().split('\n')
    daily.close()
    removeEmpty(data)
    for l in range(1, len(data)):
      temp = data[l].split(',')
      nobs = int(temp[2])
      if (nobs < 96):
	n += 1
  else:
    #CSV file does not exist!  This should not happen!  Create CSV
    #file with all zero data.  Data will be queried and filled  in later
    temp = sids[1:]
    temp.sort()
    daily = open(currFile,'wb')
    daily.write("StationID,Date,num_obs,trf,t60cm_max,t60cm_min,rain_sum\n")
    for l in range(len(temp)):
      daily.write(temp[l]+','+d.strftime("%Y-%m-%d")+",0,0,0,0,0\n")
      n+=1
    daily.close()
#Report total lines with nobs = 0.
log = open('fawnData/fawnUpdates.log','ab')
log.write(str(datetime.today())+": Found "+str(n)+" datapoints with < 96 observations!\n")
log.close()

if (n == 0):
  sys.exit()

#Format HTTP POST request and submit to FAWN report generator
html = ''
host = "fawn.ifas.ufl.edu"
page = "/data/reports/?res"
#postData = "reportType=daily&presetRange=30&vars__AirTemp1=1&vars__Rainfall=1&vars__TotalRad=1&format=.CSV+(Excel)";
postData = "reportType=daily&presetRange=dates"
d = datetime.today()-timedelta(days=1)
d2 = d-timedelta(days=ndays)
postData += "&fromDate_m=" +str(int(d2.strftime("%m"))) + "&fromDate_d=" + str(int(d2.strftime("%d"))) + "&fromDate_y=" + d2.strftime("%Y")
postData += "&toDate_m=" + str(int(d.strftime("%m"))) + "&toDate_d=" + str(int(d.strftime("%d"))) + "&toDate_y=" + d.strftime("%Y")
#postData += "&toDate_m=1&toDate_d=8&toDate+y=2010"
postData += "&vars__AirTemp1=1&vars__Rainfall=1&vars__TotalRad=1&format=.CSV+(Excel)";
for j in range(1, len(sids)):
  postData+="&locs__"+sids[j]+"=1";
fp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
fp.connect((host, 80))
request = "POST " + page + " HTTP/1.0\r\n"
request += "Host: " + host + "\r\n"
request += "Referer: http://fawn.ifas.ufl.edu\r\n";
request += "User-Agent: PHP test client\r\n";
request += "Content-Type: application/x-www-form-urlencoded\r\n";
request += "Content-Length: "+str(len(postData))+"\r\n\r\n";
request += postData+"\r\n\r\n";
fp.send(request)

fp.settimeout(60)
#Read CSV response
sf = fp.makefile('rb')
try:
  html = sf.read().split('\n')
except Exception:
  log = open('fawnData/fawnUpdates.log','ab')
  log.write(str(datetime.today())+": Socket timed out!\n") 
  log.close()
  sys.exit()
sf.close()
removeEmpty(html)
fp.close()

#parse data
report = []
isData = False
for j in range(len(html)):
  if (html[j].startswith('"FAWN Station"')):
    isData = True
  if (isData):
    report.append(html[j].replace('"',''))

d = datetime.today()
#Look at previous ndays days data again, pick out lines with missing data
nupdated = 0
for j in range(ndays):
  d -= t
  reportDate = d.strftime("%d %b %Y")
  if (reportDate[0] == "0"):
    reportDate = reportDate[1:]
  currFile = 'fawnData/fawn-' + d.strftime("%m%d%y") + '.csv'
  doUpdate = False
  #Read CSV file again
  if (os.access(currFile, os.F_OK)):
    daily = open(currFile,'rb')
    data = daily.read().split('\n')
    daily.close()
    removeEmpty(data)
    #Look for any line with < 96 obs
    #This includes all rows of zeros added above
    for l in range(1, len(data)):
      temp = data[l].split(',')
      nobs = int(temp[2])
      fawnDate = temp[1]
      id = temp[0]
      if (nobs < 96):
	#Look through newly downloaded report.  Find line corresponding to
	#This station and date.  If there are new observations listed
	#Proceed with updating
	for i in range(len(sids)):
	  if (sids[i] == id):
	    currName = names[i]
	for i in range(1, len(report)):
	  if (report[i].startswith(currName+","+reportDate)):
	    row = report[i].split(',')
	    #nobs is last column
	    reportnobs = int(row[-1])
	    #Must be greater than old nobs AND at least 48
	    if (reportnobs > nobs and reportnobs > 47):
	      #Construct new line for fawn .csv file
	      newline = id+","+fawnDate+","+str(reportnobs)+","
	      try:
		#convert F to C
		mint = (float(row[3])-32)/1.8
		maxt = (float(row[4])-32)/1.8
		#convert inches to mm 
		rain = float(row[5])*25.4
		#convert W/m^2 to MJ/m^2
		solar = float(row[7])*86400/1000000.
	      except Exception:
		continue
	      #Take 3 decimals for solar, rain
	      #take 2 decimals for temps
	      newline += "%0.3f" % solar + ","
              newline += "%0.2f" % maxt + ","
              newline += "%0.3f" % mint + ","
              newline += "%0.3f" % rain
	      data[l] = newline
	      doUpdate = True
	      #send log message
	      log = open('fawnData/fawnUpdates.log','ab')
              log.write(str(datetime.today())+': checkForMissingData.py> updated '+currName+' for '+d.strftime("%m-%d-%y")+'\n')
	      log.close()
              #read in corresponding .wth file
	      wthFile = currName.replace(" ","_")+'.wth'
	      f = open(wthFile,'rb')
	      wthData = f.read().split('\n')
	      f.close()
	      removeEmpty(wthData)
	      #find this date and update it
	      for k in range(len(wthData)):
		if (wthData[k].startswith(d.strftime("%Y%j"))):
		  newWthLine = '%7s %5.1f  %4.1f  %4.1f  %4.1f' % (d.strftime("%Y%j"), solar, maxt, mint, rain)
		  wthData[k] = newWthLine
	      #write new weather file
	      f = open(wthFile,'wb')
	      for k in range(len(wthData)):
		f.write(wthData[k]+'\n')
		if (k == 0):
		  f.write('\n')
	      f.close()
	      nupdated += 1
  #write new fawn .csv file
  if (doUpdate):
    daily = open(currFile, 'wb')
    for l in range(len(data)):
      daily.write(data[l]+'\n')
      #if (l == 0):
	#daily.write('\n')
    daily.close()

log = open('fawnData/fawnUpdates.log','ab')
log.write(str(datetime.today())+": checkForMissingData.py> Updated "+str(nupdated)+" datapoints!\n")
log.close()
