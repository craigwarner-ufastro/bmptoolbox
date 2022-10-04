<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<script language="javascript">
  function updateAllGraphs(id) {
    var i1 = document.graphForm.varGraph1.selectedIndex;
    updateGraph('imgGraph1', id, i1, 'dailystats.txt');
    var i2 = document.graphForm.varGraph2.selectedIndex;
    updateGraph('imgGraph2', id, i2, 'Nplantstats.txt');
    var i3 = document.graphForm.varGraph3.selectedIndex;
    updateGraph('imgGraph3', id, i3, 'NRelstats.txt');
    var i4 = document.graphForm.varGraph4.selectedIndex;
    updateGraph('imgGraph4', id, i4, 'Nleachstats.txt');
    if (isIE) billGatesSucks();
  }

  function billGatesSucks() {
    var r = Math.random();
    document['imgGraph1'].src += "&" + r;
    document['imgGraph2'].src += "&" + r;
    document['imgGraph3'].src += "&" + r;
    document['imgGraph4'].src += "&" + r;
  }

  function updateGraph(imgName, id, index, file) {
    if (document.images) {
      if (document.graphForm.yearPlot.selectedIndex == 0) {
        //mean 
        path = "graph.php?file=" + pfix + id + "_" + file + "&ymin=0&xcol=0&ycol=";
        path += (index+1)*5;
        path += "&skipl=3&usedates=1";
        document[imgName].src = path; 
      } else {
        //year
	file = file.replace('stats.txt','output.txt');
        path = "yearlygraph.php?file=" + pfix + id + "_" + file + "&ymin=0&xcol=1&ycol=";
        path += index+3; 
        path += "&year=" + document.graphForm.yearPlot.options[document.graphForm.yearPlot.selectedIndex].value;
        path += "&skipl=2&usedates=1";
        document[imgName].src = path; 
      }
    }
    if (isIE) billGatesSucks();
  }

  function getGraphURL(name, id, index, ytitle) {
        //mean
        var path = "graph.php?file=" + pfix + id + "_" + name + "&ymin=0&xcol=0&ycol=";
        path += (index+1)*5;
        path += "&skipl=3&usedates=1";
	return path;
  }

  function setCalendarValsTech(y, m, d) {
    document.paramForm.START_YR.value=y;
    var theDate = new Date(y, m-1, d);
    var janone = new Date(y, 0, 1);
    var doy = Math.ceil((theDate-janone)/86400000)+1;
    document.paramForm.PLT_DOY.value = doy;
  }

  function updateCalendarTech(cal, anchor) {
    var doy = Math.floor(document.paramForm.PLT_DOY.value);
    var yr = document.paramForm.START_YR.value;
    var janone = new Date(yr, 0, 1);
    var theDate = new Date(janone.getTime()+86400000*(doy-1));
    var mon = theDate.getMonth()+1;
    var selDate = ''+mon; 
    selDate += '/' + theDate.getDate();
    selDate += '/' + yr; 
    var startDate = wthStart;
    var endDate = wthEnd[0];
    if (document.paramForm.WFNAME.selectedIndex < wthEnd.length) {
      endDate = wthEnd[document.paramForm.WFNAME.selectedIndex];
    }
    var selIdx = document.paramForm.WFNAME.selectedIndex;
    if (document.paramForm.WFNAME.options[selIdx].value.indexOf('fawn') != -1) {
      for (j = 0; j < fawn.length; j++) {
	if (fawn[j] == document.paramForm.WFNAME.options[selIdx].value) {
	  startDate = fawnStart[j];
	  endDate = latest;
	}
      }
    } else if (document.paramForm.WFNAME.options[selIdx].value.indexOf('data/') != -1) {
      for (j = 0; j < userWth.length; j++) {
        if (userWth[j] == document.paramForm.WFNAME.options[selIdx].value) {
          startDate = userWthStart[j];
	  endDate = userWthEnd[j];
        }
      }
    } else {
      for (j = 0; j < wth.length; j++) {
        if (wth[j] == document.paramForm.WFNAME.options[selIdx].value) {
          startDate = wthStart;
          endDate = wthEnd[j];
        }
      }
    }
    d = new Date(startDate);
    d.setDate(d.getDate()-1);
    d2 = new Date(endDate);
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(d2,"yyyy-MM-dd"), null);
    cal.addDisabledDates(null, formatDate(d,"yyyy-MM-dd"));
    cal.showCalendar(anchor, selDate);
    return false;
  }

  function updateYearTech() {
    var startDate;
    var selIdx = document.paramForm.WFNAME.selectedIndex;
    if (document.paramForm.WFNAME.options[selIdx].value.indexOf('fawn') != -1) {
      wthType = 0;
      for (j = 0; j < fawn.length; j++) {
        if (fawn[j] == document.paramForm.WFNAME.options[selIdx].value) {
          startDate = fawnStart[j];
          endDate = latest;
        }
      }
    } else if (document.paramForm.WFNAME.options[selIdx].value.indexOf('data/') != -1) {
      wthType = 2;
      for (j = 0; j < userWth.length; j++) {
        if (userWth[j] == document.paramForm.WFNAME.options[selIdx].value) {
          startDate = userWthStart[j];
          endDate = userWthEnd[j];
        }
      }
    } else {
      wthType = 1;
      for (j = 0; j < wth.length; j++) {
        if (wth[j] == document.paramForm.WFNAME.options[selIdx].value) {
          startDate = wthStart;
          endDate = wthEnd[j];
        }
      }
    }
    var sd = new Date(startDate);
    var yr = sd.getFullYear();
    var janone = new Date(yr, 0, 1);
    var doy = Math.ceil((sd-janone)/86400000)+1;
    var currDoy = document.paramForm.PLT_DOY.value;
    if (currDoy <= doy) {
      document.paramForm.START_YR.value = (yr+1);
    } else document.paramForm.START_YR.value = yr;
    var ed = new Date(endDate);
    yr = ed.getFullYear();
    document.paramForm.END_YR.value = yr;
  }


