<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  var currDateField = null;

  function updateLFCalendar(cal, dateField, anchor) {
    currDateField = dateField;
    var selDate = dateField.value;
    var startDate = new Date();
    var endDate = new Date();
    var sd = new Date();
    var ed = new Date();
    ed.setDate(ed.getDate()+1);
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(ed,"yyyy-MM-dd"), null);
    //cal.addDisabledDates(null, formatDate(sd,"yyyy-MM-dd"));
    cal.showCalendar(anchor, selDate);
    return false;
  }

  function setLFCalendarVals(y,m,d) {
    currDateField.value = ""+y+"-"+m+"-"+d;
  }

  function updateUnits(inform, outid, factor) {
    var outform = document.getElementById(outid);
    var invalue = parseFloat(inform.value);
    outform.value = invalue*factor;
  }

  function check() {
    var a = true;
    var i = 0;
    var x = "";
    for (i = 0; i < document.editzone.elements.length; i++) {
      if (document.editzone.elements[i].name == 'external') continue;
      if (document.editzone.elements[i].name == 'priority') continue;
      if (document.editzone.elements[i].value == '') {
        //check for hidden element
        if (document.createzone.elements[i].parentNode.style.display == "none" || document.createzone.elements[i].parentNode.parentNode.style.display == "none") continue;
	alert("You must enter a value for all fields.");
	return false;
     }
    }
    return a;
  }

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

  function setZoneType(zoneType) {
    if (zoneType == "ET-sprinkler") {
      document.getElementById("threshold").style.display="";
      document.getElementById("irrig_in_per_hr_td").style.display="";
      document.getElementById("irrig_cm_per_hr_td").style.display="";
      document.getElementById("irrig_gal_per_hr_td").style.display="none";
      document.getElementById("irrig_cm3_per_min_td").style.display="none";
      document.getElementById("irrig_capture_tr").style.display="";
      document.getElementById("leaching_fraction_tr").style.display="";
      document.getElementById("lf_input_label_tr").style.display="none";
      document.getElementById("lf_test_date_tr").style.display="none";
      document.getElementById("lf_test_runtime_tr").style.display="none";
      document.getElementById("lf_test_pct_tr").style.display="none";
      document.getElementById("lf_target_pct_tr").style.display="none";
      document.getElementById("infrequent_label_tr").style.display="";
      document.getElementById("pctCover_tr").style.display="";
      document.getElementById("plantHeight_tr").style.display="";
      document.getElementById("plantWidth_tr").style.display="";
      document.getElementById("contSpacing_tr").style.display="";
      document.getElementById("spacingArr_tr").style.display="";
    } else if (zoneType == "ET-micro") {
      document.getElementById("threshold").style.display="";
      document.getElementById("irrig_in_per_hr_td").style.display="none";
      document.getElementById("irrig_cm_per_hr_td").style.display="none";
      document.getElementById("irrig_gal_per_hr_td").style.display="";
      document.getElementById("irrig_cm3_per_min_td").style.display="";
      document.getElementById("irrig_capture_tr").style.display="none";
      document.getElementById("leaching_fraction_tr").style.display="";
      document.getElementById("lf_input_label_tr").style.display="none";
      document.getElementById("lf_test_date_tr").style.display="none";
      document.getElementById("lf_test_runtime_tr").style.display="none";
      document.getElementById("lf_test_pct_tr").style.display="none";
      document.getElementById("lf_target_pct_tr").style.display="none";
      document.getElementById("infrequent_label_tr").style.display="";
      document.getElementById("pctCover_tr").style.display="";
      document.getElementById("plantHeight_tr").style.display="";
      document.getElementById("plantWidth_tr").style.display="";
      document.getElementById("contSpacing_tr").style.display="";
      document.getElementById("spacingArr_tr").style.display="";
    } else if (zoneType == "LF-sprinkler") {
      document.getElementById("threshold").style.display="none";
      document.getElementById("irrig_in_per_hr_td").style.display="";
      document.getElementById("irrig_cm_per_hr_td").style.display="";
      document.getElementById("irrig_gal_per_hr_td").style.display="none";
      document.getElementById("irrig_cm3_per_min_td").style.display="none";
      document.getElementById("irrig_capture_tr").style.display="none";
      document.getElementById("leaching_fraction_tr").style.display="none";
      document.getElementById("lf_input_label_tr").style.display="";
      document.getElementById("lf_test_date_tr").style.display="";
      document.getElementById("lf_test_runtime_tr").style.display="";
      document.getElementById("lf_test_pct_tr").style.display="";
      document.getElementById("lf_target_pct_tr").style.display="";
      document.getElementById("infrequent_label_tr").style.display="none";
      document.getElementById("pctCover_tr").style.display="none";
      document.getElementById("plantHeight_tr").style.display="none";
      document.getElementById("plantWidth_tr").style.display="none";
      document.getElementById("contSpacing_tr").style.display="none";
      document.getElementById("spacingArr_tr").style.display="none";
    } else if (zoneType == "LF-micro") {
      document.getElementById("threshold").style.display="none";
      document.getElementById("irrig_in_per_hr_td").style.display="none";
      document.getElementById("irrig_cm_per_hr_td").style.display="none";
      document.getElementById("irrig_gal_per_hr_td").style.display="";
      document.getElementById("irrig_cm3_per_min_td").style.display="";
      document.getElementById("irrig_capture_tr").style.display="none";
      document.getElementById("leaching_fraction_tr").style.display="none";
      document.getElementById("lf_input_label_tr").style.display="";
      document.getElementById("lf_test_date_tr").style.display="";
      document.getElementById("lf_test_runtime_tr").style.display="";
      document.getElementById("lf_test_pct_tr").style.display="";
      document.getElementById("lf_target_pct_tr").style.display="";
      document.getElementById("infrequent_label_tr").style.display="none";
      document.getElementById("pctCover_tr").style.display="none";
      document.getElementById("plantHeight_tr").style.display="none";
      document.getElementById("plantWidth_tr").style.display="none";
      document.getElementById("contSpacing_tr").style.display="none";
      document.getElementById("spacingArr_tr").style.display="none";
    }
  }
