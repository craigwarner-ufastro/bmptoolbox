<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  function saveHistory(x) {
    document.historyForm.historyToSave.value = x;
    document.historyForm.submit();
  }

  function updateOrder(column) {
    document.updateWeather.orderby.value = column;
    document.updateWeather.submit()
  }
</script>
<?php require('header.php'); ?> 
<script language="javascript">
  homeButton.src = "images/button-sel-home.png";
  off('home');
</script>
<h2><img src="images/cirrig.png" alt="cirrig logo" title="cirrig" align="absmiddle">
<?php
  echo '<span style="margin-left: 50px;">Irrigation for ' . date("M j, Y") . "</span></h2>";
  if ($isLoggedIn) {
    $uid = $_SESSION['bmpid'];
  } else {
    require('login_code.php');
    if (!$doPreview) {
      echo '<form name="previewForm" action="index.php" method="post">';
      echo '<input type="hidden" name="showPreview" value="true">';
      echo '<center><input type="button" name="doPreview" value="Click here to get a quick peek at CIRRIG" onClick="document.previewForm.submit();"></center>';
      echo '</form>';
    } else $uid = 39; //doPreview mode
  }
  if ($isLoggedIn or $doPreview) {
?>
    <ul>
    <li><a href="createZone.php">Create a new zone</a><br>
    <li><a href="myaccount.php?cat=4">Manage My Zones</a><br>
    </ul>
<?php
    require('zone.php');
    require('etzone.php');
    require('lfzone.php');
    #Get distinct weather stations
    $ws = Array();
    #$sql = "select distinct zones.wsid, weatherStations.location from zones INNER JOIN weatherStations on zones.wsid=weatherStations.wsid where zones.uid=" . $_SESSION['bmpid'];
    $sql = "select distinct zones.wsid, weatherStations.location from zones INNER JOIN weatherStations on zones.wsid=weatherStations.wsid where zones.uid=$uid";
    $out = $mysqli_db->query($sql);
    $nws = 0;
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $ws[] = $row;
        $nws++;
      }
    }

    $wsid = -1;
    $wsLocation = "Null";
    echo '<form method="POST" name="updateWeather" action="index.php">';
    if ($nws != 0) {
      $wsid = $ws[0]['wsid'];
      $wsLocation = $ws[0]['location'];
    }

    $zoneDispType = "ET";
    if (isset($_POST['zoneDispType'])) {
      $zoneDispType = $_POST['zoneDispType']; 
      #Update prefs table to make this the default type
      $sql = "select count(*) from prefs where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      $update = false;
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
          if ($row[0] > 0) $update = true;
        }
      }
      if ($update) {
        $sql = "update prefs set defaultZoneType='" . $zoneDispType . "' where uid = " . $_SESSION['bmpid'];
      } else {
        $sql = "insert into prefs (uid, defaultZoneType) values ("; 
        $sql .= $_SESSION['bmpid'] . ", '" . $zoneDispType . "')";
      }
      $out = $mysqli_db->query($sql);
    } else {
      #Get default type from prefs
      $sql = "select defaultZoneType from prefs where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      $update = false;
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	  $zoneDispType = $row['defaultZoneType'];
	}
      }
    }

    $orderby = "zid";
    if (isset($_POST['orderby'])) $orderby = $_POST['orderby']; 
    echo '<input type="hidden" name="orderby" value="' . $orderby . '">';
 
    #Handle case with > 1 weather station
    if ($nws > 1) {
      if (isset($_POST['wsid'])) {
	$wsid = $_POST['wsid'];
      }
      echo '<b>Select a weather station: </b>';
      echo '<select name="wsid" id="wsid" onChange="document.updateWeather.submit();">';
      for ($j = 0; $j < count($ws); $j++) {
	echo '<option name="' . $ws[$j]['wsid'] . '" value="' . $ws[$j]['wsid'] . '"';
        if ($ws[$j]['wsid'] == $wsid) {
	  echo ' selected';
	  $wsLocation = $ws[$j]['location'];
	} 
        echo '>' . $ws[$j]['location'];
      }
      echo '</select>';
      echo '<br><br>';
      $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.wsid=$wsid AND zones.uid=$uid";
      #$sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.wsid=$wsid AND zones.uid=". $_SESSION['bmpid'];
    } else {
      $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid = $uid";
      #$sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid = " . $_SESSION['bmpid'];
    }
    $sql .= " order by $orderby"; 

    #Get zones
    $zones = Array();
    $out = $mysqli_db->query($sql);
    $nzones = 0;
    $doThreshTable = false;
    $wTime = "auto";
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	//throw away zones that don't match zoneDispType
	if (substr($row['zoneType'], 0, 2) != $zoneDispType) continue;
	if ($zoneDispType == "ET") {
	  $zones[] = new etzone($row);
	  $nzones++;
	} else if ($zoneDispType == "LF") {
	  $zones[] = new lfzone($row);
	  $nzones++;
	}
      }
    }

    if (count($zones) == 0) {
?>
      <table border=0>
      <tr><td><b>Zone Type: </b>
      </td>
      <td><input type="radio" name="zoneDispType" id="zoneDispType" value="ET" onClick="document.updateWeather.submit();" <?php if ($zoneDispType == "ET") echo " checked";?>>ET</td></tr>
      <tr><td></td>
      <td><input type="radio" name="zoneDispType" id="zoneDispType" value="LF" onClick="document.updateWeather.submit();" <?php if ($zoneDispType == "LF") echo " checked";?>>LF</td></tr>
      </table>
      <br><br>
      </form>
<?php
      echo "You currently have no $zoneDispType zones.";
      require('footer.php');
      exit(0);
    }

    #Get weather
    $firstHour = -1;
    if (strtotime($zones[0]->getAutoRunTime()) > mktime()) {
      $hourVal = "'" . date("Y-m-d", mktime()-86400) . ' ' . $zones[0]->getAutoRunTime() . "'";
    } else {
      $hourVal = "'" . date("Y-m-d", mktime()) . ' ' . $zones[0]->getAutoRunTime() . "'";
    }
    $temp = explode(':', $zones[0]->getAutoRunTime());
    $firstHour = (int)($temp[0])+1;

    #Don't check uid to allow public access.  wsid is unique.
    $sql = "select * from hourlyWeather where wsid=$wsid AND hour > $hourVal-INTERVAL 25 HOUR AND hour <= $hourVal ORDER BY hour;";
    $hourly = Array();
    $out = $mysqli_db->query($sql);
    $nhourly = 0;
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$hr = intval(date("H", strtotime($row['hour'])));
	if (!isset($hourly[$hr])) $nhourly++;
        $hourly[$hr] = $row;
      }
    }
    if ($nhourly < 24) {
      for ($j = 0; $j < 24; $j++) {
        if (isset($hourly[$j])) continue;
        #Don't check uid to allow public access.  wsid is unique.
        $sql = "select * from hourlyWeather where wsid=$wsid AND HOUR(hour)=$j AND hour <= $hourVal ORDER BY hour DESC LIMIT 1;";
        $out = $mysqli_db->query($sql);
        if ($out != false) {
	  if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	    if ($wTime == "auto" && $nzones > 1) {
              echo "Could not find weather for " . date("H:00:00", strtotime($row['hour'])) . ". Using data from " . date("M j", strtotime($row['hour'])) . " instead for zone " . $zones[0]->getName() . ".<br>";
	    } else {
	      echo "Could not find weather for " . date("H:00:00", strtotime($row['hour'])) . ". Using data from " . date("M j", strtotime($row['hour'])) . " instead.<br>"; 
	    }
	    $hourly[$j] = $row;
	    $nhourly++;
	  }
	}
      }
    }

    if ($nhourly < 24) {
      echo "<center><b>ERROR: Weather station $wsLocation only has $nhourly hourly weather datapoints!</b></center>";
      #exit(0);
    }
    $zones[0]->setHourlyWeather($hourly);

    if ($wTime == "auto" && $nzones > 1) {
      #reset and reuse hourly array and nhourly
      $hourly = Array();
      $nhourly = 0;
      for ($i = 1; $i < $nzones; $i++) {
        if (strtotime($zones[$i]->getAutoRunTime()) > mktime()) {
          $hourVal = "'" . date("Y-m-d", mktime()-86400) . ' ' . $zones[$i]->getAutoRunTime() . "'";
        } else {
          $hourVal = "'" . date("Y-m-d", mktime()) . ' ' . $zones[$i]->getAutoRunTime() . "'";
        }
        #Don't check uid to allow public access.  wsid is unique.
        $sql = "select * from hourlyWeather where wsid=$wsid AND hour > $hourVal-INTERVAL 25 HOUR AND hour <= $hourVal ORDER BY hour;";
	$out = $mysqli_db->query($sql);
	if ($out != false) {
	  while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	    $hr = intval(date("H", strtotime($row['hour'])));
	    if (!isset($hourly[$hr])) $nhourly++;
	    $hourly[$hr] = $row;
	  }
	}
	if ($nhourly < 24) {
	  for ($j = 0; $j < 24; $j++) {
	    if (isset($hourly[$j])) continue;
	    #$sql = "select * from hourlyWeather where wsid=$wsid AND uid=" . $_SESSION['bmpid'] . " AND HOUR(hour)=$j AND hour <= $hourVal ORDER BY hour DESC LIMIT 1;";
            #Don't check uid to allow public access.  wsid is unique.
            $sql = "select * from hourlyWeather where wsid=$wsid AND HOUR(hour)=$j AND hour <= $hourVal ORDER BY hour DESC LIMIT 1;";
	    $out = $mysqli_db->query($sql);
	    if ($out != false) {
	      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	        echo "Could not find weather for " . date("H:00:00", strtotime($row['hour'])) . ". Using data from " . date("M j", strtotime($row['hour'])) . " instead for zone " . $zones[$i]->getName() . ".<br>";
	        $hourly[$j] = $row;
	        $nhourly++;
	      }
	    }
	  }
        }
    
        if ($nhourly < 24) {
          echo "<center><b>ERROR: Weather station $wsLocation only has $nhourly hourly weather datapoints!</b></center>";
          #exit(0);
        }
	$zones[$i]->setHourlyWeather($hourly);
      }
    } else {
      for ($i = 1; $i < $nzones; $i++) {
	$zones[$i]->setHourlyWeather($hourly);
      }
    }
