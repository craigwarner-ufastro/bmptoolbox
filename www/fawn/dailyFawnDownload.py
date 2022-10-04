#!/usr/bin/python
### Daily FAWN download script to query data feed, download each day's ###
### weather data, interpolate if necessary, write daily CSV file to disk, ###
### and update weather files.  Run every hour on the hour. ###
import socket, os, sys
import urllib2
from datetime import datetime
from math import *
import time

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

log = open('fawnData/fawn.log','ab')
now = datetime.today()
d = datetime.fromtimestamp(time.time()-86400)
d2 = datetime.fromtimestamp(time.time()-172800)
if (d.hour > 22 or d.hour < 3):
  #Don't run at 11pm, 12am, 1am, or 2am
  sys.exit()
csvfile = 'fawnData/fawn-' + d.strftime("%m%d%y") + '.csv'
prevcsv = 'fawnData/fawn-' + d2.strftime("%m%d%y") + '.csv'

if (os.access(csvfile, os.F_OK)):
  #If csv file exists, exit.
  sys.exit()

#Open log file
log = open('fawnData/fawn.log','ab')
#Read previous day's csv file in case needed
f = open(prevcsv, 'rb')
prevdata = f.read().split('\n')
f.close()
removeEmpty(prevdata)

#Sent HTTP request for current data
#html = ''
#host = "fawn.ifas.ufl.edu"
#page = "/controller.php/lastDay/summary/csv"
#fp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#fp.connect((host, 80))
#request = "GET " + page + " HTTP/1.0\r\n"
#request += "Host: " + host + "\r\n"
#request += "Referer: http://fawn.ifas.ufl.edu\r\n";
#request += "User-Agent: PHP test client\r\n\r\n";
#fp.send(request)

##Receive response and read into an array
#sf = fp.makefile('rb')
#html = sf.read().split('\n')
#sf.close()


###3/27/18 new urllib2 code for https
response = urllib2.urlopen('https://fawn.ifas.ufl.edu/controller.php/lastDay/summary/csv')
html = response.read().split('\n')
removeEmpty(html)
#fp.close()

if (os.access(csvfile, os.F_OK)):
  #Second check: exit if csv file for today already exists.
  sys.exit()

#parse data
data = []
foundIds = []
isData = False

#Check that day is correct
n = 0
n2 = 0
for j in range(len(html)):
  #Skip over HTML/HTTP headers
  if (html[j].startswith('StationID')):
    #First line of data contains column headers
    isData = True
  if (isData):
    data.append(html[j])
    temp = html[j].split(',')
    foundIds.append(temp[0])
  #Check that line is for correct day
  if (html[j].find(d2.strftime('%Y-%m-%dT23:45:00')) != -1):
    n+=1
    temp = html[j].split(',')
    #Check number observations
    if (temp[2] != '' and int(temp[2]) > 80):
      n2+=1

#n = number of stations with correct day
#n2 = number of stations with > 80 observations
#Download at 4am regardless of value of n 
if (n != len(data)-1 and d.hour < 4):
  log.write(str(now)+": Received incorrect day!  Exiting.\n")
  log.close()
  sys.exit()

#Download at 4am regardless of value of n2 
if (n2 != len(data)-1 and d.hour < 4):
  log.write(str(now)+": Incomplete data!  Exiting.\n")
  log.close()
  sys.exit()

if (isData):
  log.write(str(now)+": SUCCESSFULLY downloaded daily fawn data!\n")
else:
  #Received no data (for today or any day) 
  log.write(str(now)+": Received incomplete or corrupt data!  Exiting.\n")
  log.close()
  sys.exit()

#Find column numbers
headers = data[0].split(',')
ncol = len(headers)
for i in range(len(headers)):
  if (headers[i] == 't60cm_min'):
    minid = i
  if (headers[i] == 't60cm_max'):
    maxid = i
  if (headers[i] == 'rain_sum'):
    rainid = i
  if (headers[i] == 'trf'):
    solarid = i
  if (headers[i] == 'rfd_avg'):
    solarid2 = i

#Read station info
f = open('FAWN_STATIONS.csv','rb')
stations = f.read().split('\n')
f.close()
removeEmpty(stations)

#Parse station info -- read id, name, lattitude, longitude
#Read id#s first and sort by id#
sids = []
for j in range(1, len(stations)):
  temp = stations[j].split(',')
  sids.append(temp[1])
sids.sort()
names = []
longs = []
latts = []
#Find matching names, lattitudes, longitudes
for j in range(len(sids)):
  for l in range(1, len(stations)):
    currStation = stations[l].split(',')
    if (sids[j] == currStation[1]):
      names.append(currStation[0])
      latts.append(float(currStation[2]))
      longs.append(float(currStation[3]))

