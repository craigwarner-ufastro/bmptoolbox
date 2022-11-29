-- MySQL dump 10.16  Distrib 10.1.41-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: mybmp
-- ------------------------------------------------------
-- Server version	10.1.41-MariaDB-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP DATABASE IF EXISTS `mybmp`;
CREATE DATABASE `mybmp`;
USE `mybmp`;

--
-- Table structure for table `costs`
--

DROP TABLE IF EXISTS `costs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `costs` (
  `cid` int(8) NOT NULL AUTO_INCREMENT,
  `uid` int(8) DEFAULT NULL,
  `trade` enum('Trade #1','Trade #3') DEFAULT NULL,
  `liner` decimal(5,3) DEFAULT NULL,
  `container` decimal(5,3) DEFAULT NULL,
  `substrate` decimal(5,3) DEFAULT NULL,
  `fillContainer` decimal(5,3) DEFAULT NULL,
  `planting` decimal(5,3) DEFAULT NULL,
  `tagging` decimal(5,3) DEFAULT NULL,
  `pruning` decimal(5,3) DEFAULT NULL,
  `spacing` decimal(5,3) DEFAULT NULL,
  `plantMovement` decimal(5,3) DEFAULT NULL,
  `fertTopdress` decimal(5,3) DEFAULT NULL,
  `irrigationPumping` decimal(5,4) DEFAULT NULL,
  `allocSpace` decimal(5,3) DEFAULT NULL,
  `costName` varchar(24) DEFAULT NULL,
  `fertCostPerPound` decimal(5,3) DEFAULT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `files` (
  `fid` int(8) NOT NULL AUTO_INCREMENT,
  `uid` int(8) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hourlyWeather`
--

DROP TABLE IF EXISTS `hourlyWeather`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hourlyWeather` (
  `hwid` int(8) NOT NULL AUTO_INCREMENT,
  `wsid` int(8) DEFAULT NULL,
  `uid` int(8) DEFAULT NULL,
  `hour` datetime DEFAULT NULL,
  `solar_radiation` decimal(7,3) DEFAULT NULL,
  `max_temp` decimal(6,3) DEFAULT NULL,
  `min_temp` decimal(6,3) DEFAULT NULL,
  `rain_in` decimal(6,3) DEFAULT NULL,
  PRIMARY KEY (`hwid`),
  UNIQUE KEY `wsid` (`wsid`,`hour`)
) ENGINE=MyISAM AUTO_INCREMENT=373595 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prefs`
--

DROP TABLE IF EXISTS `prefs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prefs` (
  `uid` int(8) DEFAULT NULL,
  `hires` tinyint(1) DEFAULT NULL,
  `plotWidth` int(8) DEFAULT NULL,
  `plotHeight` int(8) DEFAULT NULL,
  `plotbg` varchar(16) DEFAULT NULL,
  `plotfg` varchar(16) DEFAULT NULL,
  `autoRunTime` time DEFAULT NULL,
  `defaultZoneType` enum('ET','LF') NOT NULL DEFAULT 'ET'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `runs`
--

DROP TABLE IF EXISTS `runs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `runs` (
  `rid` int(8) NOT NULL AUTO_INCREMENT,
  `uid` int(8) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `ncomp` int(8) DEFAULT NULL,
  `defFile` tinyint(1) NOT NULL,
  `auto` tinyint(1) NOT NULL,
  `doy` varchar(8) DEFAULT NULL,
  `irrig` decimal(4,2) DEFAULT NULL,
  `yestIrrig` decimal(4,2) DEFAULT NULL,
  `threeAvgIrrig` decimal(4,2) DEFAULT NULL,
  `fiveMax` decimal(4,2) DEFAULT NULL,
  `autoRunTime` time DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=MyISAM AUTO_INCREMENT=116 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tracker`
--

DROP TABLE IF EXISTS `tracker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tracker` (
  `hid` int(8) NOT NULL AUTO_INCREMENT,
  `time` datetime DEFAULT NULL,
  `res` varchar(12) DEFAULT NULL,
  `browser` varchar(16) DEFAULT NULL,
  `os` varchar(16) DEFAULT NULL,
  `ip` varchar(16) DEFAULT NULL,
  `referrer` varchar(64) DEFAULT NULL,
  `page` varchar(32) DEFAULT NULL,
  `username` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`hid`)
) ENGINE=MyISAM AUTO_INCREMENT=1311922 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `uid` int(8) NOT NULL AUTO_INCREMENT,
  `username` varchar(32) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `firstName` varchar(32) DEFAULT NULL,
  `lastName` varchar(32) DEFAULT NULL,
  `affiliation` varchar(32) DEFAULT NULL,
  `email` varchar(64) DEFAULT NULL,
  `birthday` int(11) DEFAULT NULL,
  `role` int(8) DEFAULT NULL,
  `state` varchar(4) DEFAULT NULL,
  `country` varchar(64) DEFAULT NULL,
  `role_other` varchar(32) DEFAULT NULL,
  `cirrig` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `weather`
--