</script>
<?php require('header.php'); ?>
<script language+"javascript">
  cal.setReturnFunction("setLFCalendarVals");
</script>
<h2><center>Edit zone</center></h2>
<?php
  if (isset($_GET['zid']) && is_numeric($_GET['zid'])) {
    $zid = (int)$_GET['zid'];
  } else {
    $zid = -1;
  }

  if (!$isLoggedIn && !$doPreview) {
    $zid = -1;
    echo 'You must <a href="login.php">login</a> to view your zone histories.';
    exit(0);
  }

  if ($isLoggedIn) $uid = $_SESSION['bmpid']; else $uid=39;

  if (($isLoggedIn || $doPreview) && isset($_GET['zid'])) {
    $ws = Array();
    $sql = "select * from weatherStations where uid = $uid OR public=true";
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$ws[] = $row;
      }
    }
    $sql = "select * from zones where zid = $zid";
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $zone = $row;
        if ($zone['availableWater'] == NULL) $zone['availableWater'] = '0';
	if ($zone['thresholdFactor'] == NULL) $zone['thresholdFactor'] = '0';
        if ($zone['irrig_gal_per_hr'] == NULL) $zone['irrig_gal_per_hr'] = '0';
        if ($zone['lfTestDate'] == NULL) $zone['lfTestDate'] = date("Y-m-d H:i:s");
        if ($zone['lfTestRuntime'] == NULL) $zone['lfTestRuntime'] = '0';
        if ($zone['lfTestPct'] == NULL) $zone['lfTestPct'] = '0';
        if ($zone['lfTargetPct'] == NULL) $zone['lfTargetPct'] = '0';
        $datearr = strptime($zone['lfTestDate'], "%Y-%m-%d %H:%M:%S");
        $lfTestDay = explode(' ', $zone['lfTestDate']);
        $lfTestDay = $lfTestDay[0];
      }
    }
?>

<form name="editzone" action="myaccount.php" method="post" onSubmit='return check();'>
<input type="hidden" name="mode" value="editzone">
<input type="hidden" name="zid" value="<?php echo $zone['zid']; ?>">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
  <td valign="top">Zone Name:</td>
  <td valign="top" align="left"><input type="text" size=15 name="zoneName" value="<?php echo $zone['zoneName'];?>"></td>

  <td valign="top" width=25></td>
  <td valign="top">Zone Number:</td>
  <td valign="top" align="left"><input type="text" size=3 name="zoneNumber" onkeypress="return validNum(event);" value="<?php echo $zone['zoneNumber'];?>"></td>
