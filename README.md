# bmptoolbox
C-Irrig and CCROP toolkits, webpages, android apps, and arduino programs

BMPToolbox is a collection of Java software, webpages with backend database, android apps, an arduino program all loosely connected and developed by Tom Yeager, Jeff Million, and Craig Warner at the Univeristy of Florida IFAS for crop irrigation modeling.

## Table of contents:
This github contains 7 subfolders:

- android - this contains 3 apps: C-Irrig, C-IrrigLF, and WaterTips
  - CirrigLF - the CirrigLF app
  - WaterTips - the WaterTips app
  - cirrig-android-201609b - the latest version I have of the android version of the original C-Irrig app (I do not have the iPhone version)
- arduino - this contains the arduino code for WaterTips
- cirrig - this contains the bulk of our software repository in Java.  It includes:
  - GUIs
    - bmp.jec - BMPJEC GUI for the old BMP agent, still used by Saunders
    - cirrig.jec - CJEC GUI used for Cirrig Agent with automatic irrigation with PLCs
    - weather.jec - Old weather GUI used on Windows to read Davis Weather station download.txt file and upload it
  - Agents
    - bmpAgent - old BMP agent, our original one that queries the CCROP agent for irrigation values and can send them to PLC or .CSV file
    - ccropAgent - runs on the same server / machine as website and database and performs zone runs at scheduled times
    - cirrigAgent - runs on pi connected to PLC for self-contained irrigation system
    - weatherAgent - runs on same server / machine as website and database and collects weather from either weather.jec or weewxAgent
    - weewxAgent - runs on pi with weewx open source software and uploads weather and also talks to cirrigAgent for self-contained system
    - winAgent - old Windows agent used to perform software updates remotely
  - Libraries and Configuration files:
    - boot - startup and shutdown scripts
    - etc - configuration files
    - javaMMTLib - a java library needed by all agents and GUIs
    - javaUFLib - a java library needed by all agents and GUIs
    - javaUFProtocol - a java library needed by all agents and GUIs
    - lib - additional java libraries needed
    - scripts - helpful scripts
    - www - webpages for counter and irrigation histories that can be put on pis
- config - contains the database template and configuration scripts to set up the environment, database, and webserver
- docs - several PDFs with documentation
- driver - the original driver fortran program that we ran in 2007
- www - the entire collection of webpages from our server - this includes both the original pages and the cirrig pages, which are in the cirrig folder.  This requires mysql databases to exist to be able to run which I will describe in my documentation but can be used to set up a stand-alone system on any Linux machine with a webserver, including a pi, and that is exactly what we're doing with Saunders.
  - admin - admin pages for managing users
  - capturefactor - I think this was informational pages that you put together
  - cirrig - the "new" cirrig webpages, e.g. what comes up at http://www.bmptoolbox.org/cirrig/ (for that matter, use www.bmptoolbox.org/dirname for any directory name here to see what comes up)
  - data - saved user data from original webpages
  - fawn - FAWN weather data and historic weather data plus scripts to run that are still going to update from FAWN data feeds every day for original page
  - images - images
  - labor - an aborted project we had back in 2010 about labor data, not much here
  - source - a user manual and a password protected page that allows users to see source code

## Raspberry Pi Installation Guide:
Below follows a guide to installing the basics of the cirrig software on a raspberry pi for use as a self-contained irrigaiton unit with a raspberry pi, a cellular modem and cradlepoint router, an AutomationDirect DL-06 PLC, and a Davis Vantage Pro2 weather station.

### 1. Install rpi OS:
- Go to https://www.raspberrypi.com/software/ and download Raspberry Pi Imager software (available for Linux, Windows, and Mac) and watch their 45 second video to learn how to install Raspberry Pi OS.
- Click the Settings icon and click ‘Enable SSH’.  This will auto-check set username and password – enter a password.
- If necessary, format micro SD card and create FAT32 partition (usually not).
- Once the microSD card is written, insert mircoSD card into pi, connect keyboard, mouse, ethernet, monitor, and lastly power.

### 2. Configure Raspbian and install additional software. All commands with > are entered from a Terminal command prompt (Start Menu - Accessories - Terminal).
- Power on the pi - it will boot into a Windows-like desktop environment.
- Click the Start Menu – Preferences – Raspberry Pi Configuration to set up localization and time zone and reboot.
- Optionally change the password - Start Menu - Accessories - Terminal and then type: ```> passwd``` to change password using the passwd command.  It will ask you to confirm the current (default) password of raspberry then will ask you to enter a new password and confirm it.
- ```>  sudo apt-get update```
     - update repositories using this command 
