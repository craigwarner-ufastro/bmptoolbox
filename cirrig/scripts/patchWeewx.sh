#!/bin/tcsh
set ispatched = `grep INET /etc/init.d/weewx`
if ("$ispatched" != "") then
  #Already patched, do nothing
  exit(0)
endif
sudo sed -n -i -e '/start-stop-daemon --start/r weewx_patch' -e 1x -e '2,${x;p}' -e '${x;p}' /etc/init.d/weewx 
