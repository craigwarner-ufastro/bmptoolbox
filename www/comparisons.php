<?php session_start(); ?>
<html>
<head>
<title>Comparison Tools</title>
<?php require('header.php'); ?> 
<h1 class="h1-container">Comparison Tools<font color="#ffffff" class="h1-shadow">Comparison Tools</font></h1>
<b>The following comparison tools will allow you to run 'what-if' experiments to evaluate several levels of a given production practice.</b>
<ul>
<li><a href="comparison_fert.php">Fertilizer Rate Comparison Tool</a>
<br>Allows you to compare 2-4 different fertilizer rates using the same fertilizer.
<br><br>
<li><a href="comparison_irr.php">Irrigation Schedule Comparison Tool</a>
<br>Allows you to compare 1-2 fixed irrigation schedules to an ET-based irrigation schedule.
<br><br>
<li><a href="comparison_fert_irr.php">Fertilizer and Irrigation Comparison Tool</a>
<br>Allows you to compare the response of two fertilizer rates to two irrigation schedules (one of which is an ET-based schedule).
<br><br>
<li><a href="comparison_date.php">Plant Date Comparison Tool</a>
<br>Allows you to compare 2-4 different plant dates.
<br><br>
<li><a href="comparison_location.php">Location Comparision Tool</a>
<br>Allows you to compare 2-4 different locations.
</ul>
<?php require('footer.php'); ?>
