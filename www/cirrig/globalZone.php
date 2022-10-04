<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  function displaySchedule(sched) {
    var fixedDay = document.getElementById("fixedDay");
    if (sched == 2) fixedDay.style.display=""; else fixedDay.style.display="none";
    var thresh = document.getElementById("thresh");
    if (sched == 3) {
      availWater.style.display="";
      thresh.style.display="";
    } else {
      availWater.style.display="none";
      thresh.style.display="none";
    }
    if (sched == 3 || sched == 4) {
      minRunTime.style.display="none";
    } else {
      minRunTime.style.display="";
    }
  }

  function displayProdArea(area) { 
    var shade = document.getElementById("shade");
    if (area == 0) shade.style.display="none"; else shade.style.display="";
  }

  function updateStatus() {
    var enableTime = document.globalzone.enableTime.checked;
    document.globalzone.autoHour.disabled = !enableTime;
    document.globalzone.autoMin.disabled = !enableTime;
    document.globalzone.autoAmPm.disabled = !enableTime;

    var enableIrrig = document.globalzone.enableIrrig.checked;
    for (j = 0; j < document.globalzone.irrigSched.length; j++) {
      document.globalzone.irrigSched[j].disabled = !enableIrrig;
    }
    document.globalzone.day0.disabled = !enableIrrig;
    document.globalzone.day1.disabled = !enableIrrig;
    document.globalzone.day2.disabled = !enableIrrig;
    document.globalzone.day3.disabled = !enableIrrig;
    document.globalzone.day4.disabled = !enableIrrig;
    document.globalzone.day5.disabled = !enableIrrig;
    document.globalzone.day6.disabled = !enableIrrig;
    document.globalzone.availableWater.disabled = !enableIrrig;
    document.globalzone.thresholdFactor.disabled = !enableIrrig;
    document.globalzone.minIrrig.disabled = !enableIrrig;

    var enableProd = document.globalzone.enableProd.checked;
    for (j = 0; j < document.globalzone.productionArea.length; j++) {
      document.globalzone.productionArea[j].disabled = !enableProd;
    }
    document.globalzone.shade.disabled = !enableProd;

    var enableLF = document.globalzone.enableLF.checked;
    document.globalzone.leaching_fraction.disabled = !enableLF;
  }
</script>
<?php require('header.php'); ?>
<h2><center>Global zone settings</center></h2>
<?php
  if ($isLoggedIn) {
    $ws = Array();
    $sql = "select * from weatherStations where uid = " . $_SESSION['bmpid'];
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$ws[] = $row;
      }
    }
?>
<center>Select the checkbox next to an option to enable it.  Only selected options will be applied globally.</center>
<form name="globalzone" action="myaccount.php" method="post" onSubmit='return check();'>
<input type="hidden" name="mode" value="globalzone">
<div style="margin-left: 100px;">
<table border=0 align="left" cellspacing=8>
<tr>
  <td><b>Apply to:</b></td>
  <td><select name="global">
  <option name="all" value="-1">All Zones
  <?php
    for ($j = 0; $j < count($ws); $j++) {
      echo '<option name="' . $ws[$j]['location'] . '" value="' . $ws[$j]['wsid'] . '">' . $ws[$j]['location'];
    }
  ?>
  </select></td>
</tr>

<tr>
  <td valign="top"><input type="checkbox" name="enableTime" onClick="updateStatus();"><b>Time to Run:</b></td>
  <td>
    <select name="autoHour" disabled>
    <?php
      for ($h = 1; $h <= 12; $h++) {
	echo '<option name="' . $h . '" value="' . $h . '"';
        if ($h == 5) echo " selected";
	echo '>' . $h . "\n";
      }
    ?>
    </select> : <select name="autoMin" disabled>
    <?php
      for ($m = 0; $m < 60; $m++) {
        echo '<option name="' . $m . '" value="' . $m . '"';
        if ($m == 15) echo " selected";
        echo '>';
        if ($m < 10) echo '0';
        echo  $m . "\n";
      }
    ?>
    </select>
    <select name="autoAmPm" disabled>
      <option name="am" value="am" selected>am
      <option name="pm" value="pm">pm
    </select>
  </td>
