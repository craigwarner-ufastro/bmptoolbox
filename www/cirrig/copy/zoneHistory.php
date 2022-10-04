<?php session_start(); ?>
<html>
<head>
<?php
  if (isset($_GET['zid']) && is_numeric($_GET['zid'])) {
    $zid = (int)$_GET['zid'];
  } else {
    $zid = -1;
  }

  $endDate = date("Y-m-d"); 
  $startDate = date("Y-m-d", time()-86400*7);
  if (isset($_POST['startDate'])) {
    $startDate = $_POST['startDate'];
  }
  if (isset($_POST['endDate'])) {
    $endDate = $_POST['endDate'];
  }
?>
<script language="javascript">
  var currDateField = null;

  function updateZoneCalendar(cal, dateField, anchor) {
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

  function setZoneCalendarVals(y,m,d) {
    currDateField.value = ""+y+"-"+m+"-"+d;
  }

</script>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
<script language+"javascript">
  cal.setReturnFunction("setZoneCalendarVals");
</script>
	<!-- Start body here -->
<?php
  if (!$isLoggedIn && !$doPreview) {
    $zid = -1;
    echo 'You must <a href="login.php">login</a> to view your zone histories.';
    exit(0);
  }

  if ($isLoggedIn) $uid = $_SESSION['bmpid']; else $uid=39;
  $sql = "select * from zones where uid = $uid and zid = $zid";
  $out = $mysqli_db->query($sql);
  if ($out != false) {
    if ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $zoneName = $row['zoneName'];
      $plantName = $row['plant'];
      $zoneDispType = substr($row['zoneType'], 0, 2);
    }
  } else if (!$isLoggedIn) {
    $zid = -1;
    echo 'You must <a href="login.php">login</a> to view your zone histories.';
    exit(0);
  }

  echo "<h2><center>Zone history for: $zoneName - $plantName</center></h2>";

  $info = Array();
  $sql = "select * from zoneHistory where zid = $zid AND histTime >= DATE('$startDate') AND histTime <= TIMESTAMP('$endDate 23:59:59') AND source != 1 ORDER BY histTime";
  $out = $mysqli_db->query($sql);
  $n = 0;
  if ($out != false) {
    while ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $info[$n] = $row;
      $n++;
    }
  }
?>
  <form name="smalltblZones" action="zoneHistory.php?zid=<?php echo $zid;?>" method="post">

  <table border=0 width=75% align="center">
  <tr>
    <td valign="top" align="left">
      Start Date: <input type="text" name="startDate" size=10 readonly value="<?php echo $startDate;?>">
      <a href="#" onClick="updateZoneCalendar(cal, document.smalltblZones.startDate, 'anchor1x'); return false;" name="anchor1x" id="anchor1x">select</a>
    </td>
    <td valign="top" width="100%" align="center">
      <input type="submit" name="submit" value="Submit">
    </td>
    <td valign="top" align="right">
      End Date: <input type="text" name="endDate" size=10 readonly value="<?php echo $endDate;?>">
      <a href="#" onClick="updateZoneCalendar(cal, document.smalltblZones.endDate, 'anchor2x'); return false;" name="anchor2x" id="anchor2x">select</a>
    </td>
  </tr></table>
  <br><br>

  <div style="margin-left: 0px;" align="center">
  <table border=1 align="center" class="smalltbl">
  <tr>
    <th valign="top" class="smalltblth-nobg" width=50>Timestamp</th>