?>

    <table border=0>
    <tr><td><b>Zone Type: </b>
    </td>
    <td><input type="radio" name="zoneDispType" id="zoneDispType" value="ET" onClick="document.updateWeather.submit();" <?php if ($zoneDispType == "ET") echo " checked";?>>ET</td></tr>
    <tr><td></td>
    <td><input type="radio" name="zoneDispType" id="zoneDispType" value="LF" onClick="document.updateWeather.submit();" <?php if ($zoneDispType == "LF") echo " checked";?>>LF</td></tr>
    </table>
<!--
    <b>Zone Type: </b>
    <select name="zoneDispType" id="zoneDispType" onChange="document.updateWeather.submit();">
    <option name="ET" value="ET">ET
    <option name="LF" value="LF" <?php #if ($zoneDispType == "LF") echo "selected";?>>LF 
    </select><br><br>
-->

<?php
    #Update weather button clicked?
    if (isset($_POST['isSubmitted']) && $_POST['isSubmitted'] == 2) {
      $tmax_f = floatVal($_POST['tmax']);
      $tmin_f = floatVal($_POST['tmin']);
      $solar_wmhr = floatVal($_POST['solar_rad']);
      $rain_in = floatVal($_POST['rain']);

      for ($i = 0; $i < $nzones; $i++) {
	$zones[$i]->updateUserWeather($tmax_f, $tmin_f, $solar_wmhr, $rain_in);
      }
    }

    ##Get from first zone for displayed weather
    $solar_wmhr = $zones[0]->getSolar();
    $tmax_f = $zones[0]->getTmax();
    $tmin_f = $zones[0]->getTmin();
    $rain_in = $zones[0]->getRain();

    echo '<input type="hidden" name="isSubmitted" value="1">';
    echo "<h3>Past day's weather for <a href=\"viewWeather.php?wsid=$wsid\">$wsLocation</a> at $hourVal</h3>";
    echo '<table border=0><tr>';
    echo '<td>Solar radiation:</td>';
    echo '<td>' . outFormat($solar_wmhr, 1) . ' W/m<sup>2</sup></td>';
    echo '<td width=50></td>';
    echo '<td>' . outFormat($solar_wmhr*0.0864, 1) . ' MJ/m<sup>2</sup></td></tr>';

    echo '<tr><td>Min temperature:</td>';
    echo '<td>' . outFormat($tmin_f, 1) . ' F</td>';
    echo '<td></td>';
    echo '<td>' . outFormat(($tmin_f-32)/1.8, 1) . ' C</td>';

    echo '<tr><td>Max temperature:</td>';
    echo '<td>' . outFormat($tmax_f, 1) . ' F</td>';
    echo '<td></td>';
    echo '<td>' . outFormat(($tmax_f-32)/1.8, 1) . ' C</td>';

    echo '<tr><td>Rainfall:</td>';
    echo '<td>' . outFormat($rain_in,2) . ' inches</td>';
    echo '<td></td>';
    echo '<td>' . outFormat($rain_in*2.54,2) . ' cm</td>';

    echo '</table><br><br>';
    echo '</form>';
    