- Download weewx – open the web browser, goto www.weewx.com or do a search for “weewx” and click on the Weewx home page.  Click on Download and then Released Versions and download the latest .deb file listed.  Current version (Nov 2022) is 4.9.1.  It will be saved to your Downloads directory.
- ```> sudo dpkg -i Downloads/python3-weewx_4.9.1-1_all.deb```
     - You will have a blue screen pop up to ask you for a location name (default Santa’s Workshop, North Pole), longitude and latitude, elevation, and weather station type (use Simulation if not hooked up to anything at the moment, Vantage otherwise).
     - It will then fail to install due to dependency issues but these will be resolved momentarily.
- ```>  sudo apt-get install python3-configobj python3-cheetah python3-pil python3-usb```
     - This will install the listed packages and weewx
- ```> sudo apt-get install git subversion tcsh vim vim-common vim-runtime vim-syntax-gtk telnet ntpdate sqlite3 mysql-common mariadb-client mariadb-server apache2 python3-pip default-jdk php php-gd php-mysql```
     - Hit ‘y’ and enter to confirm.  This will install numerous packages.

### 3. Setup mysql
- https://pimylifeup.com/raspberry-pi-mysql/
- ```> sudo mysql_secure_installation```
     - Hit ‘Y’ for all prompts.
- ```> sudo mysql -u root -p```
     - This will start mysql as the root (super user) for the first time.  You can create an additional user account as follows with the mysql queries:
```
CREATE USER 'pi'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'pi'@'localhost';
quit
```
- The last command (quit) will exit mysql back to a command prompt.  You may use any user other than pi if you desire.  Enter the actual password you want instead of ‘password’.
- You should then be able to: `> mysql -u pi -p` and enter the password you set up to get to a mysql prompt (type `quit` to exit).


### 4. Use config to initialize database and webserver
- `> cd /home/pi/bmptoolbox/config`
- `> ./setup_cshrc`
   - This will copy the default .cshrc file into your home directory
- `> ./setup_database.py`
   - Enter your MYSQL username and password and the database shell will be created and all scheduled scripts will be updated with the correct database information and installed.
- `> ./setup_webserver`
   - This will link the built-in webpages to your apache webserver in the folder /var/www/html (and backup anything existing as /var/www/html_old).
   - To undo this, run `> ./unset_webserver` at any time.


### 5. Setup config files
- (Optional) Setup `.cshrc` file - if for some reason the `./setup_cshrc` above does not work, you can manually set up that file:
  - `> cd`
  - `> nano .cshrc`
  - This changes to your home directory uses the “nano” editor to create a file .cshrc which is a global configuration file. Paste the following into the file and then hit CTRL+x to save.  Hit ‘y’ and enter to confirm and then enter again to accept the filename as .cshrc.
```
setenv BMPINSTALL /home/pi/bmpinstall
setenv UFMMTINSTALL /home/pi/bmpinstall
set autolist on

if (-e ${BMPINSTALL}/.ufcshrc) source ${BMPINSTALL}/.ufcshrc
set os = `/bin/uname`

alias ls 'ls --color=auto'
alias grep 'grep --color=auto'
alias fgrep 'fgrep --color=auto'
alias egrep 'egrep –color=auto'
```
- `> sudo nano /etc/hosts`
   - Again use the nano editor, this time to edit the file /etc/hosts.  Change raspberrypi in the line starting 127.0.0.1 to whatever you want the host name to be, e.g. pi-uf, pi-battery, pi-ctlf.
   - Hit CTRL+x to save, then confirm and accept the filename as /etc/hosts.
- `> sudo nano /etc/hostname`
   - Again use the nano editor, this time to edit the file /etc/hostname.  Change raspberrypi to the same hostname you selected in the previous step.
   - Hit CTRL+x to save, then confirm and accept the filename as /etc/hostname.
- `> sudo /etc/init.d/hostname.sh`
   - This causes the changes you just made to take effect.  Don’t worry if you see the message “sudo: unable to resolve host raspberrypi”
   - At this point it is a good idea to reboot again.

### 6. After reboot, install BMPToolbox software:
- `> tcsh`
   - Enter tcsh mode.  You’ll see the prompt change.  You can type `> echo $BMPINSTALL` and you should see it reply `/home/pi/bmpinstall`.  If so, you have your .cshrc file correct.
- `> git clone https://github.com/craigwarner-ufastro/bmptoolbox.git`
   - This will check out the BMPToolbox software to the directory /home/pi/bmptoolbox
