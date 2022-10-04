#!/usr/bin/python
#Script to archive irrigHistory logs by month
import os, sys, glob
from datetime import *
a = glob.glob('bmplogs/irrigHistory*.log')
a.sort()
now = datetime.today()
t = timedelta(days=1)
#subtract 1 day from today to get timestamp for last month on 1st of each month
yest = now - t
for j in range(len(a)):
  archname = 'bmplogs/'+yest.strftime("%b%Y")+'.'+(a[j].split('/'))[1]
  if (not os.access(archname, os.F_OK)):
    os.rename(a[j], archname)