?>
    <div style="margin-left: 0px;" align="center">
    <form action="index.php" method="post" name="historyForm">
    <table border=1 align="center" class="manage">
    <tr>
      <th valign="top" class="manageth-nobg"><?php if ($orderby == 'zoneNumber') echo "Zone"; else echo "<a href=\"javascript:updateOrder('zoneNumber');\">Zone</a>"; ?></th>
      <th valign="top" class="manageth"><?php if ($orderby == 'zoneName') echo "Name"; else echo "<a href=\"javascript:updateOrder('zoneName');\">Name</a>"; ?></th>
      <th valign="top" class="manageth"><?php if ($orderby == 'plant') echo "Plant"; else echo "<a href=\"javascript:updateOrder('plant');\">Plant</a>"; ?></th>
      <th valign="top" class="manageth">Sched</th>
      <th valign="top" class="manageth">Time</th>
      <th valign="top" class="manageth">Irrig<br>(inch)</th>
      <th valign="top" class="manageth">Run time<br>(min)</th>
      <th valign="top" class="manageth">Deficit<br>(inch)</th>
<?php
if ($zoneDispType == "ET") {
?>
      <th valign="top" class="manageth">ETC<br>(inch)</th>
      <th valign="top" class="manageth">CF</th>
      <th valign="top" class="manageth">IU<br>(%)</th>
<?php
} else if ($zoneDispType == "LF") {
?>
      <th valign="top" class="manageth">RT<sub>LF</sub><br>(min)</th>
      <th valign="top" class="manageth">ET<sub>LF</sub><br>(inch)</th>
      <th valign="top" class="manageth">ETo/ET<sub>LF</sub><br>(%)</th>
<?php
}
?>
      <th valign="top" class="manageth">Irrig<br>rate</th>
    </tr>