</tr>

<tr>
  <td>Plant Name:</td>
  <td><input type="text" size=15 name="plant" value="<?php echo $zone['plant'];?>"></td>

  <td></td>
  <td>Zone Type:</td>
  <td valign="top" align="left"><select name="zoneType" onChange="setZoneType(this.options[this.selectedIndex].value);">
  <?php
    foreach($zoneTypes as $key=>$value) {
      echo "<option name=\"$value\" value=\"$value\"";
      if ($zone['zoneType'] == $value) echo " selected";
      echo ">" . $value;
    }
  ?>
  </select>
  </td>
</tr>

<tr>
  <td>External<br>Reference:</td>
  <td><input type="text" size=15 name="external" value="<?php echo $zone['external'];?>"></td>
</tr>

<tr>
  <td align="left" colspan=6><br><br><b>Irrigation Inputs</b><br></td>
</tr>

<tr>
  <td valign="top">Irrigation Schedule:</td>
  <td>
    <input type="radio" name="irrigSched" value="daily" <?php if ($zone['irrigSchedule'] == "daily") echo 'checked';?> onClick="displaySchedule(0);">Daily (<a href="javascript:popupText(dailytext);" onMouseOver="if (!IsIE()) ddrivetip(dailytext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" name="irrigSched" value="odd days" <?php if ($zone['irrigSchedule'] == "odd days") echo 'checked';?> onClick="displaySchedule(1);">Odd Days (<a href="javascript:popupText(odd_days_text);" onMouseOver="if (!IsIE()) ddrivetip(odd_days_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
    <input type="radio" name="irrigSched" value="fixed days" <?php if ($zone['irrigSchedule'] == "fixed days") echo 'checked';?> onClick="displaySchedule(2);">Fixed Days (<a href="javascript:popupText(fixed_days_text);" onMouseOver="if (!IsIE()) ddrivetip(fixed_days_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
<!-- only display threshold for ET micro and ET sprinkler -->
    <div id="threshold"><input type="radio" name="irrigSched" value="threshold" <?php if ($zone['irrigSchedule'] == "threshold") echo 'checked';?> onClick="displaySchedule(3);">Threshold (<a href="javascript:popupText(threshold_text);" onMouseOver="if (!IsIE()) ddrivetip(threshold_text,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br></div>
    <input type="radio" name="irrigSched" value="none" <?php if ($zone['irrigSchedule'] == "none") echo 'checked';?> onClick="displaySchedule(4);">None (<a href="javascript:popupText(nonetext);" onMouseOver="if (!IsIE()) ddrivetip(nonetext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>
  </td>
</tr>

<tr id="fixedDay" <?php if ($zone['irrigSchedule'] != 'fixed days') echo 'style="display:none"';?>>
  <td>Fixed Day Scheudle:</td>
  <td colspan=5>
  <?php
    $dayset = array();
    $fx = $zone['fixedDays'];
    for ($j = 6; $j >= 0; $j--) {
      if ($fx >= pow(2,$j)) {
	//this day is set, e.g Sat = 64, Fri = 32
	$dayset[$j] = true;
	$fx -= pow(2,$j);
      } else $dayset[$j] = false;
    }
    $days = array("Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat");
    for ($j = 0; $j < count($days); $j++) {
      echo ' ' . $days[$j] . '<input type="checkbox" name="day' . $j . '" value="' . (pow(2, $j)) . '"';
      if ($dayset[$j]) echo ' checked';
      echo '>';
    }
  ?>
  </td>
</tr>

<tr id="availWater" <?php if ($zone['irrigSchedule'] != 'threshold') echo 'style="display:none"';?>>
  <td>Available Water:</td>
  <td><input type="text" size=4 name="availableWater" id="availableWater" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['availableWater'], -1);?>"> %</td>
</tr>

<tr id="thresh" <?php if ($zone['irrigSchedule'] != 'threshold') echo 'style="display:none"';?>> 
  <td>Threshold Factor:</td>
  <td><input type="text" size=4 name="thresholdFactor" id="thresholdFactor" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['thresholdFactor'], -1);?>"> %</td>
</tr>

<tr>
  <td>Number of Cycles:</td>
  <td><select name="ncycles">
<?php
    for ($j = 1; $j <=5; $j++) {
      echo "<option name=\"$j\" value=\"$j\"";
      if ($zone['ncycles'] == $j) echo ' selected'; 
      echo ">$j";
    }
?>
  </select></td>
</tr>

<tr>
  <td>Irrigation application rate:</td>
  <td id="irrig_in_per_hr_td"><input type="text" size=4 name="irrig_in_per_hr" id="irrig_in_per_hr" onkeypress="return validDec(event);" onChange="updateUnits(this, 'irrig_cm_per_hr', 2.54);" value="<?php echo outFormat($zone['irrig_in_per_hr'], 2);?>"> inch/hr</td>
  <td id="irrig_gal_per_hr_td" style="display:none;"><input type="text" size=4 name="irrig_gal_per_hr" id="irrig_gal_per_hr" onkeypress="return validDec(event);" onChange="updateUnits(this, 'irrig_cm3_per_min', 63.090196);" value="<?php echo outFormat($zone['irrig_gal_per_hr'], 2);?>"> gal/hr</td>
  <td></td>
  <td id="irrig_cm_per_hr_td"><input type="text" size=4 name="irrig_cm_per_hr" id="irrig_cm_per_hr" onkeypress="return validDec(event);" onChange="updateUnits(this, 'irrig_in_per_hr', 1/2.54);" value="<?php echo outFormat($zone['irrig_in_per_hr']*2.54, 2);?>"> cm/hr</td>
  <td id="irrig_cm3_per_min_td" style="display:none;"><input type="text" size=4 name="irrig_cm3_per_min" id="irrig_cm3_per_min" onkeypress="return validDec(event);" onChange="updateUnits(this, 'irrig_gal_per_hr', 1/63.090196);" value="<?php echo outFormat($zone['irrig_gal_per_hr']*63.090196, -1);?>"> cm<sup>3</sup>/min</td>
</tr>

<tr id="minRunTime" <?php if ($zone['irrigSchedule'] == 'threshold' || $zone['irrigSchedule'] == 'none') echo 'style="display:none"';?>>
  <td>Min Run Time (minutes):</td>
  <td><select name="minIrrig">
  <?php
    for ($j = 0; $j <= 60; $j++) {
      echo '<option name="' . $j . '" value="' . $j . '"';
      if ($zone['minIrrig'] == $j) echo ' selected';
      echo '>' . $j;
    }
  ?>
  </select></td>
</tr>

<tr>
  <td>Irrigation system uniformity:</td>
  <td><input type="text" size=4 name="irrig_uniformity" id="irrig_uniformity" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['irrig_uniformity'], -1);?>"> %</td>
</tr>

<tr>
  <td align="left" colspan=6><br><br><b>Fixed Inputs</b><br></td>
</tr>

<tr>
  <td>Location:</td>
  <td><select name="location">
  <?php
    for ($j = 0; $j < count($ws); $j++) {
      echo '<option name="' . $ws[$j]['location'] . '" value="' . $ws[$j]['wsid'] . '"';
      if ($zone['wsid'] == $ws[$j]['wsid']) echo ' selected';
      echo '>' . $ws[$j]['location'];
    }
  ?>
  </select></td>
</tr>

<tr>
  <td valign="top">Production Area:</td>
  <td>
    <input type="radio" name="productionArea" value="open field" <?php if ($zone['productionArea'] == "open field") echo 'checked';?> onClick="displayProdArea(0);">Open Field<br>
    <input type="radio" name="productionArea" value="shadecloth" <?php if ($zone['productionArea'] == "shadecloth") echo 'checked';?> onClick="displayProdArea(1);">Shadecloth<br>
    <input type="radio" name="productionArea" value="plastic" <?php if ($zone['productionArea'] == "plastic") echo 'checked';?> onClick="displayProdArea(2);">Plastic<br>
  </td>
</tr>

<tr id="shade" <?php if ($zone['productionArea'] == "open field") echo 'style="display:none"';?>>
  <td>Shade (% light reduction):</td>
  <td><input type="text" size=4 name="shade" id="shade" onkeypress="return validDec(event);" value="<?php echo $zone['shade'];?>"> %</td>
</tr>

<tr>
  <td>Container top diameter:</td>
  <td><input type="text" size=4 name="containerDiam_in" id="containerDiam_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'containerDiam_cm', 2.54);" value="<?php echo outFormat($zone['containerDiam_in'], 1);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="containerDiam_cm" id="containerDiam_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'containerDiam_in', 1/2.54);" value="<?php echo outFormat($zone['containerDiam_in']*2.54, 1);?>"> cm</td>
</tr>

<tr id="irrig_capture_tr">
  <td>Plant irrigation capture ability:</td>
  <td><select name="irrigCaptureAbility" id="irrigCaptureAbility">
  <?php
    $irrigVals = Array("low", "medium", "high", "negative", "nil");
    for ($j = 0; $j < count($irrigVals); $j++) {
      echo '<option name="' . $irrigVals[$j] . '" value="' . $irrigVals[$j] . '"';
      if ($zone['irrigCaptureAbility'] == $irrigVals[$j]) echo ' selected';
      echo '>' . $irrigVals[$j];
    }
  ?>
  </select>
  </td>
</tr>

<tr id="leaching_fraction_tr">
  <td>Leaching fraction (LF):</td>
  <td><input type="text" size=4 name="leaching_fraction" id="leaching_fraction" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['leachingFraction'], -1);?>"> %</td>
