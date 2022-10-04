#!/usr/bin/python
### Script to interpolate missing historical data through Jan 31, 2010 ###
### Will only be used again if reconstructing weather files from scratch ###
### Takes 2 arguments: lattitude and longitude thresholds ###
### Finds up to 3 stations with data on the same day within these ###
### thresholds and uses a weighted average of the 3 closest by overall ###
### distance to interpolate the missing datapoint ###
import time, os, sys, glob
from math import *
from datetime import datetime
from datetime import timedelta

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

#Open log file and parse command line args
logfile = 'fawnData/interp.log'
log = open(logfile,'ab')
lattthresh = 1
longthresh = 1
if (len(sys.argv) > 1):
  lattthresh = float(sys.argv[1])
  longthresh = float(sys.argv[1])
if (len(sys.argv) > 2):
  longthresh = float(sys.argv[2])

#Read station info
f = open('FAWN_STATIONS.csv','rb')
stations = f.read().split('\n')
f.close()
removeEmpty(stations)

#Hardcoded start date and end date
#Later data is interpolated as it comes in by the new dailyFawnDownload.py
now = datetime.today()
endDate = datetime(2010, 3, 21)
t = timedelta(days=1)

names = []
ids = []
longs = []
latts = []
data = []
newData = []
count = []
header = []
#Loop over stations
for j in range(1, len(stations)):
  #Parse station info into arrays
  currStation = stations[j].split(',')
  names.append(currStation[0])
  ids.append(currStation[1])
  latts.append(float(currStation[2]))
  longs.append(float(currStation[3]))
  data.append(dict())
  count.append(0)
  newData.append(dict())
  #Open weather file for this station and read data
  f = open(names[j-1].replace(' ','_')+'.wth','rb')
  currData = f.read().split('\n')
  f.close()
  removeEmpty(currData)
  header.append(currData[0:5])
  #Append daily weather data to data dict.
  for l in range(5, len(currData)):
    temp = currData[l].split()
    data[j-1][temp[0]] = currData[l] 

#Check for missing data
#Loop over stations
for j in range(len(names)):
  id = ids[j] 
  long = longs[j]
  latt = latts[j]
  #Keys are dates of the form 1999001
  keys = data[j].keys()
  keys.sort()
  currDate = datetime(*(time.strptime(keys[0], '%Y%j')[0:6]))
  n = 1
  #Loop over dates up to the specified end date
  while (currDate < endDate):
    #Add one day to currDate to increment date
    currDate += t
    d = datetime(*(time.strptime(keys[n],'%Y%j')[0:6]))
    if (d != currDate):
      #There is a missing datapoint as the next key is farther in the
      #future than the current date (e.g. a skip from 1999001 to 1999003.
      #Increment count and generate new key index for this date.
      count[j]+=1
      idx = currDate.strftime("%Y%j")
      dist = []
      x = []
      #Loop over stations and look for ones with data on this date.
      for l in range(len(names)):
	if (data[l].has_key(idx)):
	  #Check that longitude and lattitude difference are within
	  #specified thresholds
	  if (abs(long-longs[l]) < longthresh and abs(latt-latts[l]) < lattthresh):
	    #Calculate and store overall distance and index number of station
	    dist.append(sqrt((long-longs[l])**2+(latt-latts[l])**2))
	    x.append(l)
      if (len(x) > 0):
	#Found at least one station in the specified region that has data
	#on this date
	weight = 0
        mint = 0
        maxt = 0
        rain = 0
        solar = 0 
	ninterp = 0
	itext = 'from'
	#Select up to 3 stations if possible.  Weight by 1/distance.
	while (ninterp < 3 and len(x) > 0):
	  ninterp += 1
	  i = dist.index(min(dist))
	  line = data[x[i]][idx].split()
	  solar += float(line[1])/dist[i]
	  maxt += float(line[2])/dist[i]
	  mint += float(line[3])/dist[i]
	  rain += float(line[4])/dist[i]
	  weight += 1./dist[i]
	  itext += ' '+names[x[i]]+' (d='+str(dist[i])[:4]+')'
	  dist.pop(i)
	  x.pop(i)
	solar /= weight
	maxt /= weight
	mint /= weight
	rain /= weight
	#Create new line in dict newData and note in log file
	newData[j][idx] = '%7s %5.1f  %4.1f  %4.1f  %4.1f' % (idx, solar, maxt, mint, rain)
	log.write(str(datetime.today())+': Interpolated '+names[j]+' '+idx+'\n\tusing latt='+str(lattthresh)+' and long='+str(longthresh)+'\n')
	log.write('\t'+itext+'\n')
      else:
	#Print out station name and date if no interpolation candidates found
	print names[j], idx
    else:
      n+=1 
log.close()

#Loop over stations in newData dict
for j in range(len(newData)):
  keys = data[j].keys()
  #extend keyset by appending new keys
  keys.extend(newData[j].keys())
  #sort to make order chronological
  keys.sort()
  #Open weather file for writing
  f = open(names[j].replace(' ','_')+'.wth','wb')
  #Write headers
  for l in range(len(header[j])):
    f.write(header[j][l]+'\n')
    if (l == 0):
      f.write('\n')
  #Loop over new key set and write data
  for l in range(len(keys)):
    if (data[j].has_key(keys[l])):
      #Data already existed
      f.write(data[j][keys[l]]+'\n')
    elif (newData[j].has_key(keys[l])):
      #Data didn't exist before but has been interpolated
      f.write(newData[j][keys[l]]+'\n')
      #Check for .csv files for this date
      currLine = newData[j][keys[l]].split()
      d = datetime(*(time.strptime(currLine[0], '%Y%j')[0:6]))
      currFile = 'fawnData/fawn-' + d.strftime("%m%d%y") + '.csv'
      if (os.access(currFile, os.F_OK)):
	#Read csv file if one exists
	daily = open(currFile, 'rb')
	dailyData = daily.read().split('\n')
	daily.close()
	removeEmpty(dailyData)
	#Update line with nobs = 0 and interpolated data
	for i in range(1, len(dailyData)):
	  temp = dailyData[i].split(',')
	  nobs = int(temp[2])
	  if (nobs == 0 and temp[0] == ids[j]):
	    dailyData[i] = ids[j]+","+d.strftime("%Y-%m-%d")+",0,"+currLine[1]+","+currLine[2]+","+currLine[3]+","+currLine[4]
	    print dailyData[i]
	#Write csv file back to disk
	daily = open(currFile, 'wb')
	for i in range(len(dailyData)):
	  daily.write(dailyData[i]+"\n")
	daily.close()
  f.close()
  #Print station name, number of missing datapoints, and number that were
  #able to be interpolated.
  print names[j], count[j], len(newData[j])