<?php
if ($zoneDispType == "ET") {
?>
    <th valign="top" class="smalltblth" title="Plant Height (inches)">Plt<br>Ht<br>(in)</th>
    <th valign="top" class="smalltblth" title="Plant Width (inches)">Plt<br>Wid<br>(in)</th>
    <th valign="top" class="smalltblth" title="Percent Cover">%<br>Cov</th>
    <th valign="top" class="smalltblth" title="Container Spacing">Con<br>Spc</th>
    <th valign="top" class="smalltblth" title="Spacing Arrangement">Spac<br>Argmt</th>
    <th valign="top" class="smalltblth" title="Irrigation (inch)">Irr<br>(in)</th>
    <th valign="top" class="smalltblth" title="Run Time (minutes)">Run<br>Time<br>(min)</th>
    <th valign="top" class="smalltblth" title="Deficit (inch)">Deficit<br>(in)</th>
    <th valign="top" class="smalltblth" title="ETC (inch)">ETC<br>(in)</th>
    <th valign="top" class="smalltblth" title="CF">CF</th>
    <th valign="top" class="smalltblth" title="IU (%)">IU<br>(%)</th>
    <th valign="top" class="smalltblth" title="Irrigation Rate (inch/hour)">Irr<br>Rate<br>(in/hr)</th>
    <th valign="top" class="smalltblth" title="Solar Radiation (W/m^2)">Solar<br>Rad<br>(W/m<sup>2</sup>)</th>
    <th valign="top" class="smalltblth" title="Max Temperature (F)">Max<br>Temp<br>(F)</th>
    <th valign="top" class="smalltblth" title="Min Temperature (F)">Min<br>Temp<br>(F)</th>
    <th valign="top" class="smalltblth" title="Rain (inch)">Rain<br>(in)</th>
<?php
} else if ($zoneDispType == "LF") {
?>
    <th valign="top" class="smalltblth" title="Irrigation (inch)">Irr<br>(in)</th>
    <th valign="top" class="smalltblth" title="Run Time (minutes)">Run<br>Time<br>(min)</th>
    <th valign="top" class="smalltblth" title="Deficit (inch)">Deficit<br>(in)</th>
    <th valign="top" class="smalltblth" title="RT_LF (min)">RT<sub>LF</sub><br>(min)</th>
    <th valign="top" class="smalltblth" title="ET_LF (inch)">ET<sub>LF</sub><br>(inch)</th>
    <th valign="top" class="smalltblth" title="ETo/ET_LF (%)">ETo/ET<sub>LF</sub><br>(%)</th>
    <th valign="top" class="smalltblth" title="Irrigation Rate (inch/hour)">Irr<br>Rate<br>(in/hr)</th>
    <th valign="top" class="smalltblth" title="Solar Radiation (W/m^2)">Solar<br>Rad<br>(W/m<sup>2</sup>)</th>
    <th valign="top" class="smalltblth" title="Max Temperature (F)">Max<br>Temp<br>(F)</th>
    <th valign="top" class="smalltblth" title="Min Temperature (F)">Min<br>Temp<br>(F)</th>
    <th valign="top" class="smalltblth" title="Rain (inch)">Rain<br>(in)</th>
    <th valign="top" class="smalltblth" title="ETo (inch)">ETo<br>(in)</th>
<?php
}
?>
  </tr>
<?php
  for ($j = 0; $j < $n; $j++) {
    if ($j%2 == 0) $td = '<td class="smalltbltd"'; else $td = '<td class="smalltbltdalt"';
    echo '<tr>';
    echo $td . '>' . $info[$j]['histTime'] . '</td>';
    if ($zoneDispType == "ET") {
      echo $td . '>' . outFormat($info[$j]['plantHeight_in'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['plantWidth_in'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['pctCover'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['containerSpacing_in'], 1) . '</td>';
      echo $td . '>' . $info[$j]['spacing'] . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_minutes'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['deficit_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['etc_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['cf'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_uniformity'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_in_per_hr'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['solar_radiation'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['max_temp'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['min_temp'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['rain_in'], 2) . '</td>';
    } else if ($zoneDispType == "LF") {
      echo $td . '>' . outFormat($info[$j]['irrig_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_minutes'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['deficit_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['rtlf_min'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['etlf_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['etratio'], 1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['irrig_in_per_hr'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['solar_radiation'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['max_temp'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['min_temp'], -1) . '</td>';
      echo $td . '>' . outFormat($info[$j]['rain_in'], 2) . '</td>';
      echo $td . '>' . outFormat($info[$j]['et0_in'], 2) . '</td>';
    }
    echo '</tr>';
  }
?>
  </table>
  <br>
  <input type="submit" name="submit" value="Submit">
  <input type="reset" name="reset" value="Reset">
  </div>
  </form>
	<!-- End body here -->
<?php require('footer.php'); ?>