</tr>

<tr>
  <td align="left" colspan=6><br><br><b>Rain Thresholds</b><br></td>
</tr>

<tr>
  <td>Hourly rain threshold:</td>
  <td><input type="text" size=4 name="hourly_rain_thresh_in" id="hourly_rain_thresh_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'hourly_rain_thresh_cm', 2.54);" value="<?php echo outFormat($zone['hourly_rain_thresh_in'], 2);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="hourly_rain_thresh_cm" id="hourly_rain_thresh_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'hourly_rain_thresh_in', 1/2.54);" value="<?php echo outFormat($zone['hourly_rain_thresh_in']*2.54, 2);?>"> cm</td>
</tr>

<tr>
  <td>Weekly rain threshold:</td>
  <td><input type="text" size=4 name="weekly_rain_thresh_in" id="weekly_rain_thresh_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'weekly_rain_thresh_cm', 2.54);" value="<?php echo outFormat($zone['weekly_rain_thresh_in'], 1);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="weekly_rain_thresh_cm" id="weekly_rain_thresh_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'weekly_rain_thresh_in', 1/2.54);" value="<?php echo outFormat($zone['weekly_rain_thresh_in']*2.54, 1);?>""> cm</td>
</tr>

<tr id="lf_input_label_tr" style="display:none;">
  <td align="left" colspan=6><br><br><b>LF Test Inputs</b><br></td>
