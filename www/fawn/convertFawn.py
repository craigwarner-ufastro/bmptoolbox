#!/usr/bin/python
### Script to read yearly FAWN csv files and write out .wth files ###
### Used up through the end of 2008 ###
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

#Glob on all yearly csv files
#Sort so they're in chronological order
csvs = glob.glob('????_daily.csv')
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
  #Create a new wth file for each station and write header info
  f = open(name.replace(' ','_')+'.wth','wb')
  f.write('*WEATHER DATA : '+currStation[5]+','+currStation[6]+'\n')
  f.write('\n')
  f.write('@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT\n')
  f.write('  %4s   %6.3f  %6.3f   %#3.0f %4.1f %4.1f %4.1f %4.1f\n' % (id, float(latt), float(longt), float(elev), -99.9, -99.9, -99.9, -99.9))
  f.write('@DATE  SRAD  TMAX  TMIN  RAIN\n')
  firstPass = True
  #Loop over csv files
  for l in range(len(csvs)):
    #Read in csv file
    fcsv = open(csvs[l], 'rb')
    currcsv = fcsv.read().split('\n')
    fcsv.close()
    removeEmpty(currcsv)
    headers = currcsv[0].split(',')
    ncol = len(headers)
    #Find column numbers for relevant quantities
    for i in range(len(headers)):
      if (headers[i] == 'min_temp_air_60cm_C'):
	minid = i
      if (headers[i] == 'max_temp_air_60cm_C'):
	maxid = i
      if (headers[i] == 'sum_rain_2m_inches'):
	rainid = i
      if (headers[i] == 'trf_2m_MJm2'):
	solarid = i
      if (headers[i] == 'avg_rfd_2m_wm2'):
	solarid2 = i
      if (headers[i] == 'min_temp_air_2m_C'):
	min2mid = i
      if (headers[i] == 'max_temp_air_2m_C'):
        max2mid = i
    #Loop over the rows of the csv file 
    for i in range(1,len(currcsv)):
      row = currcsv[i].split(',')
      if (row[0] != id):
	#Not the current station
	continue
      #Parse the date
      d = datetime(*(time.strptime(row[1],'%Y-%m-%d')[0:6]))
      try:
	#Try reading the columns
        mint2m = -99
	mint60cm = -99
	maxt2m = -99
	maxt60cm = -99
	if (row[min2mid] != ''):
	  mint2m = float(row[min2mid])
        if (row[minid] != ''):
	  mint60cm = float(row[minid])
        if (row[max2mid] != ''):
	  maxt2m = float(row[max2mid])
	if (row[maxid] != ''):
	  maxt60cm = float(row[maxid])
	if (mint60cm == -99 and maxt60cm == -99):
	  #60cm temps not defined, try 2m
	  mint = mint2m
	  maxt = maxt2m
	elif (mint2m == -99 and maxt2m == -99):
	  #2m temps not defined, use 60cm
	  mint = mint60cm
	  maxt = maxt60cm
	elif ((abs(mint60cm-mint2m) > 10 or abs(maxt60cm-maxt2m) > 10) and mint60cm < 0):
	  #If 60cm temps are bad (there were a few bizarre datapoints for 60cm
	  #where they clearly had an equipment malfunction), use 2m temps
	  mint = mint2m
	  maxt = maxt2m
	else:
	  #Default case, use 60cm temps
	  mint = mint60cm
	  maxt = maxt60cm 
	if (mint == -99 or maxt == -99):
	  #If neither is defined, skip this line
	  continue
        #convert inches to mm
        rain = float(row[rainid])*25.4
	if (row[solarid] == '' and row[solarid2] != ''):
	  #If trf is not defined and rfd_avg is, use that and convert
	  solar = float(row[solarid2])*86400/1000000.
        else:
	  #Default case, use trf
	  solar = float(row[solarid])
      except Exception:
	continue
      if (firstPass):
	if (solar < 1):
	  #Don't start using data until trf or rfd_avg is defined.
	  #Some stations had temps defined before this point but we need
	  #all 4 quantities.
	  continue
	else:
	  firstPass = False
      #Write out line to weather file
      f.write('%7s %5.1f  %4.1f  %4.1f  %4.1f\n' % (d.strftime("%Y%j"), solar, maxt, mint, rain))
  f.close()
