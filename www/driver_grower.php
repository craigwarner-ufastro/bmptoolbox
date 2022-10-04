<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Grower Tool</title>
<?php
  require('growerGraphFuncs.php');
  require('header.php');
?>

<h1 class="h1-container">Grower Tool<font color="#ffffff" class="h1-shadow">Grower Tool</font></h1>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = "grower";

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $id = $_GET['runid'];
      $pfix = "data/" . $bmpusername . "/";
      echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
      if (file_exists($pfix . $id . '_P_spec.txt')) {
	$spec = file($pfix . $id . '_P_spec.txt');
	for ($l = 0; $l < $npspec; $l++) {
	  $temp = trim($spec[$l*4+1]);
          $tempnames = preg_split("/\s+/", $temp);
          $temp = trim($spec[$l*4+2]);
          $tempvals = preg_split("/\s+/", $temp);
          for ($i = 0; $i < count($tempnames); $i++) {
	    $input[$tempnames[$i]] = $tempvals[$i];
	  }
	}
      }
      $runfile = "data/run" . $id . ".txt";
      $fert = $input['FERT'];
      $pctn = $input['PCT_N'];
      $pctp = $input['PCT_P'];
      $wfname = $input['WFNAME'];
      $pltdoy = $input['PLT_DOY'];
      $sched = $input['SCHED'];
      $startYr = $input['START_YR'];
      $endYr = $input['END_YR'];
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      require("isSavedRun.php");
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      $pfix = "data/" . $bmpusername . "/";
      $f = opendir("data");
      while ($temp = readdir($f)) {
        if (preg_match("/i{$id}/i", $temp)) {
        #if (eregi('i' . $id, $temp)) {
           $rfiles[] = $temp;
        }
      }
      rename("data/run" . $id . ".txt", $pfix . "run" . $_POST['savename'] . ".txt");
      for ($j = 0; $j < count($rfiles); $j++) {
        rename('data/' . $rfiles[$j], $pfix . str_replace("i" . $id, $_POST['savename'], $rfiles[$j]));
      }
      $id = $_POST['savename'];
      $f = fopen($pfix . $id . '_inputs.txt','ab');
      fwrite($f, "savename\t" . $_POST['savename'] . "\n");
      fclose($f);
      echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
      if (file_exists($pfix . $id . '_P_spec.txt')) {
        $spec = file($pfix . $id . '_P_spec.txt');
        for ($l = 0; $l < $npspec; $l++) {
          $temp = trim($spec[$l*4+1]);
          $tempnames = preg_split("/\s+/", $temp);
          $temp = trim($spec[$l*4+2]);
          $tempvals = preg_split("/\s+/", $temp);
          for ($i = 0; $i < count($tempnames); $i++) {
            $input[$tempnames[$i]] = $tempvals[$i];
          }
        }
      }
      $runfile = "data/run" . $id . ".txt";
      $fert = $input['FERT'];
      $pctn = $input['PCT_N'];
      $pctp = $input['PCT_P'];
      $wfname = $input['WFNAME'];
      $pltdoy = $input['PLT_DOY'];
      $sched = $input['SCHED'];
      $startYr = $input['START_YR'];
      $endYr = $input['END_YR'];
   } else $isSub = 3;

   if ($isSub == 3) {
      $temp = explode(" ", microtime(true));
      $timestamp = $temp[0];//+$temp[1];
      $id = substr($timestamp, 6, 7);
?>

<ul>
<li>Use this tool to get detailed output for one set of inputs. For
comparing two or more runs, the <a href="comparisons.php">Comparisons</a>
tool is recommended.
<li>After selecting inputs hit the Submit button at bottom to run
simulation and view output
</ul>
<form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="return checkRuns(true);" target="output">
<input type="hidden" name="isSubmitted" value=1>
<input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
<input type="hidden" name="id" value="<?php echo $id; ?>">
<table border=0 name="inputTable">

<?php 
  require('user-save.php');
  require('common.php'); 
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $tempFile = $_POST['template'];
      $template = file($tempFile);
      for ($j = 0; $j < $npspec; $j++) {
	$temp = trim($template[$j*4]);
	$labels[$j] = $temp;
	$temp = trim($template[$j*4+1]);
	$infields[$j] = preg_split("/\s+/", $temp);
	$temp = trim($template[$j*4+2]);
	$def[$j] = preg_split("/\s+/", $temp);
      }

      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      $runfile = "data/run" . $id . ".txt";
      $fert = $_POST['FERT'];
      $pctn = $_POST['PCT_N'];
      $pctp = $_POST['PCT_P'];
      $wfname = $_POST['WFNAME'];
      $pltdoy = $_POST['PLT_DOY'];
      $sched = $_POST['SCHED'];
      $yrs = findValidYears($wfname, $pltdoy);
      $startYr = $yrs[0];
      if (isset($_POST['START_YR'])) $startYr = $_POST['START_YR'];
      $endYr = $yrs[1];
      if (isset($_POST['END_YR'])) $endYr = $_POST['END_YR'];
      require("isSavedRun.php");

      require("output-save.php");

      $infile = $pfix . $id . "_P_spec.txt";
      $f = fopen($infile, 'wb');
      for ($j = 0; $j < count($infields); $j++) {
	if (isset($_POST[$infields[$j][0]])) {
	   $temp = $_POST[$infields[$j][0]];
	   if (strpos($temp, "/") !== false) $temp = '"' . $temp . '"';
	} else {
	   $temp = $def[$j][0];
	}
	for ($l = 1; $l < count($infields[$j]); $l++) {
           if ($infields[$j][$l] === 'START_YR') $def[$j][$l] = $startYr;
           if ($infields[$j][$l] === 'END_YR') $def[$j][$l] = $endYr;
	   if (isset($_POST[$infields[$j][$l]])) {
              if (strpos($_POST[$infields[$j][$l]], "/") !== false) {
                $temp = "\t" . '"' . $_POST[$infields[$j][$l]] . '"';
              } else $temp .= "\t" . $_POST[$infields[$j][$l]];
	   } else {
	      $temp .= "\t" . $def[$j][$l];
	   }
	}
	$template[$j*4+2] = $temp . "\n";
      }
      for ($j = 0; $j < count($template); $j++) {
	fwrite($f, $template[$j]);
      }
      fclose($f);
      $f = fopen($runfile, 'wb');
      fwrite($f, '"' . $pfix . $id . '"' . "\n");
      fclose($f);

      $p = popen("../driver/a.out < " . $runfile, "r");
      while (!feof($p)) {
	$read = fread($p,1024);
	flush();
      }
      pclose($p);

      //Calculate daily stats
      //dailyfiles defined in header
      for ($i = 0; $i < count($dailyfiles); $i++) {
	//read file and parse fields and values into arrays
	$daily = file($pfix . $id . "_" . $dailyfiles[$i] . "output.txt");
	$temp = trim($daily[$skipl[$i]-1]);
	$dfields = preg_split("/\s+/", $temp);
	for ($j = $skipl[$i]; $j < count($daily); $j++) {
	   $temp = trim($daily[$j]);
	   $temp = preg_split("/\s+/", $temp);
	   for ($l = 0; $l < count($temp); $l++) {
	      $dvals[$temp[2]][$l][] = $temp[$l];
	   }
	   $keys[$temp[2]] = $temp[2];
	}

        //calculate max, min, mean, median, stddev
        $maxcount = 0;
	foreach ($keys as $key => $value) {
	   for ($j = 3; $j < count($dvals[$key]); $j++) {
	      $currArr = $dvals[$key][$j];
	      //if < 1/4 of original datapoints, throw out
	      $maxcount = max($maxcount, count($currArr));
	      if (count($currArr) < $maxcount/4) {
		unset($keys[$key]);
	        break;
	      }
	      $dcount[$key] = count($currArr);
	      $dmax[$key][$j-3] = substr('' . max($currArr), 0, 7);
	      $dmin[$key][$j-3] = substr('' . min($currArr), 0, 7);
	      $temp = '' . arraymean($currArr);
	      $epos = stripos($temp, "e-");
	      if ($epos === False) $temp = substr($temp, 0, 7); else {
		$temp = substr($temp, 0, min(4, $epos-1)) . substr($temp, $epos);
	      }
	      $dmean[$key][$j-3] = $temp; 
	      //$dmedian[$key][$j-3] = substr('' . arraymedian($currArr), 0, 7);
	      //$dstddev[$key][$j-3] = substr('' . arraystddev($currArr), 0, 7);
	   }
	}

	//write output file headers
	$f = fopen($pfix . $id . "_" . $dailyfiles[$i] . "stats.txt","wb");
	fwrite($f, $daily[0]);
	fwrite($f, $daily[1]);
	fwrite($f, "\t\t");
	for ($j = 3; $j < count($dfields); $j++) {
	   //fwrite($f, "\tMax\tMin\tMean\tMedian\tStdDev");
	   fwrite($f, "\tMax\tMin\tMean");
	}
	fwrite($f, "\n");
	fwrite($f, $dfields[1] . "\t" . $dfields[2]);
	fwrite($f, "\tNdays");
	for ($j = 3; $j < count($dfields); $j++) {
	   //for ($l = 0; $l < 5; $l++) {
           for ($l = 0; $l < 3; $l++) {
	      fwrite($f, "\t" . substr($dfields[$j], 0, 7));
	   }
	}
	fwrite($f, "\n");

	//write data
        ksort($keys);
	foreach ($keys as $key => $value) {
	   fwrite($f, $dvals[$key][1][0] . "\t" . $dvals[$key][2][0]);
	   fwrite($f, "\t" . $dcount[$key]);
	   for ($j = 0; $j < count($dmax[$key]); $j++) {
	      fwrite($f ,"\t" . $dmax[$key][$j] . "\t" . $dmin[$key][$j] . "\t" . $dmean[$key][$j]);
	      //fwrite($f, "\t" . $dmedian[$key][$j] . "\t" . $dstddev[$key][$j]);
	   }
	   fwrite($f, "\n");
	}
	fclose($f);

	//free memory
	unset($dvals);
	unset($daily);
	unset($keys);
	unset($dmax);
	unset($dmin);
	unset($dcount);
	unset($dmean);
      }

      //yearly
      $yearlyfiles = array("summaryoutput.txt", "summaryarea.txt");
      for ($i = 0; $i < count($yearlyfiles); $i++) {
	//read file and parse into arrays
	$yearly = file($pfix . $id . "_" . $yearlyfiles[$i]);
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
	$f = fopen($pfix . $id . "_" . $yearlyfiles[$i],"wb");
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

   if ($isSub < 3) {
      showOutputButtons();
      require('outputGraphs.php');
   }
   require('footer.php');
?>