<?php
      $tdred = '<td class="managetdred">';
      $tdgreen = '<td class="managetdgreen">';
      $tdblue = '<td class="managetdblue">';

      for ($j = 0; $j < $nzones; $j++) {
	#Check prev deficit and set in zones
	$prevDeficit = 0;
        $sql = "select histTime, deficit_in from zoneHistory where wsid=$wsid AND zid=" . $zones[$j]->getId() . " AND date(histTime) = curdate()-interval 1 day and source=2 LIMIT 1;";
        $out = $mysqli_db->query($sql);
        if ($out != false) {
          if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	    $prevDeficit = $row['deficit_in'];
          }
        }
        $zones[$j]->setPrevDeficit($prevDeficit);

	#For LF zones, get weather on day before LF test date
        if ($zones[$j]->zoneType == "LF-sprinkler" || $zones[$j]->zoneType == "LF-micro") {
          #Don't check uid to allow public access.  wsid is unique.
          $sql = "select * from hourlyWeather where wsid=$wsid AND hour <= '" . $zones[$j]->lfTestDate . "' AND hour > '" . $zones[$j]->lfTestDate . "' - INTERVAL 1 day ORDER BY hour;";
          $lfhourly = Array();
          $out = $mysqli_db->query($sql);
          $lfnhourly = 0;
          if ($out != false) {
            while ($row = $out->fetch_array(MYSQLI_BOTH)) {
              $hr = intval(date("H", strtotime($row['hour'])));
              if (!isset($zhourly[$i][$hr])) $lfnhourly++;
              $lfhourly[$hr] = $row;
            }
          }
	  $zones[$j]->setLFHourlyWeather($lfhourly);
	}

	#get irrigation -- ET and LF zones will each handle proper calculations
        $irrigation = $zones[$j]->getIrrigation();
        $irrig_in = $irrigation['irrig_in'];
        $irrig_time = $irrigation['irrig_time'];

	#Calculate effecitve rain values
	$zones[$j]->calcEffectiveRain();

        #process different irrig schedules and min irrig here
	$irrigation = $zones[$j]->processIrrigSchedule($irrig_in, $irrig_time);
        $irrig_in = $irrigation['irrig_in'];
        $irrig_time = $irrigation['irrig_time'];
	$deficit_in = $irrigation['deficit_in'];
	$irrig_output = $irrigation['irrig_output'];
	if ($irrigation['doThreshTable']) $doThreshTable = True;

        if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . '<a href="editZone.php?zid=' . $zones[$j]->getId() . '" title="Edit this zone">' . $zones[$j]->zoneNumber . '</a></td>';
        echo $td . $zones[$j]->getName() . '</td>';
        echo $td . $zones[$j]->getPlant() . '</td>';
	echo $td . $zones[$j]->getIrrigSchedule() . '</td>';
	echo $td . $zones[$j]->getAutoRunTime() . '</td>';
	echo $tdgreen . outFormat($irrig_in, 2) . '</td>';
	echo $tdgreen . $irrig_output . '</td>';
        echo $tdgreen . outFormat($deficit_in, 2) . '</td>';
	if ($zoneDispType == "ET") {
	  echo $td . outFormat($zones[$j]->contETin, 2) . '</td>';
	  echo $td . outFormat($zones[$j]->cf, 1) . '</td>';
	  echo $td . outFormat($zones[$j]->irrig_uniformity, -1) . '</td>';
	} else if ($zoneDispType == "LF") {
	  echo $td . outFormat($zones[$j]->rtlf, 1) . '</td>';
          echo $td . outFormat($zones[$j]->etlf, 2) . '</td>';
          echo $td . outFormat($zones[$j]->etlfratio, -1) . '</td>';
	}
	#irrig rate - dependent on zoneType
	echo $td . outFormat($zones[$j]->getIrrigRate(), 2);
	echo ' ' . $zones[$j]->getIrrigRateUnits() . '</td>';
        echo '</tr>';
      }