</tr>

<tr>
  <td valign="top"><input type="checkbox" name="enableIrrig" onClick="updateStatus();"><b>Irrigation Schedule:</b></td>
  <td>
    <input type="radio" disabled name="irrigSched" value="daily" checked onClick="displaySchedule(0);">Daily (<a href="javascript:popupText(dailytext);" onMouseOver="if (!IsIE()) ddrivetip(dailytext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" disabled name="irrigSched" value="odd days" onClick="displaySchedule(1);">Odd Days (<a href="javascript:popupText(odd_days_text);" onMouseOver="if (!IsIE()) ddrivetip(odd_days_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" disabled name="irrigSched" value="fixed days" onClick="displaySchedule(2);">Fixed Days (<a href="javascript:popupText(fixed_days_text);" onMouseOver="if (!IsIE()) ddrivetip(fixed_days_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" disabled name="irrigSched" value="threshold" onClick="displaySchedule(3);">Threshold (<a href="javascript:popupText(threshold_text);" onMouseOver="if (!IsIE()) ddrivetip(threshold_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" disabled name="irrigSched" value="none" onClick="displaySchedule(4);">None<br>
  </td>
</tr>

<tr id="fixedDay" style="display:none;">
  <td>Fixed Day Scheudle:</td>
  <td colspan=5>
  <?php
    $days = array("Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat");
    for ($j = 0; $j < count($days); $j++) {
      echo ' ' . $days[$j] . '<input type="checkbox" disabled name="day' . $j . '" value="' . (pow(2, $j)) . '">';
    }
  ?>
  </td>
</tr>

<tr id="availWater" style="display:none">
  <td>Available Water:</td>
  <td><input type="text" disabled size=4 name="availableWater" id="availableWater" onkeypress="return validDec(event);" value="25"> %</td>
</tr>

<tr id="thresh" style="display:none">
  <td>Threshold Factor:</td>
  <td><input type="text" disabled size=4 name="thresholdFactor" id="thresholdFactor" onkeypress="return validDec(event);" value="40"> %</td>
</tr>
</div>

<tr id="minRunTime">
  <td>Min Run Time (minutes):</td>
  <td><select name="minIrrig" disabled>
  <?php
    for ($j = 0; $j <= 60; $j++) {
      echo '<option name="' . $j . '" value="' . $j . '">' . $j;
    }
  ?>
  </select></td>
</tr>

<tr>
  <td valign="top"><input type="checkbox" name="enableProd" onClick="updateStatus();"><b>Production Area:</b></td>
  <td>
    <input type="radio" disabled name="productionArea" value="open field" checked onClick="displayProdArea(0);">Open Field<br>
    <input type="radio" disabled name="productionArea" value="shadecloth" onClick="displayProdArea(1);">Shadecloth<br>
    <input type="radio" disabled name="productionArea" value="plastic" onClick="displayProdArea(2);">Plastic<br>
  </td>
</tr>
    
<tr id="shade" style="display:none">
  <td>Shade (% light reduction):</td>
  <td><input type="text" disabled size=4 name="shade" id="shade" onkeypress="return validDec(event);" value="0"> %</td>
</tr>

<tr>
  <td><input type="checkbox" name="enableLF" onClick="updateStatus();"><b>Leaching fraction (LF):</b></td>
  <td><input type="text" disabled size=4 name="leaching_fraction" id="leaching_fraction" onkeypress="return validDec(event);" value="10"> %</td>
</tr>

<tr>
  <td></td>
  <td><br><br><input type="submit" name="submit" value="Apply Settings">
  <input type="reset" name="reset" value="Reset">
  </td>
</tr>
</table>
</div>
</form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to edit a zone.';
  }
?>
<?php require('footer.php'); ?>