DROP TABLE IF EXISTS `weather`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weather` (
  `wid` int(8) NOT NULL AUTO_INCREMENT,
  `wsid` int(8) DEFAULT NULL,
  `uid` int(8) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `solar_radiation` decimal(6,3) DEFAULT NULL,
  `max_temp` decimal(6,3) DEFAULT NULL,
  `min_temp` decimal(6,3) DEFAULT NULL,
  `rain_in` decimal(6,3) DEFAULT NULL,
  `npoints` int(8) DEFAULT NULL,
  PRIMARY KEY (`wid`),
  UNIQUE KEY `wsid` (`wsid`,`date`)
) ENGINE=MyISAM AUTO_INCREMENT=15034 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `weatherStations`
--

DROP TABLE IF EXISTS `weatherStations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weatherStations` (
  `wsid` int(8) NOT NULL AUTO_INCREMENT,
  `uid` int(8) DEFAULT NULL,
  `location` varchar(32) DEFAULT NULL,
  `elevation_ft` decimal(6,2) DEFAULT NULL,
  `lattitude` decimal(6,3) DEFAULT NULL,
  `longitude` decimal(6,3) DEFAULT NULL,
  `weatherTime` varchar(8) DEFAULT NULL,
  `public` tinyint(1) DEFAULT NULL,
  `et_fac` decimal(4,2) DEFAULT NULL,
  PRIMARY KEY (`wsid`)
) ENGINE=MyISAM AUTO_INCREMENT=20 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `zoneHistory`
--

DROP TABLE IF EXISTS `zoneHistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zoneHistory` (
  `zhid` int(8) NOT NULL AUTO_INCREMENT,
  `zid` int(8) DEFAULT NULL,
  `uid` int(8) DEFAULT NULL,
  `histTime` datetime DEFAULT NULL,
  `pctCover` decimal(6,3) DEFAULT NULL,
  `plantHeight_in` decimal(6,3) DEFAULT NULL,
  `plantWidth_in` decimal(6,3) DEFAULT NULL,
  `containerSpacing_in` decimal(6,3) DEFAULT NULL,
  `spacing` enum('square','offset') DEFAULT NULL,
  `irrig_in` decimal(6,3) DEFAULT NULL,
  `irrig_minutes` decimal(6,3) DEFAULT NULL,
  `etc_in` decimal(6,3) DEFAULT NULL,
  `cf` decimal(6,3) DEFAULT NULL,
  `irrig_uniformity` decimal(6,3) DEFAULT NULL,
  `irrig_in_per_hr` decimal(5,3) DEFAULT NULL,
  `solar_radiation` decimal(6,3) DEFAULT NULL,
  `max_temp` decimal(6,3) DEFAULT NULL,
  `min_temp` decimal(6,3) DEFAULT NULL,
  `rain_in` decimal(6,3) DEFAULT NULL,
  `wsid` int(8) DEFAULT NULL,
  `deficit_in` decimal(6,3) DEFAULT NULL,
  `source` int(2) DEFAULT NULL,
  `ncycles` int(8) DEFAULT NULL,
  `rtlf_min` decimal(6,3) DEFAULT NULL,
  `etlf_in` decimal(6,3) DEFAULT NULL,
  `etratio` decimal(6,3) DEFAULT NULL,
  `et0_in` decimal(6,3) DEFAULT NULL,
  PRIMARY KEY (`zhid`),
  KEY `combo_idx` (`zid`,`uid`,`wsid`)
) ENGINE=MyISAM AUTO_INCREMENT=2423891 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `zones`
--

DROP TABLE IF EXISTS `zones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zones` (
  `zid` int(8) NOT NULL AUTO_INCREMENT,
  `uid` int(8) DEFAULT NULL,
  `zoneNumber` int(8) NOT NULL,
  `zoneName` varchar(32) DEFAULT NULL,
  `plant` varchar(32) DEFAULT NULL,
  `containerDiam_in` decimal(6,3) DEFAULT NULL,
  `irrigCaptureAbility` enum('low','medium','high','nil','negative') DEFAULT NULL,
  `irrig_in_per_hr` decimal(5,3) DEFAULT NULL,
  `irrig_uniformity` decimal(6,3) DEFAULT NULL,
  `plantHeight_in` decimal(6,3) DEFAULT NULL,
  `plantWidth_in` decimal(6,3) DEFAULT NULL,
  `pctCover` decimal(6,3) DEFAULT NULL,
  `containerSpacing_in` decimal(6,3) DEFAULT NULL,
  `spacing` enum('square','offset') DEFAULT NULL,
  `wsid` int(8) DEFAULT NULL,
  `lastChanged` datetime DEFAULT NULL,
  `priority` int(8) NOT NULL,
  `auto` tinyint(1) NOT NULL,
  `autoRunTime` time DEFAULT NULL,
  `external` varchar(32) DEFAULT NULL,
  `shade` decimal(6,3) DEFAULT NULL,
  `leachingFraction` decimal(6,3) DEFAULT NULL,
  `irrigSchedule` enum('daily','odd days','fixed days','threshold','none') DEFAULT NULL,
  `fixedDays` int(8) DEFAULT NULL,
  `minIrrig` int(8) DEFAULT NULL,
  `productionArea` enum('open field','shadecloth','plastic') DEFAULT NULL,
  `availableWater` decimal(6,3) DEFAULT NULL,
  `thresholdFactor` decimal(6,3) DEFAULT NULL,
  `lfTestDate` datetime DEFAULT NULL,
  `lfTestRuntime` decimal(5,2) DEFAULT NULL,
  `lfTestPct` decimal(5,2) DEFAULT NULL,
  `lfTargetPct` decimal(5,2) DEFAULT NULL,
  `irrig_gal_per_hr` decimal(5,3) DEFAULT NULL,
  `zoneType` enum('ET-sprinkler','ET-micro','LF-sprinkler','LF-micro') DEFAULT NULL,
  `ncycles` int(8) DEFAULT NULL,
  `hourly_rain_thresh_in` decimal(5,3) DEFAULT NULL,
  `weekly_rain_thresh_in` decimal(5,3) DEFAULT NULL,
  PRIMARY KEY (`zid`),
  UNIQUE KEY `uid` (`uid`,`zoneNumber`)
) ENGINE=MyISAM AUTO_INCREMENT=627 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-10-27  1:44:51
