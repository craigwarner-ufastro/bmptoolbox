#!/usr/bin/python

import os, sys, time
from datetime import datetime
import sqlite3

def uptime2():  
    with open('/proc/uptime', 'r') as f:
        uptime_seconds = float(f.readline().split()[0])
        return uptime_seconds

def getDbTime(weedb):
  try:
    con = sqlite3.connect(weedb)
    cursorObj = con.cursor()
    cursorObj.execute('select dateTime from archive ORDER BY dateTime desc limit 1')
    rows = cursorObj.fetchall()
    lastTime = rows[0][0]
    return lastTime
  except Exception as ex:
    return -1 

weedb = '/var/lib/weewx/weewx.sdb'

if (not os.access(weedb, os.F_OK)):
  print "NO"
  sys.exit(0)

if (uptime2() < 3600):
  #System has not been up for an hour yet
  print "NO"
  sys.exit(0)

lastTime = getDbTime(weedb)
if (lastTime == -1):
  print "NO"
  sys.exit(0)

t = time.time()
dt = datetime.now()
fmt = '%Y-%m-%d %H:%M:%S'
if (t - lastTime > 3600):
  print "YES"
  #has been more than 1 hour since db update
  #Touch file so that checkWeewxDaemon knows not to run
  #os.system('sudo touch /var/lib/weewx/updating')
  #os.system('sudo service weewx stop')
  #os.system('sudo wee_device --dump -y')
  #os.system('sudo wee_device --clear -y')
  #os.system('sudo service weewx start')
  #os.system('sudo rm /var/lib/weewx/updating')
  f = open('/home/pi/bmplogs/weewx_reset.log', 'a')
  #f.write('Reset started at '+ datetime.strftime(dt, fmt)+"; finished at "+datetime.strftime(datetime.now(), fmt)+"\n") 
  f.write('Reset started at '+ datetime.strftime(dt, fmt)+"\n")
  f.close()
else:
  print "NO"