#Check for stations with no data and add a line of zeros
for j in range(len(sids)):
  if (foundIds.count(sids[j]) == 0):
    data.insert(j+1,sids[j]+','+d2.strftime('%Y-%m-%dT23:45:00')+",0,0,0,0,0")

#Convert data and store in arrays
nobs = []
mints = []
maxts = []
rains = []
solars = []
for i in range(1, len(data)):
  row = data[i].split(',')
  try:
    mint = float(row[minid])
    maxt = float(row[maxid])
    #convert rain to mm
    rain = float(row[rainid])*10
    if (row[solarid] == '' and row[solarid2] != ''):
      #trf not given but rfd_avg is.  convert units.
      solar = float(row[solarid2])*86400/1000000.
    else:
      #trf found
      solar = float(row[solarid])
    nx = row[2]
  except Exception:
    #set all to 0 on error.  will get flagged later.
    mint = 0
    maxt = 0
    rain = 0
    solar = 0
    nx = 0
  nobs.append(nx)
  mints.append(mint)
  maxts.append(maxt)
  rains.append(rain)
  solars.append(solar)

#Find and interpolate missing data
for i in range(len(sids)):
  dist = []
  x = []
  if (nobs[i] > 0):
    continue
  #If missing data, loop back over stations
  for l in range(len(sids)):
    if (i == l):
      continue 
    #Look for stations with data that are within 1 degree of lattitude and 2 of longitude
    if (nobs[l] > 0 and abs(longs[i]-longs[l]) < 2 and abs(latts[i]-latts[l]) < 1):
      dist.append(sqrt((longs[i]-longs[l])**2+(latts[i]-latts[l])**2))
      x.append(l)
  if (len(x) > 0):
    weight = 0
    mint = 0
    maxt = 0
    rain = 0
    solar = 0
    ninterp = 0
    itext = 'from'
    #Use up to 3 closest stations by distance to interpolate.
    #Weight by 1/distance.
    while (ninterp < 3 and len(x) > 0):
      ninterp += 1
      idx = dist.index(min(dist))
      solar += solars[x[idx]]/dist[idx]
      maxt += maxts[x[idx]]/dist[idx]
      mint += mints[x[idx]]/dist[idx]
      rain += rains[x[idx]]/dist[idx]
      weight += 1./dist[idx]
      itext += ' '+names[x[idx]]+' (d='+str(dist[idx])[:4]+')'
      dist.pop(idx)
      x.pop(idx)
    solars[i] = solar/weight
    maxts[i] = maxt/weight
    mints[i] = mint/weight
    rains[i] = rain/weight
    log.write(str(datetime.today())+': Interpolated '+names[i]+' '+d.strftime("%Y%j")+'\n\tusing latt=1 and long=2\n')
    log.write('\t'+itext+'\n')
  else:
    log.write("\tCould not find data to interpolate for missing data in "+names[i]+"\n")

#Replace old data with previous day's data and nobs = 0
for j in range(1, len(data)):
  if (data[j].find(d2.strftime('%Y-%m-%dT23:45:00')) == -1):
    #This data is not from today.  Replace with yesterday's values and nobs = 0.
    row = data[j].split();
    for l in range(len(prevdata)):
      temp = prevdata[l].split()
      if (temp[0] == row[0]):
	solars[j-1] = float(temp[2])
	maxts[j-1] = float(temp[3])
	mints[j-1] = float(temp[4])
	rains[j-1] = float(temp[5])
	nobs[j-1] = 0
        break

#Write output
f = open(csvfile, 'wb')
f.write(headers[0]+',Date,'+headers[2]+','+headers[solarid]+','+headers[maxid]+','+headers[minid]+','+headers[rainid]+'\n')
for j in range(len(sids)):
  f.write(sids[j]+','+d.strftime('%Y-%m-%d')+','+str(nobs[j])+','+str(solars[j])+','+str(maxts[j])+','+str(mints[j])+','+str(rains[j])+'\n')
f.close()

#Check for missing data
for j in range(len(sids)):
  f = open(names[j].replace(' ','_')+'.wth','rb')
  oldData = f.read().split('\n')
  f.close()
  removeEmpty(oldData)
  temp = oldData[-1].split()
  if (temp[0] != d2.strftime("%Y%j")):
    if (temp[0] != d.strftime("%Y%j")):
      #If last line of weather file is not yesterday or 2 days ago, note 
      #this in log file.  Should not happen!
      log.write(str(now)+": "+names[j]+" is not up to date!\n")
  else:
    #Update weather file.
    f = open(names[j].replace(' ','_')+'.wth','ab')
    f.write('%7s %5.1f  %4.1f  %4.1f  %4.1f\n' % (d.strftime("%Y%j"), solars[j], maxts[j], mints[j], rains[j]))
    f.close()
log.close()

#Update latestUpdate.txt file read by website
f = open('latestUpdate.txt','wb')
f.write(d.strftime("%b %d, %Y"))
f.close()
