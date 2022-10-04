#!/usr/bin/python
### Obsolete script used to convert format of daily csv downloads from ###
### original format to current more useful format ###
import time, os, sys, glob
from datetime import datetime

def removeEmpty(s):
  while (s.count('') > 0):
    s.remove('')

csvs = glob.glob(sys.argv[1])
csvs.sort()

for j in range(len(csvs)):
  d = datetime(*(time.strptime(csvs[j][csvs[j].find('fawn-')+5:csvs[j].rfind('.')],'%m%d%y')[0:6]))
  f = open(csvs[j],'rb')
  data = f.read().split('\n')
  f.close()
  removeEmpty(data)

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

  #Write output
  f = open(csvs[j], 'wb')
  f.write(headers[0]+',Date,'+headers[2]+','+headers[solarid]+','+headers[maxid]+','+headers[minid]+','+headers[rainid]+'\n')
  for i in range(1,len(data)):
    row = data[i].split(',')
    try:
      mint = float(row[minid])
      maxt = float(row[maxid])
      rain = float(row[rainid])*10
      if (row[solarid] == '' and row[solarid2] != ''):
        solar = float(row[solarid2])*86400/1000000.
      else:
        solar = float(row[solarid])
    except Exception:
      #num_obs = 0 will get flagged later
      f.write(row[0]+','+d.strftime('%Y-%m-%d')+',0,0,0,0,0\n')
      continue
    f.write(row[0]+','+d.strftime('%Y-%m-%d')+','+row[2]+','+str(solar)+','+str(maxt)+','+str(mint)+','+str(rain)+'\n')
  f.close()
