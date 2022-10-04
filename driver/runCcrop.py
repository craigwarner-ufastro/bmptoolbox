#!/usr/bin/python
import os, sys

runName = ""
userPath = ""
for j in range(len(sys.argv)):

  if (sys.argv[j] == '-run'):
    if (len(sys.argv) > j+1):
      runName = sys.argv[j+1]
  if (sys.argv[j] == '-user'):
    if (len(sys.argv) > j+1):
      userPath = sys.argv[j+1]

if (runName == ''):
  print "No run defined!"
else:
  os.system('../driver/a.out < '+userPath+'run'+runName+'.txt')
  os.system('chmod g+w '+userPath+'*'+runName+'*')
