<?php
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      $runfile = "data/run" . $id . ".txt";
      if ($nruns == 4) {
        $xtickv='0.6,1.6,2.6,3.6';
        $xextrema='0,4';
      } else if ($nruns == 3) {
        $xtickv='0.6,1.6,2.6';
        $xextrema='0,3';
      } else {
        $xtickv='0.6,1.6';
        $xextrema='0,2';
      }

      $style = 3;
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) $style=0;
      if (strpos($pagename, "fert") === false) $fert = $_POST['FERT'];
      $pctn = $_POST['PCT_N'];
      $pctp = $_POST['PCT_P'];
      #$irrcost = $_POST['irrcost']/100.;
      #if ($pagename == "fert") $fertcost = $_POST['fertcost'];
      if (strpos($pagename, "location") === false) $wfname = $_POST['WFNAME'];
      if (strpos($pagename, "date") === false) $pltdoy = $_POST['PLT_DOY'];
      if (strpos($pagename, "irr") === false) $sched = $_POST['SCHED'];
      if (strpos($pagename, "date") !== false) {
        $doymin = $_POST['PLT_DOY_1'];
        $doymax = $_POST['PLT_DOY_1'];
        for ($j = 1; $j < $nruns; $j++) {
          $doymin = min($doymin, $_POST['PLT_DOY_' . ($j+1)]);
          $doymax = max($doymax, $_POST['PLT_DOY_' . ($j+1)]);
        }
        $yrs = findValidYears($wfname, $doymin);
        $startYr = $yrs[0];
        $yrs = findValidYears($wfname, $doymax);
        $endYr = $yrs[1];
      } else if (strpos($pagename, "location") !== false) {
	$yrs = findValidYears($_POST['WFNAME_1'], $pltdoy);
	$startYr = $yrs[0];
	$endYr = $yrs[1];
	for ($j = 1; $j < $nruns; $j++) {
	  $yrs = findValidYears($_POST['WFNAME_' . ($j+1)], $pltdoy);
	  $startYr = max($startYr, $yrs[0]);
	  $endYr = min($endYr, $yrs[1]);
	}
      } else {
	$yrs = findValidYears($wfname, $pltdoy);
	$startYr = $yrs[0];
	$endYr = $yrs[1];
      }
      if (isset($_POST['START_YR'])) $startYr = $_POST['START_YR'];

      require("isSavedRun.php");

      require("output-save.php");

      //compnames should contain list of variable names to be replaced
      //comp vars should contain replacements for each run
      for ($n = 0; $n < $nruns; $n++) {
	if (strpos($pagename, "fert") !== false) $fert = $compvals[0][$n];
	if (strpos($pagename, "irr") !== false) $sched = $compvals[1][$n];
	if (isset($_POST['START_YR_' . ($n+1)])) $startYr = $_POST['START_YR_' . ($n+1)];
	$infile = $pfix . $id . ($n+1) . "_P_spec.txt";
	$f = fopen($infile, 'wb');
	for ($j = 0; $j < count($infields); $j++) {
	   if (isset($_POST[$infields[$j][0]])) {
	      $temp = $_POST[$infields[$j][0]];
           } else if (strpos($infields[$j][0], "IRR") !== false && isset($_POST[$infields[$j][0] . '_' . $n])) {
              $temp = $_POST[$infields[$j][0] . '_' . $n];
	   } else {
	      $idx = array_search($infields[$j][0], $compnames);
	      if ($idx !== false && isset($compvals[$idx][$n])) {
		$temp = $compvals[$idx][$n];
	      } else {
		$temp = $def[$j][0];
	      }
	   }
           if (strpos($temp, "/") !== false) $temp = '"' . $temp . '"';
	   for ($l = 1; $l < count($infields[$j]); $l++) {
	      if ($infields[$j][$l] === 'START_YR') $def[$j][$l] = $startYr;
	      if ($infields[$j][$l] === 'END_YR') $def[$j][$l] = $endYr;
	      if (isset($_POST[$infields[$j][$l]])) {
		$temp .= "\t" . $_POST[$infields[$j][$l]];
              } else if (strpos($infields[$j][$l], "IRR") !== false && isset($_POST[$infields[$j][$l] . '_' . $n])) {
                $temp .= "\t" . $_POST[$infields[$j][$l] . '_' . $n];
	      } else {
		$idx = array_search($infields[$j][$l], $compnames);
		if ($idx !== false && isset($compvals[$idx][$n])) {
		  $temp .= "\t" . $compvals[$idx][$n];
		} else {
		  $temp .= "\t" . $def[$j][$l];
		}
	      }
	   }
	   $template[$j*4+2] = $temp . "\n";
	}
	for ($j = 0; $j < count($template); $j++) {
	   fwrite($f, $template[$j]);
	}
	fclose($f);
	$f = fopen($runfile, 'wb');
	fwrite($f, '"' . $pfix . $id . ($n+1) . '"' . "\n");
	fclose($f);
	//echo "Run " . ($n+1) . ":<br>";
	$p = popen("../driver/a.out < " . $runfile, "r");
	while (!feof($p)) {
	   $read = fread($p,1024);
	   flush();
	}
	pclose($p);

	//yearly
	$yearlyfiles = array("summaryoutput.txt", "summaryarea.txt");
	for ($i = 0; $i < count($yearlyfiles); $i++) {
           //read file and parse into arrays
           $yearly = file($pfix . $id . ($n+1) . "_" . $yearlyfiles[$i]);
           $temp = trim($yearly[2]);
           $yfields = preg_split("/\s+/", $temp);
           $yearly[2] = implode("\t",$yfields) . "\n";
           for ($j = 3; $j < count($yearly); $j++) {
              $temp = trim($yearly[$j]);
              $temp = preg_split("/\s+/", $temp);
              $yearly[$j] = implode("\t",$temp) . "\n";
              for ($l = 0; $l < count($temp); $l++) {
		if ($temp[$l] == "") continue;
		$yvals[$l][] = $temp[$l];
              }
           }

           //calculate stats
           for ($j = 1; $j < count($yvals); $j++) {
              $currArr = $yvals[$j];
              $ymax[$j-1] = max($currArr);
              $ymin[$j-1] = min($currArr);
              $ymean[$j-1] = substr('' . arraymean($currArr), 0, 7);
              $ymedian[$j-1] = arraymedian($currArr);
              $ystddev[$j-1] = substr('' . arraystddev($currArr), 0, 7);
           }

           //write new summary file with stats
           $f = fopen($pfix . $id . ($n+1) . "_" . $yearlyfiles[$i],"wb");
           for ($j = 0; $j < count($yearly); $j++) {
	      fwrite($f, $yearly[$j]);
	   }
           fwrite($f, "Min");
           for ($j = 0; $j < count($ymin); $j++) {
              fwrite($f, "\t" . $ymin[$j]);
           }
           fwrite($f, "\n");
           fwrite($f, "Mean");
           for ($j = 0; $j < count($ymean); $j++) {
              fwrite($f, "\t" . $ymean[$j]);
           }
           fwrite($f, "\n");
           fwrite($f, "Median");
           for ($j = 0; $j < count($ymedian); $j++) {
              fwrite($f, "\t" . $ymedian[$j]);
           }
           fwrite($f, "\n");
           fwrite($f, "StdDev");
           for ($j = 0; $j < count($ystddev); $j++) {
              fwrite($f, "\t" . $ystddev[$j]);
           }
           fwrite($f, "\n");
           fwrite($f, "Max");
           for ($j = 0; $j < count($ymax); $j++) {
              fwrite($f, "\t" . $ymax[$j]);
           }
           fwrite($f, "\n");
           fclose($f);
	   #if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
	   #   $idx = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15); 
	   #} else {
	   #   $idx = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
	   #}
	   $idx = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

	   //calculate vals to plot
	   if ($i == 0) {
	      $xplot[$n] = ($n+1); 
	      if (strpos($pagename, "fert") !== false) {
		if (strpos($pagename, "irr") !== false) {
		  $fertplot[$n] = $fert*$pctn*16.86/$subVol;
		} else {
		  #comparion_fert
		  #Runoff (gal/container)
		  $xplot[$n] = $fert*$pctn*16.86/$subVol; 
		  $yplot[$n][$idx[4]] = $ymean[6]*0.264; #Runoff (gal/ container)
		}
	      }
              $yplot[$n][$idx[0]] = $ymean[9]/2.54; #Plant height (in)
	      $yplot[$n][$idx[1]] = $ymean[1]/7; #Time to finish (weeks)
	      $yplot[$n][$idx[5]] = $ymean[7]; #Runoff N (g/ container)
	      $yplot[$n][$idx[6]] = $ymean[7]*100/($fert*$pctn/100.); #Nloss (%) 
              $yplot[$n][$idx[7]] = $fert*$pctn/100.; #N rate (g N/ container)
	      $yplot[$n][$idx[8]] = $ymean[8]; #Runoff N conc.
              $yplot[$n][$idx[9]] = $ymean[13]; #Runoff P (g/ container)
              $yplot[$n][$idx[10]] = $ymean[13]*100/($fert*$pctp/100.); #Ploss (%) 
              $yplot[$n][$idx[11]] = $fert*$pctp/100.; #P rate (g N/ container)
              $yplot[$n][$idx[12]] = $ymean[14]; #Runoff P conc.
              #$yplot[$n][$idx[13]] = $ymean[3]*0.264*$irrcost; #Irrig cost
              #$yplot[$n][$idx[14]] = $ymean[16]; #Final NSUF
              $yplot[$n][$idx[13]] = $ymean[16]; #Final NSUF

              #if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
		#comparison_fert
		#$yplot[$n][13] = $fert*$pctn/100.*$fertcost*2.205; #Fert cost
	      #}
	   } else if ($i == 1) {
              $yplot[$n][$idx[3]] = $ymean[3]/2.54; #Irrig (in)
	      if (strpos($pagename, "fert") === false || strpos($pagename, "irr") !== false) {
		#all but comparison_fert
		$yplot[$n][$idx[4]] = $ymean[6]/2.54; #Runoff (in)
	      }
	      $yplot[$n][$idx[2]] = $ymean[2]/2.54; #Rain (in)
	   }

           //free memory
           unset($yvals);
           unset($yearly);
           unset($keys);
           unset($ymax);
           unset($ymin);
           unset($ymean);
           unset($ymedian);
           unset($ystddev);
	}
      }
?>
