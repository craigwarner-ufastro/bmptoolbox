<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  function checkUploaded() {
    var validExt = false;
    var ext = document.uploadfiles.userfile.value.substring(document.uploadfiles.userfile.value.length-4);
    if (ext == ".csv") validExt = true;
    if (!validExt) {
      alert('Invalid file type!  Must be .csv.');
      return false;
    }
    return retVal;
  }
</script>
<?php require('header.php'); ?>
<h2><center>Upload CSV to My Zones</center></h2>
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

    $fields = array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrigCaptureAbility", "irrig_in_per_hr", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "zoneType", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "irrig_gal_per_hr", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in", "zid");
?>

<form name="uploadweather" action="myaccount.php" method="post" enctype="multipart/form-data" onSubmit="return checkUploaded();">
<input type="hidden" name="mode" value="uploadzones">
<input type="hidden" name="MAX_FILE_SIZE" value="1000000">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
  <td valign="top" colspan=3 align="center">Instructions: The CSV file to upload must have a header line with column names correctly defined.  Use <a href="downloadZones.php">Download My Zones to CSV</a> to get an example template.
  If the zid column (last columnn) is defined for a row in the CSV file, that zone will be updated.  If that column is left blank, a new zone will be created. 
<br><br>
  </td>
</tr>

<tr><td>File to Upload</td>
    <td>
      <input type="file" name="zonecsvfile">
      <input type="submit" name="Upload" value="Upload">
    </td>
</tr>
<tr><td colspan=4 align="center"><br><br><a href="myaccount.php?cat=4">Back to My Zones</a></td></tr>
</table>
</div>
</form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to upload zones.';
  }
?>
<?php require('footer.php'); ?>
