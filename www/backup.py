#!/usr/bin/python
import os, sys
destdir = sys.argv[1]

if (os.access(destdir, os.F_OK)):
  os.system('cp *.php '+destdir)
  os.system('cp *.txt '+destdir)
  os.system('cp *.css '+destdir)
  os.system('cp *.js '+destdir)
  os.system('cp *.plt '+destdir)
  os.system('cp *.sfn '+destdir)