?>
      </table>
      <br><br>
      <center><b>Rain</b> (<a href="javascript:popupText(raintabletext);" onMouseOver="if (!IsIE()) ddrivetip(raintabletext,'99ffaa',550);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)</center>
      <table border=1 align="center" class="manage">
      <tr>
        <th valign="top" class="manageth-nobg">Zone</th>
        <th valign="top" class="manageth">Name</th>
	<th valign="top" class="manageth">Water<br>deficit<br>(inch)</th>
        <th valign="top" class="manageth">Rain total<br>(inch)</th>
        <th valign="top" class="manageth">Eff rain<br>total (inch)</th>
	<th valign="top" class="manageth">Water<br>deficit<br>offset (inch)</th>
        <th valign="top" class="manageth">% Water<br>deficit<br>reduction</th>
      </tr>
<?php
      for ($j = 0; $j < $nzones; $j++) {
        if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . '<a href="editZone.php?zid=' . $zones[$j]->getId() . '" title="Edit this zone">' . $zones[$j]->zoneNumber . '</a></td>';
        echo $td . $zones[$j]->getName() . '</td>';
	echo $td . outFormat($zones[$j]->water_deficit_cm/2.54, 2) . '</td>';
	echo $td . outFormat($zones[$j]->rain_cm/2.54, 2) . '</td>';
        echo $td . outFormat(round($zones[$j]->eff_rain_cm/2.54, 3), 2) . '</td>';
	echo $td . outFormat(round($zones[$j]->water_deficit_offset_cm/2.54, 3), 2) . '</td>';
        echo $td . outFormat(round($zones[$j]->water_deficit_red, 3), -1) . '</td>';
        echo '</tr>';
      }
?>
      </table>
<?php
    if ($doThreshTable) {
?>
      <br><br>
      <center><b>Thresholds</b></center>
      <table border=1 align="center" class="manage">
      <tr>
        <th valign="top" class="manageth-nobg">Zone</th>
        <th valign="top" class="manageth">Name</th>
	<th valign="top" class="manageth">Threshold (%)</th>
	<th valign="top" class="manageth">Avail Water (in)</th>
	<th valign="top" class="manageth">Threshold (in)</th>
	<th valign="top" class="manageth">Deficit (in)</th>
	<th valign="top" class="manageth">ETC cdr</th>
	<th valign="top" class="manageth">Irrigate?</th>
      </tr>
<?php
      $nthresh = 0;
      for ($j = 0; $j < $nzones; $j++) {
 	if ($zones[$j]->irrigSchedule != "threshold") continue;
	if ($nthresh%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
	$thresh_in = $zones[$j]->thresholdFactor/100.0*$zones[$j]->awin;
        echo '<tr>';
        echo $td . '<a href="editZone.php?zid=' . $zones[$j]->getId() . '" title="Edit this zone">' . $zones[$j]->zoneNumber . '</a></td>';
        echo $td . $zones[$j]->getName() . '</td>';
	echo $td . outFormat($zones[$j]->thresholdFactor, -1) . '</td>';
	echo $td . outFormat($zones[$j]->awin, 2) . '</td>';
	echo $td . outFormat($thresh_in, 2) . '</td>';
	echo $td . outFormat($zones[$j]->cum, 2) . '</td>';
	echo $td . outFormat($zones[$j]->contETcdrin, 2) . '</td>';
	if ($zones[$j]->cum+$zones[$j]->contETcdrin < $thresh_in) {
	  echo $tdred . 'NO' . '</td>';
	} else {
	  echo $tdgreen . 'YES' . '</td>';
	}
	echo '</tr>';
      }
?>
      </table>
<?php
    }
//  } else {
//    require('login_code.php');
  } 
?>
<?php require('footer.php'); ?>
