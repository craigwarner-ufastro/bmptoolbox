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
  calculatorButton.src = "images/button-sel-calculator.png";
  off('calculator');
</script>
<h2><img src="images/cirrig.png" alt="cirrig logo" title="cirrig" align="absmiddle">
<?php
  echo '<span style="margin-left: 50px;">Irrigation for ' . date("M j, Y") . "</span></h2>";
  if ($isLoggedIn) {
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
    $sql = "select distinct zones.wsid, weatherStations.location from zones INNER JOIN weatherStations on zones.wsid=weatherStations.wsid where zones.uid=" . $_SESSION['bmpid'];
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
    echo '<form method="POST" name="updateWeather" action="calculator.php">';
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
      $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.wsid=$wsid AND zones.uid=". $_SESSION['bmpid'];
    } else {
      $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid where zones.uid = " . $_SESSION['bmpid'];
    }
    $sql .= " order by $orderby";

    #Get zones
    $zones = Array();
    $out = $mysqli_db->query($sql);
    $doThreshTable = false;
    $nzones = 0;
    $wTime = "live";
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
        //throw away zones that don't match zoneDispType
        if (substr($row['zoneType'], 0, 2) != $zoneDispType) continue;
	if ($nzones == 0) {
	  if (isset($row['weatherTime'])) $wTime = $row['weatherTime'];
	}
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

    #Override default if form is submitted
    if (isset($_POST['weatherTime'])) $wTime = $_POST['weatherTime'];

    #Get weather
    $firstHour = -1;
    if ($wTime == "live") {
      $hourVal = "NOW()";
      $firstHour = (int)date("H")+1;
    } else if ($wTime == "auto") {
      if (strtotime($zones[0]->getAutoRunTime()) > mktime()) {
        $hourVal = "'" . date("Y-m-d", mktime()-86400) . ' ' . $zones[0]->getAutoRunTime() . "'";
      } else {
        $hourVal = "'" . date("Y-m-d", mktime()) . ' ' . $zones[0]->getAutoRunTime() . "'";
      }
      $temp = explode(':', $zones[0]->getAutoRunTime());
      $firstHour = (int)($temp[0])+1;
    } else {
      if ($wTime > (int)date("H")) {
	$hourVal = "'" . date("Y-m-d", mktime()-86400) . " $wTime:00:00" . "'"; 
      } else {
        $hourVal = "'" . date("Y-m-d", mktime()) . " $wTime:00:00" . "'"; 
      }
      $firstHour = $wTime+1;
    }

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
	    echo "Could not find weather for " . date("H:00:00", strtotime($row['hour'])) . ". Using data from " . date("M j", strtotime($row['hour'])) . " instead.<br>"; 
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
    $zones[0]->setFirstHour($firstHour);

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
	$zones[$i]->setFirstHour($firstHour);
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
    <option name="LF" value="LF" <?php if ($zoneDispType == "LF") echo "selected";?>>LF 
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
    echo '<b>Use weather from: </b>';
    echo '<select name="weatherTime" onChange="document.updateWeather.submit();">';
    echo '<option name="live" value="live">Live - Past 24 hours';
    echo '<option name="auto" value="auto"';
    if ($wTime == 'auto') echo ' selected';
    echo '>At automated time for each zone';
    for ($j = 0; $j < 24; $j++) {
      if ($j%12 == 0) {
	$time = "12:00";
      } else {
	$time = (string)($j%12) . ":00";
      }
      if ($j < 12) $time .= " AM"; else $time .= " PM";
      echo '<option name="' . $j . '" value="' . $j . '"';
      if ($wTime == (string)$j) echo ' selected';
      echo '>' . $time;
    }
    echo '</select>';
    echo "<h3>Past day's weather for <a href=\"viewWeather.php?wsid=$wsid\">$wsLocation</a></h3>";
    echo '<table border=0><tr>';
    echo '<td>Solar radiation:</td>';
    echo '<td><input type="text" name="solar_rad" size=5 value="' . outFormat($solar_wmhr, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.solar_rad_mj.value = Math.round((parseFloat(this.value)*0.0864)*1000.)/1000.;"> W/m<sup>2</sup></td>';
    echo '<td width=50></td>';
    echo '<td><input type="text" name="solar_rad_mj" size=5 value="' . outFormat($solar_wmhr*0.0864, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.solar_rad.value = Math.round((parseFloat(this.value)/0.0864)*1000.)/1000.;"> MJ/m<sup>2</sup></td></tr>';

    echo '<tr><td>Min temperature:</td>';
    echo '<td><input type="text" name="tmin" size=5 value="' . outFormat($tmin_f, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.tmin_c.value = Math.round((parseFloat(this.value)-32)/1.8*1000.)/1000.;"> F</td>';
    echo '<td></td>';
    echo '<td><input type="text" name="tmin_c" size=5 value="' . outFormat(($tmin_f-32)/1.8, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.tmin.value = Math.round((parseFloat(this.value)*1.8+32)*1000.)/1000.;"> C</td>';

    echo '<tr><td>Max temperature:</td>';
    echo '<td><input type="text" name="tmax" size=5 value="' . outFormat($tmax_f, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.tmax_c.value = Math.round((parseFloat(this.value)-32)/1.8*1000.)/1000.;"> F</td>';
    echo '<td></td>';
    echo '<td><input type="text" name="tmax_c" size=5 value="' . outFormat(($tmax_f-32)/1.8, 1) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.tmax.value = Math.round((parseFloat(this.value)*1.8+32)*1000.)/1000.;"> C</td>';

    echo '<tr><td>Rainfall:</td>';
    echo '<td><input type="text" name="rain" size=5 value="' . outFormat($rain_in,2) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.rain_cm.value = Math.round((parseFloat(this.value)*2.54)*1000.)/1000.;"> inches</td>';
    echo '<td></td>';
    echo '<td><input type="text" name="rain_cm" size=5 value="' . outFormat($rain_in*2.54,2) . '" onkeypress="return validDec(event);" onChange="document.updateWeather.rain.value = Math.round((parseFloat(this.value)/2.54)*1000.)/1000.;"> cm</td>';

    echo '<tr><td colspan=5 align="center">';
    echo '<input type="submit" name="submitWeather" value="Update Weather" onClick="document.updateWeather.isSubmitted.value=2;">';
    echo '<input type="reset" name="reset" value="Reset">';
    echo '</td></tr>';
    echo '</table><br><br>';
    echo '</form>';
    
?>
    <div style="margin-left: 0px;" align="center">
    <form action="calculator.php" method="post" name="historyForm">
    <input type="hidden" name="historyToSave" value=0>
    <input type="hidden" name="zoneDispType" value="<?php echo $zoneDispType; ?>">
    <input type="hidden" name="wsid" value="<?php echo $wsid; ?>">
    <input type="button" onClick="saveHistory(-1);" name="saveAll" value="Save All Histories"><br><br>
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
      <th valign="top" class="manageth">History</th>
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

	#Update weather button clicked?
	if (isset($_POST['isSubmitted']) && $_POST['isSubmitted'] == 2) {
	  $rainPerPot_in = $zones[$j]->getRain()*(1+$zones[$j]->cf)/2.;
	  #cum in inches
	  if ($zoneDispType == "ET") {
	    $cum = $zones[$j]->contETin - $rainPerPot_in;
	  } else $cum = $zones[$j]->lfIrrig_in - $rainPerPot_in;
	  if ($cum < 0) $cum = 0;
          $irrig_in = ($cum+$zones[$j]->lf*$cum/(1.0-$zones[$j]->lf))/$zones[$j]->cf*100.0/$zones[$j]->irrig_uniformity;
          $irrig_time = $irrig_in/$zones[$j]->irrig_in_hr*60.0;
	}

        #process different irrig schedules and min irrig here
        $irrigation = $zones[$j]->processIrrigSchedule($irrig_in, $irrig_time);
        $irrig_in = $irrigation['irrig_in'];
        $irrig_time = $irrigation['irrig_time'];
        $deficit_in = $irrigation['deficit_in'];
        $irrig_output = $irrigation['irrig_output'];
        if ($irrigation['doThreshTable']) $doThreshTable = True;

	#save zone if requested
	$histSaved = false;
	if (isset($_POST['historyToSave'])) {
	  if ($_POST['historyToSave'] == $zones[$j]->zid || $_POST['historyToSave'] == -1) {
	    if ($zoneDispType == "ET") {
	      $sql = "insert into zoneHistory(zid, uid, histTime, pctCover, plantHeight_in, plantWidth_in, containerSpacing_in, spacing, irrig_in, irrig_minutes, etc_in, cf, irrig_uniformity, irrig_in_per_hr, solar_radiation, max_temp, min_temp, rain_in, wsid, source)";
	      $sql .= " VALUES (" . $zones[$j]->zid . ", " . $zones[$j]->uid . ", '" . date('Y-m-d H:i:s') . "', " . $zones[$j]->pctCover . ", " . $zones[$j]->plantHt . ", " . $zones[$j]->plantWd . ", " . $zones[$j]->contSpacing . ", '" . $zones[$j]->spacingString . "', $irrig_in, $irrig_time, " . $zones[$j]->contETin . ", " . $zones[$j]->cf . ", " . $zones[$j]->irrig_uniformity . ", " . $zones[$j]->irrig_in_hr . ", " . $zones[$j]->solar_wmhr . ", " . $zones[$j]->tmax_f . ", " . $zones[$j]->tmin_f . ", " . $zones[$j]->getRain() . ", " . $zones[$j]->wsid . ", 3);";
	    } else if ($zoneDispType == "LF") {
              $sql = "insert into zoneHistory(zid, uid, histTime, irrig_in, irrig_minutes, rtlf_min, etlf_in, etratio, et0_in, irrig_in_per_hr, solar_radiation, max_temp, min_temp, rain_in, wsid, source)";
              $sql .= " VALUES (" . $zones[$j]->zid . ", " . $zones[$j]->uid . ", '" . date('Y-m-d H:i:s') . "', $irrig_in, $irrig_time, " . $zones[$j]->rtlf . ", " . $zones[$j]->etlf . ", " . $zones[$j]->etlfratio . ", " . $zones[$j]->et0 . ", " . $zones[$j]->irrig_in_hr . ", " . $zones[$j]->solar_wmhr . ", " . $zones[$j]->tmax_f . ", " . $zones[$j]->tmin_f . ", " . $zones[$j]->getRain() . ", " . $zones[$j]->wsid . ", 3);";
	    }
	    $out = $mysqli_db->query($sql);
	    if ($out !== false) $histSaved = true;
	  }
	} 

        if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . '<a href="editZone.php?zid=' . $zones[$j]->getId() . '" title="Edit this zone">' . $zones[$j]->zoneNumber . '</a></td>';
        echo $td . $zones[$j]->getName() . '</td>';
        echo $td . $zones[$j]->getPlant() . '</td>';
        echo $td . $zones[$j]->getIrrigSchedule() . '</td>';
        echo $td;
        if (isset($_POST['isSubmitted']) && $_POST['isSubmitted'] == 2) {
          echo "Custom";
        } else if ($wTime == "live") {
          echo date("H:i:s");
        } else if ($wTime == "auto") {
          echo $zones[$j]->getAutoRunTime();
        } else {
          echo "$wTime:00:00";
        }
        echo '</td>';
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
	if ($histSaved) {
	  echo $td . '<font color="green">Saved</font></td>';
	} else echo $td . '<input type="button" onClick="saveHistory(' . $zones[$j]->getId() . ');" name="Save" value="Save"></td>';
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
  } else {
    require('login_code.php');
  }
?>
<?php require('footer.php'); ?>
