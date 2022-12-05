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