</tr>

<tr id="lf_test_date_tr" style="display:none;">
  <td>LF Test Date:</td>
  <td valign="top" align="left">
    <input type="text" name="lfTestDate" size=10 readonly value="<?php echo $lfTestDay;?>">
    <a href="#" onClick="updateLFCalendar(cal, document.editzone.lfTestDate, 'anchor1x'); return false;" name="anchor1x" id="anchor1x">select</a>
    <select name="lfTestDateHour">
<?php
      for ($h = 1; $h <= 12; $h++) {
        echo '<option name="' . $h . '" value="' . $h . '"';
        if ($h%12 == $datearr['tm_hour']%12) echo " selected";
        echo '>' . $h . "\n";
      }
?>
    </select>
    <select name="lfTestDateMinute">
<?php
      for ($m = 0; $m < 60; $m++) {
        echo '<option name="' . $m . '" value="' . $m . '"';
	if ($m == $datearr['tm_min']) echo " selected";
	echo '>';
        if ($m < 10) echo '0';
	echo $m . "\n";
      }
?>
    </select>
    <select name="lfTestDateAmPm">
      <option name="am" value="am" <?php if ($datearr['tm_hour'] < 12) echo 'selected';?>>am 
      <option name="pm" value="pm" <?php if ($datearr['tm_hour'] >= 12) echo 'selected';?>>pm
    </select>
  </td>
