<?php #ini_set('session.cache_limiter', 'nocache');
session_start(); ?>
<html>
<head>
<title>Real-Time Tool</title>
<script language="Javascript">
  function setDayListRT(n, elem) {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    nd = daysInMonth[n];
    var elem = eval("document.paramForm."+elem);
    nsel = elem.selectedIndex;
    elem.options.length=0;
    for (j = 0; j < nd; j++) {
      elem.options[j] = new Option(j+1, j+1, false, false);
    }
    elem.options[nsel].selected = true;
  }

  function adjPlantSize() {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    for (i = 1; i < 4; i++) {
      var monthMenu = eval("document.paramForm.adjSizeMonth"+i);
      var dayMenu = eval("document.paramForm.adjSizeDay"+i);
      var htMenu = eval("document.paramForm.chkht"+i);
      var wMenu = eval("document.paramForm.chkw"+i);
      var chkbox = eval("document.paramForm.enableAdj"+i);
      var CHKDAY = eval("document.paramForm.CHK_DAY"+i);
      var CHKHT = eval("document.paramForm.CHK_HT"+i);
      var CHKW = eval("document.paramForm.CHK_W"+i);
      monthMenu.disabled = chkbox.checked ? false:true;
      dayMenu.disabled = chkbox.checked ? false:true;
      htMenu.disabled = chkbox.checked ? false:true;
      wMenu.disabled = chkbox.checked ? false:true;
      if (!chkbox.checked) {
	CHKDAY.value = -99;
	CHKHT.value = -99;
	CHKW.value = -99;
	continue;
      }

      var month = monthMenu.selectedIndex;
      var day = 0;
      for (j = 0; j < month; j++) {
	day+=daysInMonth[j];
      }
      day += dayMenu.selectedIndex+1;
      day -= (document.paramForm.PLT_DOY.value-1);
      if (day < 0) day += 365;
      CHKDAY.value = day;
      var ht = 2.54*htMenu.options[htMenu.selectedIndex].value;
      var w = 2.54*wMenu.options[wMenu.selectedIndex].value;
      CHKHT.value = ht;
      CHKW.value = w;
    }
  }

  function adjPrune() {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    var i;
    document.paramForm.NOPMAX.value=3;
    for (i = 1; i < 4; i++) {
      var monthMenu = eval("document.paramForm.pruneMonth"+i);
      var dayMenu = eval("document.paramForm.pruneDay"+i);
      var htMenu = eval("document.paramForm.prht"+i);
      var wMenu = eval("document.paramForm.prw"+i);
      var chkbox = eval("document.paramForm.enablePrune"+i);
      var PRDAY = eval("document.paramForm.PR"+i);
      var PRHT = eval("document.paramForm.PR_H"+i);
      var PRW = eval("document.paramForm.PR_W"+i);
      var befht = eval("document.paramForm.bprht"+i);
      var befw = eval("document.paramForm.bprw"+i);
      var chkht = eval("document.paramForm.chkht"+i);
      var chkw = eval("document.paramForm.chkw"+i);
      var chkmon = eval("document.paramForm.adjSizeMonth"+i);
      var chkday = eval("document.paramForm.adjSizeDay"+i);
      var chkchk = eval("document.paramForm.enableAdj"+i);
      monthMenu.disabled = chkbox.checked ? false:true;
      dayMenu.disabled = chkbox.checked ? false:true;
      htMenu.disabled = chkbox.checked ? false:true;
      wMenu.disabled = chkbox.checked ? false:true;
      befht.disabled = chkbox.checked ? false:true;
      befw.disabled = chkbox.checked ? false:true;
      if (!chkbox.checked) {
        PRDAY.value = -99;
        PRHT.value = -99;
        PRW.value = -99;
        continue;
      }

      var month = monthMenu.selectedIndex;
      var day = 0;
      for (j = 0; j < month; j++) {
        day+=daysInMonth[j];
      }
      day += dayMenu.selectedIndex+1;
      day -= (document.paramForm.PLT_DOY.value-1);
      if (day < 0) day += 365;
      PRDAY.value = day;
      var ht = 2.54*htMenu.options[htMenu.selectedIndex].value;
      var w = 2.54*wMenu.options[wMenu.selectedIndex].value;
      PRHT.value = ht;
      PRW.value = w;
      chkht.selectedIndex = befht.selectedIndex;
      chkw.selectedIndex = befw.selectedIndex;
      if (chkbox.checked && !chkchk.checked) chkchk.click(); 
      if (dayMenu.selectedIndex > 0) {
	chkmon.selectedIndex = monthMenu.selectedIndex;
	chkday.selectedIndex = dayMenu.selectedIndex-1;
      } else {
	if (monthMenu.selectedIndex == 0) {
	  chkmon.selectedIndex = 11;
	} else {
	  chkmon.selectedIndex = monthMenu.selectedIndex-1;
        }
	setDayListRT(chkmon.selectedIndex, 'adjSizeDay'+i);
	chkday.selectedIndex = chkday.options.length-1;
      }
    }
    adjPlantSize();
  }

  function adjMove() {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    var tri = document.paramForm.ARRANGE[0].checked;
    for (i = 1; i < 4; i++) {
      var monthMenu = eval("document.paramForm.moveMonth"+i);
      var dayMenu = eval("document.paramForm.moveDay"+i);
      var bMenu = eval("document.paramForm.spacb"+i);
      var wMenu = eval("document.paramForm.spacw"+i);
      var chkbox = eval("document.paramForm.enableMove"+i);
      var MOVE = eval("document.paramForm.MOVE"+i);
      var PTA = eval("document.paramForm.PTA"+i);
      monthMenu.disabled = chkbox.checked ? false:true;
      dayMenu.disabled = chkbox.checked ? false:true;
      bMenu.disabled = chkbox.checked ? false:true;
      wMenu.disabled = chkbox.checked ? false:true;
      if (!chkbox.checked) {
        MOVE.value = -99;
        PTA.value = -99;
        continue;
      }

      var month = monthMenu.selectedIndex;
      var day = 0;
      for (j = 0; j < month; j++) {
        day+=daysInMonth[j];
      }
      day += dayMenu.selectedIndex+1;
      day -= (document.paramForm.PLT_DOY.value-1);
      if (day < 0) day += 365;
      MOVE.value = day;

      var b, w; 
      var spacb = bMenu.options[bMenu.selectedIndex].value;
      var spacw = wMenu.options[wMenu.selectedIndex].value;
      var potDiam = eval(document.paramForm.POTDIAM.value);

      if (spacb == "contdiam") {
	b = 2*potDiam;
      } else if (spacb == "3/4") {
	b = 1.75*potDiam;
      } else b = potDiam + 2.54*spacb;
      if (spacw == "contdiam") {
	w = 2*potDiam;
      } else if (spacw == "3/4") {
	w = 1.75*potDiam;
      } else w = potDiam + 2.54*spacw;

      if (tri) {
        PTA.value = Math.pow(Math.pow(b,2) - Math.pow(w/2, 2), 0.5)*w;
      } else PTA.value = b*w;
    }
  }

  function toggleIrr(j, state) {
    var formel = eval("document.paramForm.IRRHIS"+j);
    formel.disabled = !state; 
    if (state) formel.style.color = "#ff0000"; else formel.style.color = "#000000";
  }
