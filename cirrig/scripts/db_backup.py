#!/usr/bin/python
import os, sys, time, glob
weekly = False 
daily = False
deleteOld = False
dirname = "backups/"

for j in range(len(sys.argv)):
  if (sys.argv[j] == "-delete"):
    deleteOld = True
  if (sys.argv[j] == "-weekly"):
    weekly = True
    daily = False
  if (sys.argv[j] == "-daily"):
    daily = True
    weekly = False
  if (sys.argv[j] == "-dir"):
    dirname = sys.argv[j+1]

mysqlpfix = dirname
if (mysqlpfix[-1] != '/'):
  mysqlpfix += '/'
mysqlpfix += "mybmp-"
if (daily):
  mysqlpfix += "daily-"
elif (weekly):
  mysqlpfix += "weekly-"

mysqlfname = mysqlpfix+time.strftime("%m-%d-%Y")+'.sql'
os.system('mysqldump -u mybmpmysqluser -pmybmpmysqlpass mybmp > '+mysqlfname)

if (deleteOld):
  a = glob.glob(mysqlpfix+'*.sql')
  a.sort()
  for j in range(len(a)):
    if (a[j] != mysqlfname):
      os.unlink(a[j])
