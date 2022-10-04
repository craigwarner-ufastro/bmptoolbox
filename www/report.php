<?php
  ini_set('session.cache_limiter', 'private');
  session_start();
?>
<html>
<head>
<script language="javascript">
    document.onload = setTimeout("this.print();", 1000);
</script>
<title>BMP Toolbox Report</title>
<link rel=stylesheet href="report.css" type="text/css">
</head>
<?php
  /*header*/

  require('stats.php');
  $pagename = "index";
  $pfix = "data/i";
  if (isset($_POST['S_VOL'])) {
    $subVol = $_POST['S_VOL'];
  } else $subVol = 2400.0;
  $nruns = 1;
  $latest = file('fawn/latestUpdate.txt');
  $latest = trim($latest[0]);
  $npspec = count(file("../driver/1_P_spec.txt"))+1;
  $npspec /= 4;
  $tw=780;
  if (isset($_SESSION['pw'])) {
    if ($_SESSION['pw']*2+40 > $tw) $tw = $_SESSION['pw']*2+40;
  }

  $isIE = false;
  $useragent = $_SERVER['HTTP_USER_AGENT'];
  if (preg_match("/MSIE/i", $useragent)) $isIE = true;
  #if (eregi("MSIE", $useragent)) $isIE = true;
  echo '<script language="javascript">';
  if ($isIE) echo 'var isIE = true;'; else echo 'var isIE=false;';
  echo '</script>';

  $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");

  function dateFromDoy($x) {
    $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");
    $days = array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    $n = 0;
    while ($x > $days[$n]) {
      $x-=$days[$n];
      $n++;
    }
    $s = $months[$n] . ' ' . $x;
    return $s;
  }

  $reportType = "grower";
  $pagename = "grower";
  if (isset($_POST['type'])) $reportType = $_POST['type'];
  if (isset($_POST['pagename'])) $pagename = $_POST['pagename'];

  if (isset($_POST['runid'])) {
    $isSub = 1;
    $id = $_POST['runid'];
    if (isset($_POST['pfix'])) $pfix = $_POST['pfix'];
    echo '<script language="javascript">pfix="' . $pfix . '";</script>';
    if ($reportType != "comparison" and file_exists($pfix . $id . '_P_spec.txt')) {
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
    } else if ($reportType == "comparison") {
      $nruns = 0;
      for ($j = 0; $j < 4; $j++) {
        if (file_exists($pfix . $id . ($j+1) . '_P_spec.txt')) {
          $nruns+=1;
          $spec = file($pfix . $id . ($j+1) . '_P_spec.txt');
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
      }
    }
  }
  $fert = $input['FERT'];
  $pctn = $input['PCT_N'];
  $pctp = $input['PCT_P'];
  $wfname = $input['WFNAME'];
  $pltdoy = $input['PLT_DOY'];
  $sched = $input['SCHED'];
  $startYr = $input['START_YR'];
  $endYr = $input['END_YR'];
  $irrig = ($sched == "MAD" ? "ET-based" : "Fixed");

  if (isset($_POST['location'])) $wfname = $_POST['location'];
  if (isset($_POST['irrig'])) $irrig = $_POST['irrig'];

  echo '<center><h2>';
  echo 'CCROP v1.0 Report from www.bmptoolbox.org';
  echo '</h2></center>';

  if ($reportType == "realtime") {
    $todayDate = $_POST['todayDate'];
    $todayIr = $_POST['todayIr'];
    echo '<h3>Today’s Irrigation Recommendation - ' . strftime("%a %b %d, %Y", $todayDate) . '</h3>';
    echo '<table border=0 align="center"><tr><td>';
    echo '<div align="center" class="realtimeRec">' . $todayIr . ' inches</div>';
    echo '</td></tr></table>';
    echo '<i>Note: This recommendation is an estimate based upon an average plant and assumes uniform irrigation within the production area.</i><br><br>';
    echo '<br>';
  }

  echo '<br><b>Inputs:</b><div class="indent75">';
  echo '<b>Run Type:</b> ';
  if (strpos($pagename, "location") !== false) echo 'Location Comparision Tool<br>'; 
  else if (strpos($pagename, "date") !== false) echo 'Plant Date Comparison Tool<br>';
  else if (strpos($pagename, "irr") !== false && strpos($pagename, "fert" !== false)) echo 'Fertilizer and Irrigation Comparison Tool<br>';
  else if (strpos($pagename, "irr") !== false) echo 'Irrigation Comparison Tool<br>';
  else if (strpos($pagename, "fert") !== false) echo 'Fertilizer Rate Comparison Tool<br>';
  else if (strpos($pagename, "realtime") !== false) echo 'Real-time Irrigation Tool<br>';
  else if (strpos($pagename, "grower") !== false) echo 'Grower Tool<br>';
  else if (strpos($pagename, "technical") !== false) echo 'Technical Tool<br>';
  echo '<b>Run ID</b>: ' . $id . '<br>';
  if ($reportType == "comparison") {
    if (strpos($pagename, "location") === false) echo 'Location: ' . str_replace('_', ' ' , $wfname) . '<br>';
    if (strpos($pagename, "date") === false) echo 'Plant Date: ' . dateFromDoy($pltdoy) . '<br>';
    if (strpos($pagename, "irr") === false) echo 'Irrigation: ' . $irrig . '<br>';
    if (strpos($pagename, "fert") === false) echo 'Fertilizer Rate: ' . outFormat($fert*$pctn/($subVol*0.059325)+0.005, 2) . ' lb N per cubic yard<br>';
  } else {
    echo 'Location: ' . str_replace('_', ' ', $wfname) . '<br>';
    echo 'Plant Date: ' . dateFromDoy($pltdoy) . '<br>';
    echo 'Irrigation: ' . $irrig . '<br>';
    echo 'Fertilizer Rate: ' . outFormat($fert*$pctn/($subVol*0.059325)+0.005, 2) . ' lb N per cubic yard<br>';
  }
  echo 'Start Year: ' . $startYr . '<br>';
  echo 'End Year: ' . $endYr . '<br>';
  echo '</div><br>';

  if ($reportType == "grower" or $reportType == "realtime") {
    if (isset($_POST['timeplots'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Daily Time Plots:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
	if (strpos($key, "timeplots_") !== false) {
	  if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
	  echo '<td valign="top">';
	  echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
	  echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
	  $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['sumcharts'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Summary Bar Charts:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "sumcharts_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgBar' . $i . '" id="imgBar' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['sumtables'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Summary Tables:</b><br>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "sumtables_") !== false) {
	  $i = (int)substr($key, strrpos($key, "_")+1)+1;;
	  $data = file($pfix . $id . '_table' . $i . '.csv');
	  echo $value;
	  echo '<table border=1 width=' . ($tw+100) . '>';
	  for ($j = 0 ; $j < count($data); $j++) {
	    $temp = explode(",", $data[$j]);
	    $td = '<td align="right">';
	    if (substr($temp[0], 0, 1) == 'M' or substr($temp[0], 0, 1) == 'S') {
	      $td = '<td align="right" style="background: #C0C0C0">';
	    }
	    if ($j == 0) $td = '<th align="right">';
	    echo '<tr>';
	    for ($l = 0; $l < count($temp); $l++) {
	      echo $td . $temp[$l];
	      if ($j == 0) echo '</th>'; else echo '</td>';
	    }
	    echo '</tr>'; 
	  }
	  echo '</table>';
	  echo '<br><br>';
        }
      }
      echo '</div>';
    }
  }

  if ($reportType == "comparison") {
    if (isset($_POST['sumgraphs'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Summary Comparison Graphs:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "sumgraphs_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['sumtables'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Summary Table:</b><br>';
      $data = file($pfix . $id . '_table.csv');
      echo '<table border=1 width=' . $tw . '>';
      for ($j = 0 ; $j < count($data); $j++) {
        $temp = explode(",", $data[$j]);
        $td = '<td align="right">';
        if (substr($temp[0], 0, 1) == 'M' or substr($temp[0], 0, 1) == 'S') {
          $td = '<td align="right" style="background: #C0C0C0">';
        }
        if ($j == 0) $td = '<th align="right">';
        echo '<tr>';
        for ($l = 0; $l < count($temp); $l++) {
          echo $td . $temp[$l];
          if ($j == 0) echo '</th>'; else echo '</td>';
        }
        echo '</tr>';
      }
      echo '</table>';
      echo '<br><br>';
      echo '</div>';
    }
  }

  if ($reportType == "technical") {
    if (isset($_POST['dailystats_txt'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Variables from dailystats.txt:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "dailystats_txt_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['Nplantstats_txt'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Variables from Nplantstats.txt:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "Nplantstats_txt_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['NRelstats_txt'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Variables from NRelstats.txt:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "NRelstats_txt_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
    if (isset($_POST['Nleachstats_txt'])) {
      echo '<div style="page-break-after:always;">';
      echo '<br><b>Variables from Nleachstats.txt:</b><p>';
      $i = 0;
      echo '<table border=0>';
      foreach ($_POST as $key => $value) {
        if (strpos($key, "Nleachstats_txt_") !== false) {
          if ($i % 2 == 0) echo '<tr>'; else echo '<td width=20 valign="top"></td>';
          echo '<td valign="top">';
          echo '<img name="imgGraph' . $i . '" id="imgGraph' . $i . '" src="' . $value . '">';
          echo '</td>';
          if ($i % 2 == 1) echo '</tr>';
          $i++;
        }
      }
      echo '</table>';
      echo '</div>';
    }
  }
?>
</body>
</html>
