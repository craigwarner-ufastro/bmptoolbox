<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  var fields = new Array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrigCaptureAbility", "irrig_in_per_hr", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "zoneType", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "irrig_gal_per_hr", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in");

  var etsfields = new Array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrigCaptureAbility", "irrig_in_per_hr", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "zoneType", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in");
  var etmfields = new Array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "irrig_gal_per_hr", "zoneType", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in");
  var lfsfields = new Array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrig_in_per_hr", "irrig_uniformity", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "zoneType", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in");
  var lfmfields = new Array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrig_uniformity", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "irrig_gal_per_hr", "zoneType", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in"); 

  function setCustomSelection() {
    document.getElementById("selectCols").value="Custom"
  }

  function setColSelection(zoneType) {
    if (zoneType == "All") {
      for (var j = 0; j < fields.length; j++) {
	document.getElementById(fields[j]).checked = true;
      }
    } else if (zoneType != "Custom") {
      for (var j = 0; j < fields.length; j++) {
        document.getElementById(fields[j]).checked = false;
      }
    }
    if (zoneType == "ET-sprinkler") {
      for (var j = 0; j < etsfields.length; j++) document.getElementById(etsfields[j]).checked = true;
    } else if (zoneType == "ET-micro") {
      for (var j = 0; j < etmfields.length; j++) document.getElementById(etmfields[j]).checked = true;
    } else if (zoneType == "LF-sprinkler") {
      for (var j = 0; j < lfsfields.length; j++) document.getElementById(lfsfields[j]).checked = true;
    } else if (zoneType == "LF-micro") {
      for (var j = 0; j < lfmfields.length; j++) document.getElementById(lfmfields[j]).checked = true;
    }
  }
</script>
<?php require('header.php'); ?>
<h2><center>Download Zones to CSV</center></h2>
<?php
  if ($isLoggedIn) {
    $ws = Array();
    $sql = "select * from weatherStations where uid = " . $_SESSION['bmpid'] . " OR public=true";
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$ws[] = $row;
      }
    }

    $fields = array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrigCaptureAbility", "irrig_in_per_hr", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "zoneType", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "irrig_gal_per_hr", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in");
?>

<form name="downloadZones" action="doCSVDownload.php" method="post">
<input type="hidden" name="isSubmitted" value="1">
<input type="hidden" name="uid" value="<?php echo $_SESSION['bmpid'];?>">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
  <td valign="top" colspan=4><b>Select type of zone or fields to download:</b> <select name="selectCols" id="selectCols" onChange="setColSelection(this.options[this.selectedIndex].value);">
    <option name="All" value="All">All
  <?php
    foreach($zoneTypes as $key=>$value) {
      echo "<option name=\"$value\" value=\"$value\"";
      echo ">" . $value;
    }
  ?>
    <option name="None" value="None">None
    <option name="Custom" value="Custom">Custom
    </select>
  </td>
</tr>

<?php
  foreach ($fields as $key=>$value) {
    if ($key % 4 == 0) echo '<tr>';
    echo "<td valign=\"top\"><input type=\"checkbox\" name=\"$value\" id=\"$value\" value=\"$value\" onClick=\"setCustomSelection();\">$value</td>";
    if ($key % 4 == 3) echo '</tr>';
  }
  echo '</tr>';
?>
<script language="javascript">
  document.getElementById("selectCols").value="ET-sprinkler";
  setColSelection("ET-sprinkler");
</script>
<input type="hidden" name="zid" value="1">
<tr>
  <td colspan=4 align="center"><br><br><input type="submit" name="submit" value="Download Zones">
  </td>
</tr>
<tr><td colspan=4 align="center"><br><br><a href="myaccount.php?cat=4">Back to My Zones</a></td></tr>
</table>
</div>
</form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to download your zones.';
  }
?>
<?php require('footer.php'); ?>
