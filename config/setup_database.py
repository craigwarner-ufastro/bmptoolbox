#!/usr/bin/python
from __future__ import print_function
if hasattr(__builtins__, 'raw_input'):
    input = raw_input
import os
import sys
import time

sqluser = None 
sqlpass = None 

if (len(sys.argv) > 1 and (sys.argv[1] == "-h" or sys.argv[1] == "--help")):
    print ("Usage: setup_database.py")
    print ("\tWill prompt for username and password and install mybmp mysql database")
    print ("\tand update scripts with username and password.")

sqluser = input("Enter mysql username: ")
if (sqluser is None):
    print ("Error> Username not specified")
    sys.exit(0)
sqlpass = input("Enter mysql password: ")
if (sqlpass is None):
    print ("Error> Password not specified")
    sys.exit(0)

file_path = os.path.realpath(__file__)
file_root = file_path[:file_path.rfind('config/')]

if (os.access('/var/lib/mysql/mybmp', os.F_OK)):
    print ("Backing up any existing mybmp database to current directory...")
    mysqlfname = 'mybmp_backup-'+time.strftime("%m-%d-%Y")+'.sql'
    os.system('mysqldump -u '+sqluser+' -p'+sqlpass+' mybmp > '+mysqlfname)

print ("Installing mybmp database...")
os.system('mysql -u '+sqluser+' -p'+sqlpass+' < '+file_root+'/config/mybmp_db_nodata.sql') 

filesToUpdate = ['cirrig/boot/checkCCrop', 'cirrig/boot/checkWeather', 'cirrig/scripts/db_backup.py', 'www/dbcnx.php']

print ("Updating scripts...")
for fn in filesToUpdate:
    f = open(file_root+'/'+fn, 'r')
    x = f.read()
    f.close()
    if (x.count('mybmpmysqluser') > 0):
        x = x.replace('mybmpmysqluser', sqluser)
    if (x.count('mybmpmysqlpass') > 0):
        x = x.replace('mybmpmysqlpass', sqlpass)
    f = open(file_root+'/'+fn, 'w')
    f.write(x)
    f.close()

print ("Installing new scripts...")
os.system('cd '+file_root+'/cirrig/boot; '+file_root+'/config/install.sh') 
os.system('cd '+file_root+'/cirrig/scripts; '+file_root+'/config/install.sh')