</script>
<title>Technical Tool</title>

<?php require('header.php'); ?>

<h1 class="h1-container">Technical Tool<font color="#ffffff" class="h1-shadow">Technical Tool</font></h1>
<?php
  $pagename = "technical";
  echo '<script language="javascript">';
  echo 'pagename="' . $pagename . '";';
  echo '</script>';

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
  } else $isSub = 2;

  $template = file('../driver/1_P_spec.txt');
  for ($j = 0; $j < $npspec; $j++) {
    $temp = trim($template[$j*4]);
    $labels[$j] = $temp;
    $temp = trim($template[$j*4+1]);
    $infields[$j] = preg_split("/\s+/", $temp);
    $temp = trim($template[$j*4+2]);
    $def[$j] = preg_split("/\s+/", $temp);
  }

  if ($isSub == 2) {
    $temp = explode(" ", microtime(true));
    $timestamp = $temp[0];
    $id = substr($timestamp, 6, 7);

    $fileLists = array();
    $fileLists['WFNAME'] = array();
    $fileLists['PFNAME'] = array();
    $fileLists['IFNAME'] = array();
    $fileLists['SFFNAME'] = array();

    $f = opendir(".");
    while ($temp = readdir($f)) {
      if (strpos($temp, '.wth') !== false) {
        $fileLists['WFNAME'][] = substr($temp, 0, strpos($temp, '.wth'));
      } else if (strpos($temp, '.plt') !== false) {
        $fileLists['PFNAME'][] = substr($temp, 0, strpos($temp, '.plt'));
      } else if (strpos($temp, '.irr') !== false) {
        $fileLists['IFNAME'][] = substr($temp, 0, strpos($temp, '.irr'));
      } else if (strpos($temp, '.sfn') !== false) {
        $fileLists['SFFNAME'][] = substr($temp, 0, strpos($temp, '.sfn'));
      }
    }
    $f = opendir("fawn");
    while ($temp = readdir($f)) {
      if (strpos($temp, '.wth') !== false) {
	$fileLists['WFNAME'][] = 'fawn/' . substr($temp, 0, strpos($temp, '.wth'));
      }
    }
    #User-defined files
    if ($isLoggedIn) {
      for ($j = 0; $j < count($userWth); $j++) {
	$fileLists['WFNAME'][] = $userWth[$j];
      }
      for ($j = 0; $j < count($userPlt); $j++) {
        $fileLists['PFNAME'][] = $userPlt[$j];
      }
      for ($j = 0; $j < count($userIrr); $j++) {
        $fileLists['IFNAME'][] = $userIrr[$j];
      }
      for ($j = 0; $j < count($userSfn); $j++) {
        $fileLists['SFNAME'][] = $userSfn[$j];
      }
    }

    sort($fileLists['WFNAME']);
    sort($fileLists['PFNAME']);
    sort($fileLists['IFNAME']);
    sort($fileLists['SFFNAME']);
?>
    <ul>
    <li>The Technical tool allows users to to change any input parameter in the
    model. Also, users may create and upload their own weather, plant,
    irrigation and/or solution fertilizer input files.
    <li>A description of each input parameter and the units required can be
    found by mousing over the parameter name in the input page.
    <li>After entering input parameter values, click the submit button to run
    the simulation and view output.
    <li>A <a href="source/UserManual.pdf" target="ccmt">user's manual</a> which
    provides detailed information regarding CCROP inputs, processes simulated,
    and output generated can be viewed in PDF format.
    </ul>

    <form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="return checkRuns(true);" target="output">
    <input type="hidden" name="isSubmitted" value=1>
    <input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
    <input type="hidden" name="id" value="<?php echo $id; ?>">
    <table border=0 name="inputTable">
<?php
    require('user-save.php');
?>
    <tr><td colspan=10>
      <br>
      <script language="javascript">
	cal.setReturnFunction("setCalendarValsTech");
	cal.showYearNavigation();
      </script>
      <a href="#" onClick="updateCalendarTech(cal, 'anchor1x'); return false;" name="anchor1x" id="anchor1x">Click here for calendar</a>
      <br>
    </td></tr>
    <tr><td>
<?php
    $notes = file('Notes_input.txt');
    for ($j = 0; $j < count($infields); $j++) {
      echo '<table border=1 class="manage">';
      echo '<tr>';
      echo '<th colspan=8 class="manageth"><center><b>' . $labels[$j] . '</b></center></th>';
      echo '</tr>';
      echo '<tr>';
      for ($l = 0; $l < count($infields[$j]); $l++) {
	if ($l == 7) {
	  echo '</tr><tr>';
	}
	$currNote = '';
	for ($i = 0; $i < count($notes); $i++) {
	  if (strpos($notes[$i], $infields[$j][$l] . ' =') === 0) $currNote = trim($notes[$i]);
	}
	echo '<td class="managetd" ';
        if ($isIE) echo 'title="' . stripQuotes($currNote) . '"'; else echo 'onMouseOver="ddrivetip(\'' . $currNote . '\',\'99ffaa\',300);" onMouseOut="hideddrivetip();"';
        echo '>' . $infields[$j][$l] . '<br>';
	if (strpos($infields[$j][$l], 'FNAME') !== false) {
	  echo '<select name="' . $infields[$j][$l] . '"';
	  if ($infields[$j][$l] == 'WFNAME') echo ' onChange="updateYearTech();"';
	  echo '>';
	  foreach ($fileLists[$infields[$j][$l]] as $key=>$value) {
	    if ($value == $def[$j][$l]) $selTxt = " selected"; else $selTxt = "";
	    echo '<option name="' . $value . '"  value="' . $value . '" ' . $selTxt . '>' . str_replace("_", " ", $value);
	  }	
	} else {
	  $size = strlen($def[$j][$l])+1;
	  echo '<input type="text" name="' . $infields[$j][$l] . '" size=' . $size . ' value="' . $def[$j][$l] . '">';
	}
	echo '</td>';
      }
      echo '</tr></table><br>';
    }
?>
    <script language="javascript">updateYearTech();</script>
    </td></tr>
    </table>
    <br>
    <input type="Submit" name="Submit" value="Submit">
    <input type="Reset" name="Reset" value="Reset">
    </form>
    <br><br>
<?php
    require('setDefault.php');
    for ($j = 0; $j < count($notes); $j++) {
      if (substr($notes[$j],0,3) == '---') echo '<hr>'; else echo $notes[$j] . '<br>';
    }
  } else if ($isSub == 0) {
      $timestamp = $_POST['timestamp'];
      $id = $_POST['id'];
      $runfile = "data/run" . $id . ".txt";
      $fert = $_POST['FERT'];
      $pctn = $_POST['PCT_N'];
      $pctp = $_POST['PCT_P'];
      $wfname = $_POST['WFNAME'];
      $pltdoy = $_POST['PLT_DOY'];
      $sched = $_POST['SCHED'];
      require("isSavedRun.php");

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
           if (isset($_POST[$infields[$j][$l]])) {
	      if (strpos($_POST[$infields[$j][$l]], "/") !== false) {
		$temp .= "\t" . '"' . $_POST[$infields[$j][$l]] . '"';
	      } else $temp .= "\t" . $_POST[$infields[$j][$l]];
           } else {
              $temp .= "\t" . $def[$j][$l];
           }
           if ($infields[$j][$l] === 'START_YR') $startYr = $_POST[$infields[$j][$l]]; 
           if ($infields[$j][$l] === 'END_YR') $endYr = $_POST[$infields[$j][$l]]; 
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
      showOutputButtons();

      $p = popen("../driver/a.out < " . $runfile, "r");
      while (!feof($p)) {
        $read = fread($p,1024);
        flush();
      }
      pclose($p);

      #Setup arrays of titles for graphs and charts
      //dailyfiles defined in header
      $plotfields = array();
      $plotfiles = array();
      if (!file_exists($pfix . $id . "_" . $dailyfiles[0] . "output.txt")) {
        echo "Error: File " . $pfix . $id . "_" . $dailyfiles[0] . "output.txt does not exist!";
        exit(0);
      }
      #counter vars for select all checkboxes
      $ny = array(0,0,0,0,0,0,0);
      for ($i = 0; $i < count($dailyfiles); $i++) {
        //read file and parse fields and values into arrays
        $daily = file($pfix . $id . "_" . $dailyfiles[$i] . "output.txt");
        $temp = trim($daily[$skipl[$i]-1]);
        $dfields = preg_split("/\s+/", $temp);
        for ($j = 3; $j < count($dfields); $j++) {
           $plotfields[] = $dfields[$j];
           $plotfiles[] = $dailyfiles[$i];
	   $ny[$i]++;
        }
      }
      #Report form
      echo '<div id="reportdiv" style="display:none;">';
        echo '<form name="reportForm" action="report.php" method="post" target="ccmt">';
        echo '<input type="hidden" name="runid" value="' . $id . '">';
        echo '<input type="hidden" name="pfix" value="' . $pfix . '">';
        echo '<input type="hidden" name="type" value="' . $pagename . '">';
        echo '<input type="hidden" name="S_VOL" value="' . $subVol . '">';
        echo '<input type="hidden" name="pagename" value="' . $pagename . '">';
        echo '<center><h2>Generate a Report</h2></center>';
        echo '<div class="indent75">';
          echo '<span class="redhead">Select sections to display in report:</span><br>';
          echo '<div class="indent25" valign="top">';
            echo '<input type="checkbox" name="dailystats.txt" onClick="setReportChecks(this.name);" checked>';
            echo '<b>Variables from dailystats.txt:</b> ';
            echo '(<input type="checkbox" onClick="toggleReportState(\'dailystats.txt\', 0,' . $ny[0] . ', this.checked);"> All)<br>';
            echo '<div class="indent25">';
              $i = 0;
              for ($j = 0; $j < count($plotfields); $j++) {
	        if ($plotfiles[$j] == "daily") {
		  echo '<input id="dailystats.txt_' . $i . '" type="checkbox" name="dailystats.txt_' . $i . '" onClick="checkReport(\'dailystats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOver="showToolTipGraph(\'dailystats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOut="hideddrivetip();">';
                  echo $plotfields[$j] . '<br>';
                  $i++;
		}
              }
            echo '</div>';
            echo '<input type="checkbox" name="Nplantstats.txt" onClick="setReportChecks(this.name);" checked>';
            echo '<b>Variables from Nplantstats.txt: </b>';
            echo '(<input type="checkbox" onClick="toggleReportState(\'Nplantstats.txt\', 0, ' . $ny[1] . ', this.checked);"> All)<br>';
            echo '<div class="indent25">';
              $i = 0;
              for ($j = 0; $j < count($plotfields); $j++) {
                if ($plotfiles[$j] == "Nplant") {
                  echo '<input id="Nplantstats.txt_' . $i . '" type="checkbox" name="Nplantstats.txt_' . $i . '" onClick="checkReport(\'Nplantstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOver="showToolTipGraph(\'Nplantstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOut="hideddrivetip();">';
                  echo $plotfields[$j] . '<br>';
                  $i++;
                }
              }
            echo '</div>';
            echo '<input type="checkbox" name="NRelstats.txt" onClick="setReportChecks(this.name);" checked>';
            echo '<b>Variables from NRelstats.txt:</b> ';
            echo '(<input type="checkbox" onClick="toggleReportState(\'NRelstats.txt\', 0, ' . $ny[2] . ', this.checked);"> All)<br>';
            echo '<div class="indent25">';
              $i = 0;
              for ($j = 0; $j < count($plotfields); $j++) {
                if ($plotfiles[$j] == "NRel") {
                  echo '<input id="NRelstats.txt_' . $i . '" type="checkbox" name="NRelstats.txt_' . $i . '" onClick="checkReport(\'NRelstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOver="showToolTipGraph(\'NRelstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOut="hideddrivetip();">';
                  echo $plotfields[$j] . '<br>';
                  $i++;
                }
              }
            echo '</div>';
            echo '<input type="checkbox" name="Nleachstats.txt" onClick="setReportChecks(this.name);" checked>';
            echo '<b>Variables from Nleachstats.txt:</b> ';
            echo '(<input type="checkbox" onClick="toggleReportState(\'Nleachstats.txt\', 0, ' . $ny[3] . ', this.checked);"> All)<br>';
            echo '<div class="indent25">';
              $i = 0;
              for ($j = 0; $j < count($plotfields); $j++) {
                if ($plotfiles[$j] == "Nleach") {
                  echo '<input id="Nleachstats.txt_' . $i . '" type="checkbox" name="Nleachstats.txt_' . $i . '" onClick="checkReport(\'Nleachstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOver="showToolTipGraph(\'Nleachstats.txt\', 30, this, \'' . $id . '\', \'' . $plotfields[$j] . '\');" onMouseOut="hideddrivetip();">';
                  echo $plotfields[$j] . '<br>';
                  $i++;
                }
              }
            echo '</div>';
?>
          </div>
        </div>
        <center>
        <input type="Submit" name="Submit" value="Submit">
        <input type="Reset" name="Reset" value="Reset">
        <input type="Button" name="Hide Report" value="Hide Report" onClick="javascript:hideReport();">
	</center>
        </form>
      </div>
<?php

      echo '<br><b>Inputs:</b><div class="indent75">';
      echo 'Location: ' . str_replace('_', ' ', $wfname) . '<br>';
      echo 'Plant Date: ' . dateFromDoy($pltdoy) . '<br>';
      echo 'Irrigation: ' . ($sched == "MAD" ? "ET-based" : "Fixed") . '<br>';
      echo 'Fertilizer Rate: ' . outFormat($fert*$pctn/($subVol*0.059325)+0.005, 2) . ' lb N per cubic yard<br>';
      echo '</div><br>';

      echo "<b>Results:</b><br>";

      //Calculate daily stats
      //dailyfiles defined in header
      $plotfields = array();
      $plotfiles = array();
      if (!file_exists($pfix . $id . "_" . $dailyfiles[0] . "output.txt")) {
        echo "Error: File " . $pfix . $id . "_" . $dailyfiles[0] . "output.txt does not exist!";
        exit(0);
      }
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
              $dmedian[$key][$j-3] = substr('' . arraymedian($currArr), 0, 7);
              $dstddev[$key][$j-3] = substr('' . arraystddev($currArr), 0, 7);
           }
        }

        //write output file headers
        $f = fopen($pfix . $id . "_" . $dailyfiles[$i] . "stats.txt","wb");
        fwrite($f, $daily[0]);
        fwrite($f, $daily[1]);
        fwrite($f, "\t\t");
        for ($j = 3; $j < count($dfields); $j++) {
           fwrite($f, "\tMax\tMin\tMean\tMedian\tStdDev");
           #fwrite($f, "\tMax\tMin\tMean");
        }
        fwrite($f, "\n");
        fwrite($f, $dfields[1] . "\t" . $dfields[2]);
        fwrite($f, "\tNdays");
        for ($j = 3; $j < count($dfields); $j++) {
           for ($l = 0; $l < 5; $l++) {
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
              fwrite($f, "\t" . $dmedian[$key][$j] . "\t" . $dstddev[$key][$j]);
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
        for ($j = 3; $j < count($dfields); $j++) {
	   $plotfields[] = $dfields[$j];
	   $plotfiles[] = $dailyfiles[$i];
        }
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
   } else if ($isSub == 1) { 
      //dailyfiles defined in header
      $plotfields = array();
      $plotfiles = array();
      for ($i = 0; $i < count($dailyfiles); $i++) {
        //read file and parse fields and values into arrays
        $daily = file($pfix . $id . "_" . $dailyfiles[$i] . "output.txt");
        $temp = trim($daily[$skipl[$i]-1]);
        $dfields = preg_split("/\s+/", $temp);
        for ($j = 3; $j < count($dfields); $j++) {
           $plotfields[] = $dfields[$j];
           $plotfiles[] = $dailyfiles[$i];
        }
      }
   }

   if ($isSub < 2) {

      echo '<div id="viewfiles" style="margin-left: 75px">';
      //list output files
      $dataDir = substr($pfix, 0, strrpos($pfix, '/'));
      $f = opendir($dataDir);
      while ($temp = readdir($f)) {
        if (preg_match("/{$id}/i", $temp) && !preg_match("/run/i", $temp)) {
        #if (eregi($id, $temp) && !eregi('run', $temp)) {
           $outfiles[] = $temp;
        }
      }
      sort($outfiles);
      for ($j = 0; $j < count($outfiles); $j++) {
        if ($pfix == "data/i") {
          $outname = substr($outfiles[$j], strpos($outfiles[$j], '_')+1);
        } else $outname = $outfiles[$j];
        echo '<a href="' . $dataDir . '/' . $outfiles[$j] . '">' . $outname .  '</a><br>';
      }
      //echo '<br>';
      //$wthfile = $_POST['WFNAME'] . '.wth';
      //echo '<a href="' . $wthfile . '">' . $wthfile . '</a><br>';
      echo '</div>';

      echo '<form name="graphForm">';
      echo '<br><b>Daily graphs:</b><br><br>Select an individual year or the mean of all years to display ';
      echo '<select name="yearPlot" onChange="updateAllGraphs(\'' . $id . '\');"><option name="mean" value="mean">Mean';
      for ($j = $startYr; $j <= $endYr; $j++) {
        echo '<option name=' . $j . ' value=' . $j . '>' . $j;
      }
      echo '</select>';
      echo '<br><br>';

      echo '<table border=0>';
      echo '<tr><td>Select the dependent variable for graph #1:<br>';
      echo '<select name="varGraph1" onChange="updateGraph(\'imgGraph1\', \'' . $id . '\', this.selectedIndex, \'dailystats.txt\');">';
      for ($j = 0; $j < count($plotfields); $j++) {
	if ($plotfiles[$j] == "daily") echo '<option name="' . $plotfields[$j] . '" value="' . $plotfields[$j] . '">' . $plotfields[$j];
      }
      echo '</select></td>';
      echo '<td width=20></td>';
      echo '<td>Select the dependent variable for graph #2:<br>';
      echo '<select name="varGraph2" onChange="updateGraph(\'imgGraph2\', \'' . $id . '\', this.selectedIndex, \'Nplantstats.txt\');">';
      for ($j = 0; $j < count($plotfields); $j++) {
        if ($plotfiles[$j] == "Nplant") echo '<option name="' . $plotfields[$j] . '" value="' . $plotfields[$j] . '">' . $plotfields[$j];
      }
      echo '</select></td>';
      echo '</tr>';

      echo '<tr><td>';
      echo '<img name="imgGraph1" id="imgGraph1" src="graph.php?file=' . $pfix . $id . '_dailystats.txt&ymin=0&xcol=0&ycol=5&skipl=3&usedates=1">';
      echo '</td>';
      echo '<td></td>';
      echo '<td>';
      echo '<img name="imgGraph2" id="imgGraph2" src="graph.php?file=' . $pfix . $id . '_Nplantstats.txt&ymin=0&xcol=0&ycol=5&skipl=3&usedates=1">';
      echo '</td></tr>';

      echo '<tr><td>Select the dependent variable for graph #3:<br>';
      echo '<select name="varGraph3" onChange="updateGraph(\'imgGraph3\', \'' . $id . '\', this.selectedIndex, \'NRelstats.txt\');">';
      for ($j = 0; $j < count($plotfields); $j++) {
        if ($plotfiles[$j] == "NRel") echo '<option name="' . $plotfields[$j] . '" value="' . $plotfields[$j] . '">' . $plotfields[$j];
      }
      echo '</select></td>';
      echo '<td width=20></td>';
      echo '<td>Select the dependent variable for graph #4:<br>';
      echo '<select name="varGraph4" onChange="updateGraph(\'imgGraph4\', \'' . $id . '\', this.selectedIndex, \'Nleachstats.txt\');">';
      for ($j = 0; $j < count($plotfields); $j++) {
        if ($plotfiles[$j] == "Nleach") echo '<option name="' . $plotfields[$j] . '" value="' . $plotfields[$j] . '">' . $plotfields[$j];
      }
      echo '</select></td>';
      echo '</tr>';

      echo '<tr><td>';
      echo '<img name="imgGraph3" id="imgGraph3" src="graph.php?file=' . $pfix . $id . '_NRelstats.txt&ymin=0&xcol=0&ycol=5&skipl=3&usedates=1">';
      echo '</td>';
      echo '<td></td>';
      echo '<td>';
      echo '<img name="imgGraph4" id="imgGraph4" src="graph.php?file=' . $pfix . $id . '_Nleachstats.txt&ymin=0&xcol=0&ycol=5&skipl=3&usedates=1">';
      echo '</td></tr>';
      echo '</table>';
      echo '<br><br>';
      showOutputButtons();
      echo '</form>';

      echo '<br><br>';
      $notes = file('Notes_output.txt');
      for ($j = 0; $j < count($notes); $j++) {
	if (substr($notes[$j],0,3) == '---') echo '<hr>'; else echo $notes[$j] . '<br>';
      }
      if ($isIE) {
        //screw you Bill Gates!
        echo '<script language="javascript">billGatesSucks();</script>';
      }
  }
  require('footer.php');
?>