</script>
<?php
  require('growerGraphFuncs.php');
  require('header.php');
?>

<h1 class="h1-container">Real Time Tool<font color="#ffffff" class="h1-shadow">Real Time Tool</font></h1>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
  $pagename = "realtime";
  $latest = file('fawn/latestUpdate.txt');
  echo '<center>Last update: ' . $latest[0] . '</center>';

   if (isset($_POST['isSubmitted'])) {
      if (isset($_POST['B'])) {
	$isSub = 0;
      } else {
	$isSub = 4;
      }
   } else if (isset($_GET['editid']) && $isLoggedIn) {
      $isSub = 4;
      require('setDefault.php');
      foreach ($input as $key=>$value) {
        if ($key == "submit") continue;
        if (strpos($key, "IRRHIS") !== false) break;
	$_POST[$key] = $value;
      }
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $id = $_GET['runid'] . 'b';
      $pfix = "data/" . $bmpusername . "/";
      echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
      if (file_exists($pfix . $id . '_P_spec.txt')) {
	$spec = file($pfix . $id . '_P_spec.txt');
	for ($l = 0; $l < 13; $l++) {
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

      if (file_exists($pfix . $_GET['runid'] . '_P_spec.txt')) {
        $spec = file($pfix . $_GET['runid'] . '_P_spec.txt');
        for ($l = 0; $l < 13; $l++) {
          $temp = trim($spec[$l*4+1]);
          $tempnames = preg_split("/\s+/", $temp);
          $temp = trim($spec[$l*4+2]);
          $tempvals = preg_split("/\s+/", $temp);
          for ($i = 0; $i < count($tempnames); $i++) {
	    if ($tempnames[$i] == "WFNAME") $wfname = $tempvals[$i];
          }
        }
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      require("isSavedRun.php");
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      if (substr($id, strlen($id)-1) == 'b') $id = substr($id, 0, strlen($id)-1);
      $pfix = "data/" . $bmpusername . "/";
      $f = opendir("data");
      while ($temp = readdir($f)) {
        if (preg_match("/i{$id}/i", $temp)) {
        #if (eregi('i' . $id, $temp)) {
           $rfiles[] = $temp;
        }
      }
      rename("data/run" . $id . ".txt", $pfix . "run" . $_POST['savename'] . ".txt");
      if (file_exists("data/run" . $id . "b.txt")) {
	rename("data/run" . $id . "b.txt", $pfix . "run" . $_POST['savename'] . "b.txt");
      }
      for ($j = 0; $j < count($rfiles); $j++) {
        rename('data/' . $rfiles[$j], $pfix . str_replace("i" . $id, $_POST['savename'], $rfiles[$j]));
      }
      $id = $_POST['savename'];
      $f = fopen($pfix . $id . '_inputs.txt','ab');
      fwrite($f, "savename\t" . $_POST['savename'] . "\n");
      fclose($f);
      $id .= "b";
      $f = fopen($pfix . $id . '_inputs.txt','ab');
      fwrite($f, "savename\t" . $_POST['savename'] . "\n");
      fclose($f);
      echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
      if (file_exists($pfix . $id . '_P_spec.txt')) {
        $spec = file($pfix . $id . '_P_spec.txt');
        for ($l = 0; $l < 13; $l++) {
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

   if (isset($_POST['template'])) {
     $tempFile = $_POST['template'];
   } else $tempFile = "1-gal_default.txt";
   $template = file($tempFile);
   for ($j = 0; $j < 13; $j++) {
      $temp = trim($template[$j*4]);
      $labels[$j] = $temp;
      $temp = trim($template[$j*4+1]);
      $infields[$j] = preg_split("/\s+/", $temp);
      $temp = trim($template[$j*4+2]);
      $def[$j] = preg_split("/\s+/", $temp);
   }

   if ($isSub == 3) {
      $temp = explode(" ", microtime(true));
      $timestamp = $temp[0];//+$temp[1];
      $id = substr($timestamp, 6, 7);
?>
<ul>
<li>This tool allows the user to track the day-to-day progress of a crop and
provides the user with a recommended amount of irrigation water to apply
each day.
<li><b>(A)</b> A real-time crop simulation is initiated by selecting management
inputs and hitting the Submit button at the bottom.
<li><b>(B)</b> Once initiated, the tool is programmed to automatically rerun
the simulation using updated weather data downloaded daily from the weather
station selected.  To make the tool more accurate, rainfall and weather data
recorded at the nursery can be entered manually to override data
automatically downloaded from the weather station.  Provisions are also made
for the user to enter spacing and pruning activities as well as make plant
height adjustments during the course of the season.
<li><b>(C)</b> A recommended irrigation amount is provided for the current
day.  Graphical output is also updated each day the simulation is rerun.
</ul>
<h2>A. Crop Detail - Select plant date and other management practices</h2>
<form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="return checkRuns(false);">
<input type="hidden" name="isSubmitted" value=1>
<input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
<input type="hidden" name="id" value="<?php echo $id; ?>">
<input type="hidden" name="MOVE1" value="-99">
<input type="hidden" name="PRUNE" value="FIXED">
<input type="hidden" name="TPR1" value="-99">
<input type="hidden" name="TPR2" value="-99">
<input type="hidden" name="TPR3" value="-99">
<input type="hidden" name="CUT" value="-99">
<table border=0 name="inputTable">

<?php 
  require('user-save.php');
  require('common.php'); 
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'] . 'b';
      if (isset($_POST['savename']) && trim($_POST['savename']) != '' && strpos($_POST['id'], $_POST['savename']) === false) {
	$pfix = "data/" . $bmpusername . "/";
	$f = opendir("data");
	while ($temp = readdir($f)) {
          if (preg_match("/i{$_POST['id']}/i", $temp)) {
	  #if (eregi('i' . $_POST['id'], $temp)) {
	    $rfiles[] = $temp;
	  }
	}
	rename("data/run" . $_POST['id'] . ".txt", $pfix . "run" . $_POST['savename'] . ".txt");
	for ($j = 0; $j < count($rfiles); $j++) {
	  rename('data/' . $rfiles[$j], $pfix . str_replace("i" . $_POST['id'], $_POST['savename'], $rfiles[$j]));
	}
	$doSave = true;
      }
      require("isSavedRun.php");
      $doSave = false;
      $daily = file($pfix . substr($id, 0, strlen($id)-1) . "_dailyoutput.txt");
      $temp = trim($daily[2]);
      $dfields = preg_split("/\s+/", $temp);
      $ircol = 0;
      for ($j = 0; $j < count($dfields); $j++) {
        if ($dfields[$j] == "Ir") $ircol = $j;
      }
      $zeros = "000";
      //update last 7 days
      $nlines = min(7, count($daily)-3);
      for ($j = 0; $j < $nlines; $j++) {
	$temp = trim($daily[count($daily)-$nlines+$j]);
        $temp = preg_split("/\s+/", $temp);
        $irdates[] = $temp[0] . substr($zeros, 0, 3-strlen($temp[1])) . $temp[1];
	if (isset($_POST['OVERRIDE' . $irdates[$j]])) {
	  $ir[$j] = $_POST['IRRHIS' . $irdates[$j]]*2.54;  
	}
      }
      //create new irrigation file
      $f = fopen($pfix . $id . "_irrigation.irr", 'wb');
      fwrite($f, "@DATE    IRR\n\n");
      for ($j = 0; $j < count($irdates); $j++) {
	fwrite($f, $irdates[$j] . ' ');
	if (isset($_POST['OVERRIDE' . $irdates[$j]])) {
	  fwrite($f, $ir[$j] . "\n");
	} else fwrite($f, "-1\n");
      }
      fclose($f);
      //create new weather file
      $wth = file($_POST['WFNAME'] . '.wth');
      $f = fopen($pfix . $id . "_weather.wth", 'wb');
      for ($j = 0; $j < count($wth)-$nlines; $j++) fwrite($f, $wth[$j]);
      $space = '   ';
      //update last 7 days
      for ($j = 0; $j < $nlines; $j++) { 
        $temp = trim($wth[count($wth)-$nlines+$j]);
        $temp = preg_split("/\s+/", $temp);
        $solar = outFormat($_POST['SOLARHIS' . $irdates[$j]], 1); 
	$solar .= substr($space, 0, 6-strlen($solar));
        $maxt = outFormat(($_POST['MAXTHIS' . $irdates[$j]]-32)/1.8, 1);
	$maxt .= substr($space, 0, 6-strlen($maxt));
        $mint = outFormat(($_POST['MINTHIS' . $irdates[$j]]-32)/1.8, 1);
	$mint .= substr($space, 0, 6-strlen($mint));
        $rain = outFormat($_POST['RAINHIS' . $irdates[$j]]*25.4, 1);
	fwrite($f, $temp[0] . '  ' . $solar . $maxt . $mint . $rain . "\n");
      }
      //copy yesterday to today
      $datearr = strptime($temp[0], "%Y%j");
      $wthdate = strtotime($datearr["tm_mday"] . '-' . ($datearr["tm_mon"]+1) . '-' . ($datearr["tm_year"]+1900));
      $wthdate += 86400;
      fwrite($f, strftime("%Y%j", $wthdate) . '  ' . $solar . $maxt . $mint . $rain . "\n");
      fclose($f);
   } if ($isSub == 0 || $isSub == 4) {
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      if ($isSub == 0) $id .= 'b';
      $runfile = "data/run" . $id . ".txt";
      $fert = $_POST['FERT'];
      $pctn = $_POST['PCT_N'];
      $pctp = $_POST['PCT_P'];
      $wfname = $_POST['WFNAME'];
      $pltdoy = $_POST['PLT_DOY'];
      $sched = $_POST['SCHED'];
      $yrs = findValidYears($wfname, $pltdoy);
      $startYr = $yrs[1];
      if (isset($_POST['START_YR'])) $startYr = $_POST['START_YR'];
      $endYr = $yrs[1];
      if (isset($_POST['END_YR'])) $endYr = $_POST['END_YR'];
      if (! isset($_GET['editid']) || !$isLoggedIn) {
	//don't overwrite B values if editing saved run!
	require("isSavedRun.php");
      }
      if (isset($_GET['editid']) && $isSub == 4 && $isLoggedIn) {
	if (isset($_POST['savename']) && $_POST['savename'] != '' && isset($_SESSION['bmpid'])) {
	  $pfix = "data/" . $bmpusername . "/";
	  echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
	  $runfile = "data/" . $bmpusername . "/run" . $id . ".txt";
	}
      }

      if ($isSub == 0) require("output-save.php");

      $infile = $pfix . $id . "_P_spec.txt";
      $f = fopen($infile, 'wb');
      for ($j = 0; $j < count($infields); $j++) {
	if (isset($_POST[$infields[$j][0]])) {
	   if ($isSub == 0 && $infields[$j][0] == 'SCHED') $temp = 'REALTIME';
	   else if ($isSub == 0 && $infields[$j][0] == 'WFNAME') $temp = $pfix . $id . "_weather";
	   else $temp = $_POST[$infields[$j][0]];
	   if (strpos($temp, "/") !== false) $temp = '"' . $temp . '"';
	} else {
	   $temp = $def[$j][0];
	}
	for ($l = 1; $l < count($infields[$j]); $l++) {
           if ($infields[$j][$l] === 'START_YR') $def[$j][$l] = $startYr;
           if ($infields[$j][$l] === 'END_YR') $def[$j][$l] = $endYr;
	   if ($infields[$j][$l] === 'IFNAME' && $isSub == 0) $def[$j][$l] = $pfix . $id . "_irrigation"; 
	   if (strpos($def[$j][$l], "/") !== false) $def[$j][$l] = '"' . $def[$j][$l] . '"';
	   if (isset($_POST[$infields[$j][$l]])) {
	      $temp .= "\t" . $_POST[$infields[$j][$l]];
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
    } if ($isSub == 0) {
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
   } else if ($isSub == 4) {
?>
<ul>
<li>This tool allows the user to track the day-to-day progress of a crop and
provides the user with a recommended amount of irrigation water to apply
each day.
<li><b>(A)</b> A real-time crop simulation is initiated by selecting management
inputs and hitting the Submit button at the bottom.
<li><b>(B)</b> Once initiated, the tool is programmed to automatically rerun
the simulation using updated weather data downloaded daily from the weather
station selected.  To make the tool more accurate, rainfall and weather data
recorded at the nursery can be entered manually to override data
automatically downloaded from the weather station.  Provisions are also made
for the user to enter spacing and pruning activities as well as make plant
height adjustments during the course of the season.
<li><b>(C)</b> A recommended irrigation amount is provided for the current
day.  Graphical output is also updated each day the simulation is rerun.
</ul>
<h2>A. Crop Detail - Select plant date and other management practices</h2>
<div id="realtime-Ahidden">
<center><b>
<a href="javascript:dispHandle('realtime-A', true); dispHandle('realtime-Ahidden', false);">Crop Detail -View and edit plant management practices</a>
</b></center>
</div>
<div id="realtime-A" style="display:none;">
<center><b>
<a href="javascript:dispHandle('realtime-A', false); dispHandle('realtime-Ahidden', true);">Hide Crop Detail</a>
</b></center>
<form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="updateTabs();" target="output">
<input type="hidden" name="isSubmitted" value=1>
<input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
<input type="hidden" name="id" value="<?php echo $id; ?>">
<input type="hidden" name="PRUNE" value="FIXED">
<input type="hidden" name="TPR1" value="-99">
<input type="hidden" name="TPR2" value="-99">
<input type="hidden" name="TPR3" value="-99">
<input type="hidden" name="CUT" value="-99">
<input type="hidden" name="B" value="1">
<table border=0 name="inputTable">

<?php
      if (isset($_GET['editid']) && $isLoggedIn) $_POST = $input; 
      require('user-save.php');
      require('common.php');
      echo '<script language="javascript">';
      foreach ($_POST as $key=>$value) {
	if ($key == "submit") continue;
        echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
      }
      foreach ($_POST as $key=>$value) {
        if ($key == "WFNAME" or $key == "fertb") {
          echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
        }
      }
      echo '</script>';
      echo '</div><br>';

      echo '<ul>';
      echo '<input type="checkbox" name="viewcheck" value="viewfiles" onClick="dispHandle(\'viewfiles\', this.checked);"><span id="viewfilesspan" style="font-weight: bold">View Output Files</span><br>';
      echo '</ul>';
      echo '<div id="viewfiles" class="indent75" style="display:none">';
      //list output files
      $dataDir = substr($pfix, 0, strrpos($pfix, '/'));
      $f = opendir($dataDir);
      while ($temp = readdir($f)) {
        if (preg_match("/{$id}_/i", $temp) && !preg_match("/run/i", $temp)) {
        #if (eregi($id . '_', $temp) && !eregi('run', $temp)) {
           $outfiles[] = $temp;
        }
      }
      for ($j = 0; $j < count($outfiles); $j++) {
        $outname = substr($outfiles[$j], strpos($outfiles[$j], '_')+1);
        echo '<a href="' . $dataDir . '/' . $outfiles[$j] . '" target="ccmt">' . $outname .  '</a><br>';
      }
      echo '</div>';
      
      echo '<h2>B. View and Edit Irrigation and Weather History</h2>';
      echo '<b>Edit and update before making today’s irrigation recommendation</b><br><br>';
      //read file and parse fields and values into arrays
      $daily = file($pfix . $id . "_" . "dailyoutput.txt");
      $temp = trim($daily[2]);
      $dfields = preg_split("/\s+/", $temp);
      $ircol = 0;
      for ($j = 0; $j < count($dfields); $j++) {
	if ($dfields[$j] == "Ir") $ircol = $j;
      }
      $nlines = min(7, count($daily)-3);
      $zeros = "000";
      for ($j = count($daily)-$nlines; $j < count($daily); $j++) {
	$temp = trim($daily[$j]);
	$temp = preg_split("/\s+/", $temp);
	$ir[] = outFormat($temp[$ircol]/2.54, 3);
	$irlabel[] = $temp[0] . substr($zeros, 0, 3-strlen($temp[1])) . $temp[1];
	$irday[] = $temp[2];
	$datearr = strptime($temp[0] . ' ' . $temp[1], "%Y %j");
	$irdates[] = strtotime($datearr["tm_mday"] . '-' . ($datearr["tm_mon"]+1) . '-' . ($datearr["tm_year"]+1900));
      }
      $wth = file($_POST['WFNAME'] . '.wth');
      for ($j = count($wth)-$nlines; $j < count($wth); $j++) {
        $temp = trim($wth[$j]);
        $temp = preg_split("/\s+/", $temp);
	$solar[] = outFormat($temp[1], 2);
	$maxt[] = outFormat($temp[2]*1.8+32, 3);
	$mint[] = outFormat($temp[3]*1.8+32, 3);
        $rain[] = outFormat($temp[4]/25.4, 3);
        $datearr = strptime($temp[0], "%Y%j");
	$currDay = (int)substr($temp[0],4) - (int)$_POST['PLT_DOY'] +1;
	if ($currDay <= 0) {
	  $currDay += 365;
	  if ((int)substr($temp[0],0,4) % 4 == 1) $currDay += 1;
	}
	$wthday[] = $currDay; 
        $wthlabel[] = $temp[0];
        $wthdates[] = strtotime($datearr["tm_mday"] . '-' . ($datearr["tm_mon"]+1) . '-' . ($datearr["tm_year"]+1900));
      }
      echo '<b>Irrigation history</b><br>';
      echo '<i>Note: The irrigation log allows the user to edit irrigation.  The model-recommended irrigation, which may';
      echo ' change from what is currently shown if you enter new spacings or prunings, will be used unless the override is selected.';
      echo ' In this case, the value from the pulldown will be used for that date.</i><br>';

      echo '<table border=0><tr><th valign="top" align="left" width=120>Date</th>';
      echo '<th valign="top" align="left" width=60>Day</th>';
      echo '<th valign="top" align="left" width=120>Day of week</th>';
      echo '<th valign="top">Irrigation (inch)</th>';
      echo '<th valign="top">Override</th></tr>';
      for ($j = 0; $j < count($ir); $j++) {
	echo '<tr><td>';
	echo strftime("%m/%d/%Y",$irdates[$j]);
	echo '</td><td align="left">';
	echo $irday[$j];
        echo '</td><td align="left">';
	echo strftime("%a",$irdates[$j]);
	echo '</td><td align="center">';
        //echo '<select name="IRRHIS' . $irlabel[$j] . '">';
	echo '<select name="IRRHIS' . $irlabel[$j] . '" style="color:#000000;" disabled>';
	echo '<option style="background:#ccccff;" value="' . $ir[$j] . '">' . outFormat($ir[$j], 3) . ' *'; 
	for ($l = 0; $l <= 1.5; $l+=0.05) {
	  echo '<option name="' . $l . '" value="' . $l . '">' . $l;
	}
	echo '</select></td>';
	echo '<td align="center"><input type="checkbox" name="OVERRIDE' . $irlabel[$j] . '" onClick="toggleIrr(\'' . $irlabel[$j] . '\', this.checked);">';
	echo '</tr>';
      }
      echo '</table>';
      echo '<i>* Highlighted value is model-recommended irrigation rate</i>';
      echo '<br><br>';
      echo '<b>Weather history</b><br>';
      echo '<i>Note: The weather log allows the user to edit weather information (particularly rainfall) if actual is known to be different than that obtained from FAWN weather station</i><br>';
      echo '<table border=0><tr><th valign="top" align="left" width=120>Date</th>';
      echo '<th valign="top" align="left" width=60>Day</th>';
      echo '<th valign="top" align="left" width=120>Day of week</th>';
      echo '<th valign="top">Solar Radiation<br>(MJ/m^2)</th>';
      echo '<th valign="top">Min Temp<br>(F)</th><th valign="top">Max Temp<br>(F)</th>';
      echo '<th valign="top">Rain<br>(inches)</th>';
      echo '<th valign="top"></th></tr>';
      for ($j = 0; $j < count($wthdates); $j++) {
	#Check for interpolated data
        $nobs = 0;
	$csvFile = "fawn/fawnData/fawn-" . strftime("%m%d%y", $wthdates[$j]) . ".csv";
	$key = array_search($_POST['WFNAME'],$fawn);
	if (file_exists($csvFile)) {
	  $csv = file($csvFile);
          for ($l = 1; $l < count($csv); $l++) {
	    $currLine = explode(",",$csv[$l]);
	    if ($fawnId[$key] == $currLine[0]) $nobs = $currLine[2]; 
	  }
	}
        echo '<tr><td>';
        echo strftime("%m/%d/%Y",$wthdates[$j]);
        echo '</td><td align="left">';
        echo $wthday[$j];
        echo '</td><td align="left">';
        echo strftime("%a",$wthdates[$j]);
        echo '</td><td align="center">';
        echo '<select name="SOLARHIS' . $wthlabel[$j] . '">';
        echo '<option style="background:#ccccff;" value="' . $solar[$j] . '">' . outFormat($solar[$j], -1) . ' *';
        for ($l = 0; $l <= 35; $l++) {
          echo '<option value="' . $l . '">' . $l;
        }
        echo '</select></td>';
        echo '<td align="center">';
        echo '<select name="MINTHIS' . $wthlabel[$j] . '">';
	echo '<option style="background:#ccccff;" value="' . $mint[$j] . '">' . outFormat($mint[$j], -1) . ' *';
        for ($l = 14; $l <= 86; $l++) {
          echo '<option value="' . $l . '">' . $l;
        }
        echo '</select></td>';
        echo '<td align="center">';
        echo '<select name="MAXTHIS' . $wthlabel[$j] . '">';
        echo '<option style="background:#ccccff;" value="' . $maxt[$j] . '">' . outFormat($maxt[$j], -1) . ' *';
        for ($l = 32; $l <= 104; $l++) {
          echo '<option value="' . $l . '">' . $l;
        }
        echo '</select></td>';
        echo '<td align="center">';
        echo '<select name="RAINHIS' . $wthlabel[$j] . '">';
        echo '<option style="background:#ccccff;" value="' . $rain[$j] . '">' . outFormat($rain[$j], 3) . ' *';
        for ($l = 0; $l < 1; $l+=0.05) {
          echo '<option value="' . $l . '">' . $l;
        }
	for ($l = 1; $l < 2; $l+=0.1) {
          echo '<option value="' . $l . '">' . $l;
	}
        for ($l = 2; $l <= 10; $l+=0.25) {
          echo '<option value="' . $l . '">' . $l;
        }
	echo '</select></td>';
	echo '<td class="redhead">';
	if ($nobs == 0) echo 'Interpolated';
	echo '</td></tr>';
      }
      echo '</table>';
      echo '<i>* Highlighted values from FAWN weather station</i>';
      echo '<br><br>';
?>
      <b>New container spacing (max of 3 moves per crop)</b><br>
      <table border=0 style="margin-left: 50px;">
      <tr>
        <th valign="top" align="left" width=70>Move</th>
        <th valign="top" align="center" width=150>Date</th>
        <th valign="top" align="left">Container Spacing After Move</th>
	<th valign="top" align="right" rowspan=4>
      </tr>
      <?php
        for ($i = 1; $i < 4; $i++) {
          echo '<tr>';
          echo '<td align="center">' . $i . '<input type="checkbox" name="enableMove' . $i . '" value="enableMove' . $i . '" onClick="adjMove();"></td>';
          echo '<td align="center">';
          echo '<input type="hidden" name="MOVE' . $i . '" value=-99>';
          echo '<select name="moveMonth' . $i . '" onChange="setDayListRT(this.selectedIndex, \'moveDay' . $i . '\'); adjMove();" disabled>';
          for ($j = 0; $j < 12; $j++) {
            echo '<option name="' . $months[$j] . '" value=' . $j;
            if ($j == $_POST['month']+$i) echo ' selected';
            echo '>';
            echo $months[$j];
          }
          echo '</select>';
          echo '<select name="moveDay' . $i . '" onChange="adjMove();" disabled>';
          for ($j = 0; $j < 31; $j++) {
            echo '<option name=' . ($j+1) . ' value=' . ($j+1) . '>';
            echo ($j+1);
          }
          echo '</select></td>';

          echo '<td align="left" valign="top">';
          echo '<input type="hidden" name="PTA' . $i . '" value=-99>';
	  echo '<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #ff0000;">B = </span>';
	  echo '<select name="spacb' . $i . '" onChange="adjMove();" disabled>';
          for ($j = 0; $j <= 18; $j++) {
            echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
          }
	  echo '<option name="contdiam" value="contdiam">Cont. Diam.';
	  echo '<option name="3/4" value="3/4">3/4 Diameter';
	  echo '</select> between top edge of containers<br>';

	  echo '<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #0000ff;">W = </span>';
	  echo '<select name="spacw' . $i . '" onChange="adjMove();" disabled>';
          for ($j = 0; $j <= 18; $j++) {
            echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
          }
          echo '<option name="contdiam" value="contdiam">Cont. Diam.';
          echo '<option name="3/4" value="3/4">3/4 Diameter';
	  echo '</select> between top edge of containers<br></td></tr>';
        }
      ?>
      </table>
      <br>
      <center>
      <?php
	if ($_POST['ARRANGE'] == "TRIANG") {
	  echo '<img src="images/triangular_start.png" id="startImg2" name="startImg2">';
	} else {
          echo '<img src="images/square_start.png" id="startImg2" name="startImg2">';
	}
      ?>
      </center>
      <br>

      <b>New pruning (max of 3 prunes per crop)</b> (<a href="javascript:popupText(rtprunetext);" onMouseOver="if (!IsIE()) ddrivetip(rtprunetext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
      <br>
      <table border=0 style="margin-left: 50px;">
      <tr>
        <th valign="top" align="left" width=70></th>
        <th valign="top" align="center"></th>
        <th valign="top" align="center" colspan=2 style="color: red;">Before Prune</th>
        <th valign="top" align="center" colspan=2 style="color: green;">After Prune</th>
      </tr>
      <tr>
	<th valign="top" align="left" width=70>Prune</th>
	<th valign="top" align="center">Date</th>
	<th valign="top" align="left">Height (inch)</th>
	<th valign="top" align="left">Width (inch)</th>
        <th valign="top" align="left">Height (inch)</th>
        <th valign="top" align="left">Width (inch)</th>
      </tr>
      <?php
	for ($i = 1; $i < 4; $i++) {
	  echo '<tr>';
	  echo '<td align="center">' . $i . '<input type="checkbox" name="enablePrune' . $i . '" value="enablePrune' . $i . '" onChange="adjPrune();"></td>';
	  echo '<td align="center">';
	  echo '<input type="hidden" name="PR' . $i . '" value=-99>';
	  echo '<select name="pruneMonth' . $i . '" onChange="setDayListRT(this.selectedIndex, \'pruneDay' . $i . '\'); adjPrune();" disabled>';
	  for ($j = 0; $j < 12; $j++) {
	    echo '<option name="' . $months[$j] . '" value=' . $j;
	    if ($j == $_POST['month']+$i) echo ' selected';
	    echo '>';
	    echo $months[$j];
	  }
	  echo '</select>';
	  echo '<select name="pruneDay' . $i . '" onChange="adjPrune();" disabled>';
	  for ($j = 0; $j < 31; $j++) {
	    echo '<option name=' . ($j+1) . ' value=' . ($j+1) . '>';
	    echo ($j+1);
	  }
	  echo '</select></td>';

          echo '<td align="center">';
          echo '<select name="bprht' . $i . '" onChange="adjPrune();" disabled>';
          for ($j = 6; $j <= 36; $j++) {
            echo '<option name=' .$j . ' value=' . $j . '>' . $j;
          }
          echo '</select></td>';

          echo '<td align="center">';
          echo '<select name="bprw' . $i . '" onChange="adjPrune();" disabled>';
          for ($j = 6; $j <= 36; $j++) {
            echo '<option name=' .$j . ' value=' . $j . '>' . $j;
          }
          echo '</select></td>';

	  echo '<td align="center">';
	  echo '<input type="hidden" name="PR_H' . $i . '" value=-99>';
	  echo '<select name="prht' . $i . '" onChange="adjPrune();" disabled>';
	  for ($j = 6; $j <= 36; $j++) {
	    echo '<option name=' .$j . ' value=' . $j . '>' . $j;
	  }
	  echo '</select></td>';

	  echo '<td align="center">';
	  echo '<input type="hidden" name="PR_W' . $i . '" value=-99>';
	  echo '<select name="prw' . $i . '" onChange="adjPrune();" disabled>';
	  for ($j = 6; $j <= 36; $j++) {
	    echo '<option name=' .$j . ' value=' . $j . '>' . $j;
	  }
	  echo '</select></td></tr>';
	}
      ?>
      </table>
      <br>

      <b>Plant size adjustment (max of 3 adjustments per crop)</b> (<a href="javascript:popupText(rtadjtext);" onMouseOver="if (!IsIE()) ddrivetip(rtadjtext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
      <br>

  <table border=0 style="margin-left: 50px;">
  <tr>
    <th valign="top" align="left">Adjustment</th>
    <th valign="top" align="center">Date</th>
    <th valign="top" align="left">Plant Height (inch)</th>
    <th valign="top" align="left">Plant Width (inch)</th>
  </tr>
  <?php
    for ($i = 1; $i < 4; $i++) {
      echo '<tr>';
      echo '<td align="center">' . $i . '<input type="checkbox" name="enableAdj' . $i . '" value="enableAdj' . $i . '" onClick="adjPlantSize();"></td>';
      echo '<td align="center">';
      echo '<input type="hidden" name="CHK_DAY' . $i . '" value=-99>';
      echo '<select name="adjSizeMonth' . $i . '" onChange="setDayListRT(this.selectedIndex, \'adjSizeDay' . $i . '\'); adjPlantSize();" disabled>';
      for ($j = 0; $j < 12; $j++) {
	echo '<option name="' . $months[$j] . '" value=' . $j;
	if ($j == $_POST['month']+$i) echo ' selected';
        echo '>';
        echo $months[$j];
      }
      echo '</select>';
      echo '<select name="adjSizeDay' . $i . '" onChange="adjPlantSize();" disabled>';
      for ($j = 0; $j < 31; $j++) {
	echo '<option name=' . ($j+1) . ' value=' . ($j+1) . '>';
	echo ($j+1);
      }
      echo '</select></td>';

      echo '<td align="center">';
      echo '<input type="hidden" name="CHK_HT' . $i . '" value=-99>';
      echo '<select name="chkht' . $i . '" onClick="adjPlantSize();" disabled>';
      for ($j = 6; $j <= 36; $j++) {
	echo '<option name=' .$j . ' value=' . $j . '>' . $j;
      }
      echo '</select></td>';

      echo '<td align="center">';
      echo '<input type="hidden" name="CHK_W' . $i . '" value=-99>';
      echo '<select name="chkw' . $i . '" onChange="adjPlantSize();" disabled>';
      for ($j = 6; $j <= 36; $j++) {
        echo '<option name=' .$j . ' value=' . $j . '>' . $j;
      }
      echo '</select></td></tr>';
    }
  ?>
  </table>
  <br>
  <script language="javascript">
    <?php
      if (!$isLoggedIn) {
        echo 'setFormState(document.paramForm, true);';
      }
    ?> 
  </script>

<?php
      echo '<input type="Submit" name="Submit" value="Submit">';
      echo '<input type="Reset" name="Reset" value="Reset">';
      echo '</form>';
      echo '<script language="javascript">';
      foreach ($_POST as $key=>$value) {
        if ($key == "submit") continue;
        echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
      }
      foreach ($_POST as $key=>$value) {
        if ($key == "WFNAME" or $key == "fertb") {
          echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
        }
      }
      echo '</script>';

   }

   if ($isSub < 3) {
      showOutputButtons();
      $daily = file($pfix . $id . "_" . "dailyoutput.txt");
      $temp = trim($daily[2]);
      $dfields = preg_split("/\s+/", $temp);
      $ircol = 0;
      $htcol = 0;
      $laicol = 0;
      for ($j = 0; $j < count($dfields); $j++) {
        if ($dfields[$j] == "Ir") $ircol = $j;
	if ($dfields[$j] == "HT") $htcol = $j;
	if ($dfields[$j] == "LAI") $laicol = $j;
      }
      $temp = trim($daily[count($daily)-1]);
      $temp = preg_split("/\s+/", $temp);
      $todayIr = outFormat($temp[$ircol]/2.54, 2);
      $todayHt = outFormat($temp[$htcol]/2.54, 2);
      $todayLai = outFormat($temp[$laicol], 2);
      $datearr = strptime($temp[0] . ' ' . $temp[1], "%Y %j");
      $todayDate = strtotime($datearr["tm_mday"] . '-' . ($datearr["tm_mon"]+1) . '-' . ($datearr["tm_year"]+1900));
      echo '<h3>C. Today’s Irrigation Recommendation - ' . strftime("%a %b %d, %Y", $todayDate);
      echo ' (<a href="javascript:popupText(rtctext1);" onMouseOver="if (!IsIE()) ddrivetip(rtctext1,\'99ffaa\',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)</h3>';
      echo '<table border=0 align="center"><tr><td>';
      echo '<div align="center" class="realtimeRec">' . $todayIr . ' inches</div>';
      echo '</td></tr></table>';
      echo '<i>Note: This recommendation is an estimate based upon an average plant and assumes uniform irrigation within the production area.</i><br><br>';
      echo '<br>';
      echo '<table border=0 align="center"><tr><td>';
      echo '<div align="left" class="realtimeEst">';
      echo 'Estimated plant height = ' . $todayHt . ' inches';
      echo ' (<a href="javascript:popupText(rtctext2);" onMouseOver="if (!IsIE()) ddrivetip(rtctext2,\'99ffaa\',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)<br>';
      echo 'Estimated LAI = ' . $todayLai;
      echo ' (<a href="javascript:popupText(rtctext3);" onMouseOver="if (!IsIE()) ddrivetip(rtctext3,\'99ffaa\',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)</h3>';
      echo '</div>';
      echo '</td></tr></table>';
      echo '<br>';

      require('outputGraphs.php');
   }
   require('footer.php');
?>