- `> cd bmptoolbox/cirrig`
   - Change into the bmptoolbox/cirrig directory
- `> make init`
   - Perform setup for install
- `> make install`
   - Perform the actual install.  This will take a few minutes.
- `> source .ufcshrc`
- `> rehash`
   - Make Linux see all the new software that has been installed
- `> ufbmpstop -l`
   - Test if everything installed properly.  If so, you will get `Currently running agents and servers: UID        PID  PPID  C STIME TTY          TIME CMD` spread across 2 lines.  If not, you will get `ufbmpstop: Command not found.`


### 7. Configure BMPToolbox software
- `> cd /home/pi/bmptoolbox/cirrig/scripts/`
- `> ./patchWeewx.sh`
   - These two command will change directories and then run the script that patches the weewx startup script to only start if the internet is connected.
- `> mkdir /home/pi/bmplogs`
   - Create directory for log files

### 8. Set up a weather station (optional)
- If starting a new weather station config on an existing pi, make sure you are using tcsh as your shell and stop any currently running weewx by typing
  - `> tcsh`
  - `> sudo service weewx stop`
- If configuring a new weather station on an existing pi, run the weewx config tool.  Note: this can be skipped if you entered the station name, longitude, latitude, type, etc when installing weewx in section 2.
- `> sudo wee_config --reconfigure`
   - You will be prompted for a name for the weather station (e.g. CLTF), then an altitude (number then a comma then units, e.g. 30, meter).  Then you'll be asked for longitude and latitude, us or metric units (us), and finally the driver to use. Select the number next to Vantage.  You will also select the default port - /dev/usb0.
- `> sudo wee_device --set-interval=300`
   - Will set the default interval to 300 seconds (5 minutes) if it is set to a different value.
- Start weewx:
  - `> sudo service weewx start`
  - Then wait until the next 5 minute interval has finished and check that it is working:
  - `> sudo service weewx status`
  - If its working, the message should have the status as running and should list `manager: added record` at the most recent 5 minute interval:
```
pi@pi-uf:~ $ sudo service weewx status
● weewx.service - LSB: weewx weather system
 Loaded: loaded (/etc/init.d/weewx)
 Active: active (running) since Fri 2018-06-08 15:12:17 EDT; 1 months 16 days ago
 Process: 1612 ExecStart=/etc/init.d/weewx start (code=exited, status=0/SUCCESS)
 CGroup: /system.slice/weewx.service
 └─1638 python /usr/bin/weewxd --daemon –pidfile=/var/run/weewx.pi…

Jul 26 00:25:16 pi-uf weewx[1638]: manager: added record 2018-07-26 00:25:0...b'
Jul 26 00:25:16 pi-uf weewx[1638]: manager: added record 2018-07-26 00:25:0...b'
```
  - If it is running but you don’t see any messages about adding records, its possible there’s a clock discrepancy between the weather station unit and the weewx software.  In this case, you want to dump all the memory of the weather station and then clear it and everything should then restart normally.
    - `> sudo service weewx stop`
    - `> sudo wee_device --dump`
    - `> sudo wee_device --clear`
    - `> sudo service weewx start`
- Once weewx is running, you’ll want to configure the weewx agent:
  - `> ufWeewxConfig`
  - Enter your username, password, and either a weather station name or id to configure the weewx agent.


### 9. Set up scheduled tasks
- `> crontab -e`
   - Edit the “crontab”, which is the automatic task scheduler.  Press 1 (or whatever number is next to nano) and enter to use nano as your text editor for setting up the crontab.  Scroll down to the bottom of the file and paste the following 4 lines:
```
0 0 1 * * /home/pi/bmpinstall/bin/archiveIrrigHis.py
* * * * * /home/pi/bmpinstall/bin/checkCirrigPlc
* * * * * /home/pi/bmpinstall/bin/checkWeewxAgent
* * * * * /home/pi/bmpinstall/bin/checkWeewxDaemon
```
   - Hit CTRL+x to save, hit ‘y’ and enter to accept changes and hit enter to accept the filename given.
- `> crontab -l`
   - You should see the same 4 lines you just entered at the bottom of the response to confirm that you properly saved the crontab.  The lines mean that on the 1st day of every month at midnight (hour = 0, minute = 0), archiveIrrigHis.py will run – this archives the irrigHistory logs.  And at every minute of every day, it will run scripts to check the status of the cirrigAgent, weewxAgent, and weewx program itself and make sure they are all running.
   - Reboot the pi and you are good to go!
