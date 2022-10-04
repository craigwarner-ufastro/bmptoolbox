<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<?php require('header.php'); ?> 
<h1 class="h1-container">Container Crop Toolbox<font color="#ffffff" class="h1-shadow">Container Crop Toolbox</font></h1>
Tools to help growers and grower-advisers optimize production, profitability, and conservation of resources in container nurseries. These simulation tools are primarily directed to producing ornamental plants in small (trade #1-3) containers with overhead, sprinkler irrigation. Check out <a href="model.php">CCROP model concepts</a> for more information.

<br>
<table border=0 align="center" width=100%>
<tr>
<td><img src="images/home-img1.png" width=250></td>
<td><img src="images/home-img2.png" width=250></td>
<td><img src="images/home-img3.png" width=250></td>
</tr>
</table>
<ul>
<span class="bigredhead">
4 CCROP tools to choose from:
</span>
<ol>
<br>
(1) <a href="driver_grower.php">GROWER TOOL</a>: This tool is streamlined for evaluating practices which have a major impact on profitability and resource conservation. The GROWER tool runs a simulation based on one set of input conditions. The output is more detailed and includes daily time-plots as well as summary plots and tables. 
<br><br>
(2) <a href="comparisons.php">COMPARISON TOOLS</a>: This set of tools allows the user to run 'what-if' experiments to evaluate several levels of a given production practice. In this version, simulations are run to compare two or more levels of a given factor or practice. For example, the user can evaluate the effect of four different fertilizer rates or the effect of three different irrigation schedules. For these comparisons, all other production practices selected by the user are held constant. The output includes summary plots and tables (no daily time-plots). 
<br><br>
(3) <a href="realtime.php">REAL-TIME IRRIGATION TOOL</a>: Tracks the day-to-day progress of a crop providing the user with a recommended amount of irrigation water to apply each day.
<br><br>
(4) <a href="driver_tech.php">TECHNICAL TOOL</a>: For technical users who want to be able to change any input parameter in the CCROP model. Output is in metric units.
</ol>
<br>
<li>Users select critical management practices and submit these inputs to be run by CCROP using historical weather data for the location selected.  Outcomes, which can be viewed in graphical or tabular form, include plant growth as well as nutrient and water-related parameters.
<br><br>
<li>The site is free-of-charge but a <a href="register.php">login account</a> is required. The account can be used to store and manage tool simulations.
</ul>
<br><br>
<center>
<br>
<?php require('footer.php'); ?>
