#!/usr/bin/python
import os, glob, sys

dir1 = sys.argv[1]
dir2 = sys.argv[2]

a = glob.glob(dir1+'/*.php')
a.sort()

for j in range(len(a)):
  print a[j]
  x = os.system('diff '+a[j]+' '+dir2+a[j][a[j].rfind('/'):])
  print "----------------------------------\n\n\n\n\n"
