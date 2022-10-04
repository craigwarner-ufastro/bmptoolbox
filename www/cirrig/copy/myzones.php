<h2><center>Manage My Zones</center></h2>
<center>
<a href="createZone.php">Create a new zone</a> |
<a href="globalZone.php">Global Zone Settings</a> |
<a href="downloadZones.php">Download My Zones to CSV</a> |
<a href="uploadZones.php">Upload CSV to My Zones</a>
<br><br></center>

<?php
  if ($isLoggedIn) {
    $uid = $_SESSION['bmpid'];
  } else if ($doPreview) $uid = 39;

  if ($isLoggedIn) {
    if (isset($_POST['mode']) && ($_POST['mode'] == "createzone" || $_POST['mode'] == "editzone")) {
      #Now read POST values
      $zoneNumber = $_POST['zoneNumber'];
      $zoneName = $_POST['zoneName'];
      $plant = $_POST['plant'];
      $containerDiam_in = $_POST['containerDiam_in'];
      $irrigCaptureAbility = $_POST['irrigCaptureAbility'];
      $irrig_in_per_hr = $_POST['irrig_in_per_hr']; #"optional"
      $irrig_uniformity = $_POST['irrig_uniformity'];
      $plantHeight_in = $_POST['plantHeight_in'];
      $plantWidth_in = $_POST['plantWidth_in'];
      $pctCover = $_POST['pctCover'];
      $containerSpacing_in = $_POST['containerSpacing_in'];
      $spacing = $_POST['spacing'];
      $wsid = $_POST['location'];
      #$priority = $_POST['priority'];
      $priority = 0;
      $external = $_POST['external'];
      $shade = $_POST['shade'];
      $lf = $_POST['leaching_fraction'];
      $irrigSchedule = $_POST['irrigSched'];
      $minIrrig = $_POST['minIrrig'];
      $fixedDays = 0;
      for ($j = 0; $j < 7; $j++) {
        if (isset($_POST['day' . $j])) $fixedDays += pow(2, $j);
      }
      $productionArea = $_POST['productionArea'];
      #Set shade = 0% for open field
      if ($productionArea == 'open field') $shade = 0;
      $availableWater = $_POST['availableWater'];
      $thresholdFactor = $_POST['thresholdFactor'];
      $zoneType = $_POST['zoneType'];
      $irrig_gal_per_hr = $_POST['irrig_gal_per_hr']; #"optional"
      $lfTestDate = $_POST['lfTestDate']; #"optional"
      if ($lfTestDate != NULL && isset($_POST['lfTestDateHour'])) {
	$hour = (int)($_POST['lfTestDateHour']) % 12;
	$minute = $_POST['lfTestDateMinute'];
	if ($_POST['lfTestDateAmPm'] == "pm") $hour += 12;
	$lfTestDate .= " $hour:$minute:00";
      }
      $lfTestRuntime = $_POST['lfTestRuntime']; #"optional"
      $lfTestPct = $_POST['lfTestPct']; #"optional"
      $lfTargetPct = $_POST['lfTargetPct']; #"optional"
      $ncycles = $_POST['ncycles'];
      $hourly_rain_thresh_in = $_POST['hourly_rain_thresh_in'];
      $weekly_rain_thresh_in = $_POST['weekly_rain_thresh_in'];

      #set "optional" default values
      if ($irrig_in_per_hr == NULL || $irrig_in_per_hr == "--") $irrig_in_per_hr = 0;
      if ($irrig_gal_per_hr == NULL || $irrig_gal_per_hr == "--") $irrig_gal_per_hr = 0;
      if ($lfTestDate == NULL || $lfTestDate == "--") $lfTestDate = "NULL";
      if ($lfTestRuntime == NULL || $lfTestRuntime == "--") $lfTestRuntime = 0;
      if ($lfTestPct == NULL || $lfTestPct == "--") $lfTestPct = 0;
      if ($lfTargetPct == NULL || $lfTargetPct == "--") $lfTargetPct = 0;
      if ($pctCover == NULL || $pctCover == "--") $pctCover = 0;
      if ($plantHeight_in == NULL || $plantHeight_in == "--") $plantHeight_in = 0;
      if ($plantWidth_in == NULL || $plantWidth_in == "--") $plantWidth_in = 0;
      if ($containerSpacing_in == NULL || $containerSpacing_in == "--") $containerSpacing_in = 0;
      if ($hourly_rain_thresh_in == NULL || $hourly_rain_thresh_in == "--") $hourly_rain_thresh_in = 0;
      if ($weekly_rain_thresh_in == NULL || $weekly_rain_thresh_in == "--") $weekly_rain_thresh_in = 0;
      if ($lf == NULL || $lf == "--") $lf = 0;
    }

    if (isset($_POST['mode']) && $_POST['mode'] == "createzone") {
      #Add new zone 
      $sql = "insert into zones(uid, zoneNumber, zoneName, plant, containerDiam_in, irrigCaptureAbility, irrig_in_per_hr, irrig_uniformity, plantHeight_in, plantWidth_in, pctCover, containerSpacing_in, spacing, wsid, lastChanged, priority, external, shade, leachingFraction, irrigSchedule, minIrrig, fixedDays, productionArea, availableWater, thresholdFactor, zoneType, irrig_gal_per_hr, lfTestDate, lfTestRuntime, lfTestPct, lfTargetPct, ncycles, hourly_rain_thresh_in, weekly_rain_thresh_in, auto, autoRunTime)";
      $sql .= " values ($uid, $zoneNumber, '$zoneName', '$plant', $containerDiam_in, '$irrigCaptureAbility', $irrig_in_per_hr, $irrig_uniformity, $plantHeight_in, $plantWidth_in, $pctCover, $containerSpacing_in, '$spacing', $wsid, now(), $priority, '$external', $shade, $lf, '$irrigSchedule', $minIrrig, $fixedDays, '$productionArea', $availableWater, $thresholdFactor, '$zoneType', $irrig_gal_per_hr, '$lfTestDate', $lfTestRuntime, $lfTestPct, $lfTargetPct, $ncycles, $hourly_rain_thresh_in, $weekly_rain_thresh_in, 1, MAKETIME(5, 15, 0))"; 
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo '<center><span class="regreq">Failed to add zone!</span></center>';
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "editzone") {
      $zid = $_POST['zid'];
      #Edit zone
      $sql = "update zones set zoneNumber=$zoneNumber, zoneName='$zoneName', plant='$plant', containerDiam_in=$containerDiam_in, irrigCaptureAbility='$irrigCaptureAbility', irrig_in_per_hr=$irrig_in_per_hr";
      $sql .= ", irrig_uniformity=$irrig_uniformity, plantHeight_in=$plantHeight_in, plantWidth_in=$plantWidth_in, pctCover=$pctCover, containerSpacing_in=$containerSpacing_in, spacing='$spacing', wsid=$wsid";
      $sql .= ", lastChanged=now(), priority=$priority, external='$external', shade=$shade, leachingFraction=$lf, irrigSchedule='$irrigSchedule', minIrrig=$minIrrig, fixedDays=$fixedDays";
      $sql .= ", productionArea='$productionArea', availableWater=$availableWater, thresholdFactor=$thresholdFactor, zoneType='$zoneType', irrig_gal_per_hr=$irrig_gal_per_hr";
      $sql .= ", lfTestDate='$lfTestDate', lfTestRuntime=$lfTestRuntime, lfTestPct=$lfTestPct, lfTargetPct=$lfTargetPct, ncycles=$ncycles";
      $sql .= ", hourly_rain_thresh_in=$hourly_rain_thresh_in, weekly_rain_thresh_in=$weekly_rain_thresh_in where uid=$uid and zid=$zid;";
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo '<center><span class="regreq">Failed to edit zone!</span></center>';
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "zones") {
      foreach ($_POST as $key=>$value) {
        //delete zones 
	if (substr($key, 0, 4) == 'del-' && $value == 'on') {
	  $id = substr($key, 4);
	  $sql = 'delete from zones where zid="' . $id . '" AND uid=' . $_SESSION['bmpid'];
          $out = $mysqli_db->query($sql);
        } 
	if (substr($key, 0, 5) == 'auto-') {
          $id = substr($key, 5);
	  if ($value == "on") {
	    $sql = "update zones set auto=1 where zid=$id AND uid = " . $_SESSION['bmpid'];
            $out = $mysqli_db->query($sql);
	  }
	  $hour = floor($_POST['autoHour-' . $id]);
	  $minute = floor($_POST['autoMin-' . $id]);
	  $second = 0;
	  if ($hour == 12) $hour = 0;
	  if ($_POST['autoAmPm-' . $id] == "pm") $hour += 12;
	  $sql = "update zones set autoRunTime=MAKETIME($hour, $minute, $second) where zid=$id AND uid=" . $_SESSION['bmpid'];
          $out = $mysqli_db->query($sql);
	} else if (substr($key, 0, 9) == "autoHour-") {
	  $id = substr($key, 9);
	  if (!isset($_POST['auto-' . $id])) {
            $sql = "update zones set auto=1 where zid=$id AND uid = " . $_SESSION['bmpid'];
	    $out = $mysqli_db->query($sql);
	    $hour = floor($_POST['autoHour-' . $id]);
	    $minute = floor($_POST['autoMin-' . $id]);
	    $second = 0;
	    if ($hour == 12) $hour = 0;
            if ($_POST['autoAmPm-' . $id] == "pm") $hour += 12;
            $sql = "update zones set autoRunTime=MAKETIME($hour, $minute, $second) where zid=$id AND uid=" . $_SESSION['bmpid'];
            $out = $mysqli_db->query($sql);
	  }
	}
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "globalzone") {
      $clause = "where uid=" . $_SESSION['bmpid'];
      if ($_POST['global'] != -1) $clause .= " AND wsid=" . $_POST['global']; 
      if (isset($_POST['enableTime'])) {
	$hour = floor($_POST['autoHour']);
	$minute = floor($_POST['autoMin']);
	$second = 0;
	if ($hour == 12) $hour = 0;
	if ($_POST['autoAmPm'] == "pm") $hour += 12;
	$sql = "update zones set lastChanged=now(), autoRunTime=MAKETIME($hour, $minute, $second) $clause";
        $out = $mysqli_db->query($sql);
      }
      if (isset($_POST['enableIrrig'])) {
        $irrigSchedule = $_POST['irrigSched'];
        $minIrrig = $_POST['minIrrig'];
        $fixedDays = 0;
        for ($j = 0; $j < 7; $j++) {
          if (isset($_POST['day' . $j])) $fixedDays += pow(2, $j);
        }
        $availableWater = $_POST['availableWater'];
        $thresholdFactor = $_POST['thresholdFactor'];
	$sql = "update zones set lastChanged=now(), irrigSchedule='$irrigSchedule', minIrrig=$minIrrig, fixedDays=$fixedDays, availableWater=$availableWater, thresholdFactor=$thresholdFactor $clause"; 
        $out = $mysqli_db->query($sql);
      }
      if (isset($_POST['enableProd'])) {
	$shade = $_POST['shade'];
        $productionArea = $_POST['productionArea'];
        #Set shade = 0% for open field
        if ($productionArea == 'open field') $shade = 0;
	$sql = "update zones set lastChanged=now(), productionArea='$productionArea', shade=$shade $clause";
        $out = $mysqli_db->query($sql);
      }
      if (isset($_POST['enableLF'])) {
        $lf = $_POST['leaching_fraction'];
	$sql = "update zones set lastChanged=now(), leachingFraction=$lf $clause";
        $out = $mysqli_db->query($sql);
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "uploadzones") {
      $csv = file($_FILES['zonecsvfile']['tmp_name']);

      $fields = array("zoneNumber", "zoneName", "plant", "containerDiam_in", "irrigCaptureAbility", "irrig_in_per_hr", "irrig_uniformity", "plantHeight_in", "plantWidth_in", "pctCover", "containerSpacing_in", "spacing", "wsid", "priority", "auto", "autoRunTime", "external", "shade", "leachingFraction", "irrigSchedule", "fixedDays", "minIrrig", "productionArea", "availableWater", "thresholdFactor", "zoneType", "lfTestDate", "lfTestRuntime", "lfTestPct", "lfTargetPct", "irrig_gal_per_hr", "ncycles", "hourly_rain_thresh_in", "weekly_rain_thresh_in", "zid");

      $delim = ',';
      $headers = explode($delim, trim(removeQuotes($csv[0])));
      $nhead = count($headers);

      $validHeaders = True;
      $invalidCol = "";
      for ($j = 0; $j < count($headers); $j++) {
	if (array_search($headers[$j], $fields) === False) {
	  $validHeaders = False;
	  $invalidCol = $headers[$j];
          echo '<center><span class="regreq">Invalid column name: ' . $invalidCol . '. Failed to add zones!</span></center><br>';
	}
      } 
      if ($headers[$nhead-1] != 'zid') {
	$validHeaders = False;
	echo '<center><span class="regreq">Error: zid is required to be last column in csv input file!  Failed to add zones!</span></center><br>';
      }	

      if ($validHeaders) {
	for ($j = 1; $j < count($csv); $j++) {
	  $cols = explode($delim, trim(removeQuotes($csv[$j])));
	  $ncols = count($cols);
	  if ($ncols == $nhead) {
	    if ($cols[$ncols-1] == '') {
	      //no zid = new zone
	      $sql = "insert into zones(uid";
	      //loop to $nhead-1 to exclude zid
	      for ($i = 0; $i < $nhead-1; $i++) $sql .= ", " . $headers[$i];
	      if (array_search("auto", $headers) === False) $sql .= ", auto";
	      if (array_search("autoRunTime", $headers) === False) $sql .= ", autoRunTime";
	      $sql .= ", lastChanged) values ($uid";
	      for ($i = 0; $i < $nhead-1; $i++) $sql .= ", '" . $mysqli_db->real_escape_string($cols[$i]) . "'";
	      if (array_search("auto", $headers) === False) $sql .= ", 1";
              if (array_search("autoRunTime", $headers) === False) $sql .= ", MAKETIME(5, 15, 0)";
	      $sql .= ", now())";
	      $out = $mysqli_db->query($sql);
	      if ($out == false) {
	        echo '<center><span class="regreq">Failed to add zone for line number ' . $j . '</span></center>';
              }
	    } else {
	      //zid exists = update zone
	      $sql = "update zones set ";
              //loop to $nhead-1 to exclude zid
              for ($i = 0; $i < $nhead-1; $i++) $sql .= $headers[$i] . "='" . $mysqli_db->real_escape_string($cols[$i]) . "', ";
              $sql .= "lastChanged=now() where uid=$uid and zid=" . $mysqli_db->real_escape_string($cols[$ncols-1]). ";";
              $out = $mysqli_db->query($sql);
              if ($out == false) {
                echo '<center><span class="regreq">Failed to update zone zid = ' . $cols[$ncols-1] . '; line number ' . $j . '</span></center>';
              }
	    }
	  } else {
	    echo '<center><span class="regreq">Error: line ' . $j . ' has ' . $ncols . ' columns instead of ' . $nhead . '.  Skipping this line.</span></center><br>'; 
	  }
	}
      }
    }
  }

  if ($isLoggedIn or $doPreview) {
    $zones = Array();
    $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid = " . $uid;
    $out = $mysqli_db->query($sql);
    $n = 0;
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $zones[] = $row;
	$n++;
      }
    }
?>
    <form name="managezones" action="myaccount.php" method="post">
    <input type="hidden" name="mode" value="zones">
    <div style="margin-left: 0px;" align="center">
    <table border=1 align="center" class="manage">
    <tr>
      <th valign="top" class="manageth-nobg">Zone</th>
      <th valign="top" class="manageth">Name</th>
      <th valign="top" class="manageth">Plant</th>
      <th valign="top" class="manageth">Type</th>
      <th valign="top" class="manageth">Weather<br>Station</th>
      <th valign="top" class="manageth">Ext<br>Ref</th>
      <th valign="top" class="manageth">Time to Run</th>
      <th valign="top" class="manageth">History</th>
      <th valign="top" class="manageth">Edit</th>
      <?php if ($isLoggedIn) echo '<th valign="top" class="manageth">Delete</th>'; ?>
    </tr>
<?php
      for ($j = 0; $j < $n; $j++) {
	if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
	echo '<tr>';
	echo $td . $zones[$j]['zoneNumber'] . '</td>';
        echo $td . $zones[$j]['zoneName'] . '</td>';
        echo $td . $zones[$j]['plant'] . '</td>';
	echo $td . $zones[$j]['zoneType'] . '</td>';
        echo $td . $zones[$j]['location'] . '</td>';
        echo $td . $zones[$j]['external'] . '</td>';
        echo $td;
	$hour = 5;
	$minute = 15;
        if ($zones[$j]['autoRunTime'] != NULL) {
	  $pos1 = strpos($zones[$j]['autoRunTime'], ":");
          $pos2 = strpos($zones[$j]['autoRunTime'], ":", $pos1+1);
          $hour = floor(substr($zones[$j]['autoRunTime'], 0, $pos1));
          $minute = floor(substr($zones[$j]['autoRunTime'], $pos1+1, $pos2));
        }
	echo '<select name="autoHour-' . $zones[$j]['zid'] . '">';
	for ($h = 1; $h <= 12; $h++) {
	  echo '<option name="' . $h . '" value="' . $h . '"';
	  if ($hour % 12 == $h%12) echo " selected";
	  echo '>' . $h . "\n";
	}
	echo '</select> : <select name="autoMin-' . $zones[$j]['zid'] . '">';
	for ($m = 0; $m < 60; $m++) {
	  echo '<option name="' . $m . '" value="' . $m . '"';
	  if ($minute == $m) echo " selected";
	  echo '>';
	  if ($m < 10) echo '0';
	  echo  $m . "\n";
	}
	echo '</select>';
	echo '<select name="autoAmPm-' . $zones[$j]['zid'] . '">';
	echo '<option name="am" value="am"';
	if ($hour < 12) echo " selected";
	echo '>am';
	echo '<option name="pm" value="pm"';
	if ($hour >= 12) echo " selected";
	echo '>pm';
	echo '</select>';
	echo '</td>';
        echo $td . '[<a href="zoneHistory.php?zid=' . $zones[$j]['zid'] . '">history</a>]</td>';
        echo $td . '[<a href="editZone.php?zid=' . $zones[$j]['zid'] . '">edit</a>]</td>';
        if ($isLoggedIn) echo $td . '<input type="checkbox" name="del-' . $zones[$j]['zid'] . '"></td>';
	echo '</tr>';
      }
?>
      </table>
      <br>
<?php
    if ($isLoggedIn) {
?>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
<?php } ?>
      </div>
      </form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to view your zones.';
  }
?>