</tr>

<tr id="lf_test_runtime_tr" style="display:none;">
  <td>LF Test Runtime:</td>
  <td><input type="text" size=4 name="lfTestRuntime" id="lfTestRuntime" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['lfTestRuntime'], 1);?>"> minutes</td>
</tr>

<tr id="lf_test_pct_tr" style="display:none;">
  <td>Test LF:</td>
  <td><input type="text" size=4 name="lfTestPct" id="lfTestPct" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['lfTestPct'], -1);?>"> %</td>
</tr>

<tr id="lf_target_pct_tr" style="display:none;">
  <td>Target LF:</td>
  <td><input type="text" size=4 name="lfTargetPct" id="lfTargetPct" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['lfTargetPct'], -1);?>"> %</td>
</tr>

<tr id="infrequent_label_tr">
  <td align="left" colspan=6><br><br><b>Infrequently or Slowly-Changing Inputs</b><br></td>
</tr>

<tr>
  <td align="left" colspan=6><b>Last changed: </b><span style="color: red;"><?php echo $zone['lastChanged'];?></span></td>
</tr>

<tr id="pctCover_tr">
  <td>Percent plant cover:</td>
  <td><input type="text" size=4 name="pctCover" id="pctCover" onkeypress="return validDec(event);" value="<?php echo outFormat($zone['pctCover'], -1);?>"> %</td>
</tr>

<tr id="plantHeight_tr">
  <td>Plant height:</td>
  <td><input type="text" size=4 name="plantHeight_in" id="plantHeight_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'plantHeight_cm', 2.54);" value="<?php echo outFormat($zone['plantHeight_in'], -1);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="plantHeight_cm" id="plantHeight_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'plantHeight_in', 1/2.54);" value="<?php echo outFormat($zone['plantHeight_in']*2.54, 1);?>"> cm</td>
</tr>

<tr id="plantWidth_tr">
  <td>Plant width:</td>
  <td><input type="text" size=4 name="plantWidth_in" id="plantWidth_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'plantWidth_cm', 2.54);" value="<?php echo outFormat($zone['plantWidth_in'], -1);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="plantWidth_cm" id="plantWidth_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'plantWidth_in', 1/2.54);" value="<?php echo outFormat($zone['plantWidth_in']*2.54, 1);?>"> cm</td>
</tr>

<tr id="contSpacing_tr">
  <td onMouseOver="ddrivetip('Average distance between the rims of adjacent containers. For pot-to-pot (jammed), container spacing = 0.', '99ffaa', 400);" onMouseOut="hideddrivetip();">Container spacing:</td>
  <td><input type="text" size=4 name="containerSpacing_in" id="containerSpacing_in" onkeypress="return validDec(event);" onChange="updateUnits(this, 'containerSpacing_cm', 2.54);" value="<?php echo outFormat($zone['containerSpacing_in'], 1);?>"> inch</td>
  <td></td>
  <td><input type="text" size=4 name="containerSpacing_cm" id="containerSpacing_cm" onkeypress="return validDec(event);" onChange="updateUnits(this, 'containerSpacing_in', 1/2.54);"  value="<?php echo outFormat($zone['containerSpacing_in']*2.54, 1);?>"> cm</td>
</tr>

<tr id="spacingArr_tr">
  <td>Spacing arrangement:</td>
  <td><select name="spacing" id="spacing">
    <option name="square" value="square" <?php if ($zone['spacing'] == 'square') echo 'selected'; ?>>square
    <option name="offset" value="offset" <?php if ($zone['spacing'] == 'offset') echo 'selected'; ?>>offset
  </select>
  </td>
</tr>

<?php if ($isLoggedIn) { ?>
<tr>
  <td></td>
  <td><br><br><input type="submit" name="submit" value="Edit Zone">
  <input type="reset" name="reset" value="Reset">
  </td>
</tr>
<?php } ?>
</table>
</div>
</form>
<script language="javascript">setZoneType(document.editzone.zoneType.options[document.editzone.zoneType.selectedIndex].value); //use current zone type to configure page</script>
<?php
  }
?>
<?php require('footer.php'); ?>
