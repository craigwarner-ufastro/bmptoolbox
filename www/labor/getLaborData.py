#!/usr/bin/python
import time, os, sys, glob, socket
from datetime import datetime

#Format HTTP POST request 
html = ''
#http://www.floridajobs.org/labor-market-information/data-center/statistical-programs/occupational-employment-statistics-and-wages
#host = "www.labormarketinfo.com"
#page = "/Library/OES.htm"
host = "www.floridajobs.org"
page = "/labor-market-information/data-center/statistical-programs/occupational-employment-statistics-and-wages"
fp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
fp.connect((host, 80))
request = "GET " + page + " HTTP/1.0\r\n"
request += "Host: " + host + "\r\n"
request += "Referer: http://fawn.ifas.ufl.edu\r\n";
request += "User-Agent: PHP test client\r\n\r\n";
fp.send(request)

fp.settimeout(60)
#Read CSV response
sf = fp.makefile('rb')
try:
  html = sf.read()
except Exception:
  sys.exit()
sf.close()
fp.close()
startpos = html.find('/library/oes/est')
endpos = html.find('.xls"', startpos)+4
hostpos = html.rfind('//', 0, startpos)+2
xlshost = html[hostpos:startpos]
xlspath = html[startpos:endpos]
xlsfile = xlspath[xlspath.rfind('/')+1:]
csvfile = xlsfile[:-4]+'.csv'
if (os.access(xlsfile, os.F_OK)):
  print "File "+xlsfile+" exists."
  sys.exit()

fp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
fp.connect((xlshost, 80))
request = "GET " + xlspath + " HTTP/1.0\r\n"
request += "Host: " + xlshost + "\r\n"
request += "Referer: http://fawn.ifas.ufl.edu\r\n";
request += "User-Agent: PHP test client\r\n\r\n";
fp.send(request)
fp.settimeout(60)
#Read CSV response
sf = fp.makefile('rb')
try:
  html = sf.read()
except Exception:
  sys.exit()
sf.close()
fp.close()
startpos = html.find('Content-Length')
endpos = html.find('\n', startpos)+3
html = html[endpos:]

f = open(xlsfile, 'wb')
f.write(html)
f.close()

os.system('convertxls2csv -x '+xlsfile+' -c '+csvfile)
f = open(csvfile, 'rb')
laborData = f.read().split('\n')
f.close()

foundLine = False
i = 0
while (not foundLine):
  currLine = laborData[i].split(',')
  if (currLine[0] == '45-2099'):
    foundLine = True
  else:
    i+=1

if (not foundLine):
  print "ERROR: Could not find code 45-2099!"
  sys.exit()

f = open('currentLabor.txt','wb')
f.write(currLine[5]+"\n")
f.write(csvfile[:-4] + "\n")
f.write(datetime.today().strftime("%b %d, %Y"))
f.close()
