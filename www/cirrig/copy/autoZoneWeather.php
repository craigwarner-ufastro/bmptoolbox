<?php
  require('dbcnx.php');
  date_default_timezone_set('America/New_York');
  #mysql_select_db("mybmp");

  #FIRST update zone histories
  $sql = "select * from zones INNER JOIN weatherStations on zones.wsid = weatherStations.wsid";
  #Get zones
  $zones = Array();
  $out = $mysqli_db->query($sql);
  $nzones = 0;
  if ($out != false) {
    while ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $zones[] = $row;
      $nzones++;
    }
  }

  #Loop over all zones
  for ($i = 0; $i < $nzones; $i++) {
    #Get weather
    $sql = "select * from hourlyWeather where wsid=" . $zones[$i]['wsid'] . " AND hour > NOW()-INTERVAL 25 HOUR ORDER BY hour;";
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
	$sql = "select * from hourlyWeather where wsid=" . $zones[$i]['wsid'] . " AND HOUR(hour)=$j ORDER BY hour DESC LIMIT 1;";
        $out = $mysqli_db->query($sql);
        if ($out != false) {
	  if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	    #echo "Could not find weather for " . date("H:00:00", strtotime($row['hour'])) . ". Using data from " . date("M j", strtotime($row['hour'])) . " instead.\n"; 
	    $hourly[$j] = $row;
	    $nhourly++;
	  }
	}
      }
    }

    if ($nhourly < 24) {
      #echo "ERROR: Weather station $wsLocation only has $nhourly hourly weather datapoints!\n";
      exit(0);
    }

    $doy = intval(date("z"))+1;
    #Accumulate hourly weather
    $tmax_f = $hourly[0]['max_temp'];
    $tmin_f = $hourly[0]['min_temp'];
    $solar_wmhr = $hourly[0]['solar_radiation'];
    $rain_in = $hourly[0]['rain_in'];
    for ($j = 1; $j < 24; $j++) {
      if (!isset($hourly[$j])) continue;
      $tmax_f = max($tmax_f, $hourly[$j]['max_temp']);
      $tmin_f = min($tmin_f, $hourly[$j]['min_temp']);
      $solar_wmhr += $hourly[$j]['solar_radiation'];
      $rain_in += $hourly[$j]['rain_in'];
    }
    $solar_wmhr /= 24.0;

    $contDiam = $zones[$i]['containerDiam_in'];
    $irrig_in_hr = $zones[$i]['irrig_in_per_hr'];
    $irrig_capture = $zones[$i]['irrigCaptureAbility'];
    $irrig_uniformity = $zones[$i]['irrig_uniformity'];
    $elev_ft = $zones[$i]['elevation_ft'];
    $lat_deg = $zones[$i]['lattitude'];
    $long_deg = $zones[$i]['longitude'];

    $plantHt = $zones[$i]['plantHeight_in'];
    $plantWd = $zones[$i]['plantWidth_in'];
    $pctCover = $zones[$i]['pctCover'];
    $contSpacing = $zones[$i]['containerSpacing_in'];
    $spacing = 1; #square
    if ($zones[$i]['spacing'] == 'offset') {
      $spacing = 0.866; #triangular
    }
    $shade = $zones[$i]['shade'];
    $lf = $zones[$i]['leachingFraction']/100.0;

    require('formula.php');


    #Add in rain
    $cum = 0;
    $firstHour = (int)date("H")+1;
    #Loop over hours to find cumulative deficit
    for ($h = $firstHour; $h < $firstHour+24; $h++) {
      $scaledET = $hourly[$h%24]['solar_radiation']/(24*$solar_wmhr)*$contETin*2.54;
      #Changed from (1+cf)/2 to cf 8/19/13
      $rainPerPot = $hourly[$h%24]['rain_in']*2.54*$cf;
      #nullify rain for prod area = plastic
      if ($zones[$i]['productionArea'] == 'plastic') $rainPerPot = 0;
      $cum += $scaledET-$rainPerPot;
      if ($cum < 0) $cum = 0;
    }
    #Re-calculate irrig_in and irrig_time
    #$irrig_in = $cum/2.54/$cf*100.0/$irrig_uniformity;
    #Apply LF here too
    $cum /= 2.54; #Convert to inches
    $irrig_in = ($cum+$lf*$cum/(1.0-$lf))/$cf*100.0/$irrig_uniformity;
    $irrig_time = $irrig_in/$irrig_in_hr*60.0;

    #Update zone history
    $sql = "insert into zoneHistory(zid, uid, histTime, pctCover, plantHeight_in, plantWidth_in, containerSpacing_in, spacing, irrig_in, irrig_minutes, etc_in, cf, irrig_uniformity, irrig_in_per_hr, solar_radiation, max_temp, min_temp, rain_in, wsid, source)";
    $sql .= " VALUES (" . $zones[$i]['zid'] . ", " . $zones[$i]['uid'] . ", '" . date('Y-m-d H:i:s') . "', $pctCover, $plantHt, $plantWd, $contSpacing, '" . $zones[$i]['spacing'] . "', $irrig_in, $irrig_time, $contETin, $cf, $irrig_uniformity, $irrig_in_hr, $solar_wmhr, $tmax_f, $tmin_f, $rain_in, " . $zones[$i]['wsid'] . ", 1);";
    #echo $sql . "\n";
    $out = $mysqli_db->query($sql);
  }

  #SECOND update daily weather
  $sql = "select wsid, uid, avg(solar_radiation) as sr, max(max_temp) as maxt, min(min_temp) as mint, sum(rain_in) as rain, date(hour) as day, count(*) as N from hourlyWeather where concat(wsid, ' ', date(hour)) not in (select concat(wsid, ' ', date) from weather) group by date(hour), wsid having n=24;";
  $out = $mysqli_db->query($sql);
  if ($out != false) {
    while ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $wsid = $row['wsid'];
      $uid = $row['uid']; 
      $date = $row['day'];
      $solarRad = $row['sr'];
      $tmax = $row['maxt'];
      $tmin = $row['mint'];
      $rain = $row['rain'];
      $npoints = $row['N'];
      $sqlIns = "insert into weather(wsid, uid, date, solar_radiation, max_temp, min_temp, rain_in, npoints)";
      $sqlIns .= " VALUES ($wsid, $uid, '$date', $solarRad, $tmax, $tmin, $rain, $npoints)";
      $outIns = $mysqli_db->query($sqlIns);
    }
  }
?>
