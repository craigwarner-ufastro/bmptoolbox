<?php session_start(); ?>
<html>
<head>
<script language="Javascript">
   function checkForm() {
      var a = true;
      return a;
   }
</script>
<title>Container Crop Management Tool</title>
<?php require('header.php'); ?>
<!-- Start body here -->
<table border=0 align="center">
<tr><td>
<h1 class="h1-container">Webpage Statistics
<font color="#ffffff" class="h1-shadow">Webpage Statistics
</font></h1>
</td></tr></table>
<?php
   if (isset($_GET['start'])) $start = $_GET['start']; else $start=0;
   if (isset($_GET['cat'])) {
      $cat = $_GET['cat'];
      if ($cat < 1 || $cat > 7) $cat = 1;
   } else $cat = 1;
   echo '<center>';
   if ($cat != 1) echo '<a href="tracker.php?cat=1" class="myaccount">Individual Pages</a> | '; else echo 'Individual Pages | ';
   if ($cat != 2) echo '<a href="tracker.php?cat=2" class="myaccount">Operating Systems</a> | '; else echo 'Operating Systems | ';
   if ($cat != 3) echo '<a href="tracker.php?cat=3" class="myaccount">Browsers</a> | '; else echo 'Browsers | ';
   if ($cat != 4) echo '<a href="tracker.php?cat=4" class="myaccount">Resolutions</a> | '; else echo 'Resolutions | ';
   if ($cat != 5) echo '<a href="tracker.php?cat=5" class="myaccount">Referrers</a> | '; else echo 'Referrers | ';
   if ($cat != 6) echo '<a href="tracker.php?cat=6" class="myaccount">Users</a> | '; else echo 'Users | ';
   if ($cat != 7) echo '<a href="tracker.php?cat=7" class="myaccount">Totals Over Time</a>'; else echo 'Totals Over Time';
?>
<br>
<br>
<?php
  //setup arrays
  $hourList = Array();
  for ($j = 0; $j < 24; $j++) {
    if ($j % 12 == 0) $hourList[$j] = "12 "; else $hourList[$j] = ($j % 12) . " ";
    if ($j < 12) $hourList[$j] .= "am"; else $hourList[$j] .= "pm";
  }

  $dayList = Array();
  for ($j = 0; $j < 31; $j++) {
    $dayList[$j] = $j+1;
  }
  $monthList = array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");

  $yearList = Array();
  $currYear = floor(date("Y", time()));
  for ($j = 0; $j <= $currYear-2009; $j++) {
    $yearList[$j] = $j+2009;
  }
?>
<form name="statsForm" action="tracker.php?cat=<?php echo $cat; ?>" method="post" onSubmit="return checkForm();">
<input type="hidden" name="isSubmitted" value="true">
<table align="center" border=0>
  <tr>
    <th valign="top">Hour</th>
    <th valign="top" width=15></th>

    <th valign="top">Day</th>
    <th valign="top" width=15></th>

    <th valign="top">Month</th>
    <th valign="top" width=15></th>

    <th valign="top">Year</th>
    <th valign="top" width=15></th>

    <?php if ($cat < 7) { ?>
      <th valign="top">Sort By</th>
      <th valign="top" width=15></th>
    <?php } else { ?>
      <th valign="top">Display</th>
      <th valign="top" width=15></th>
    <?php } ?> 
    <th valign="top"></th>
  </tr>

  <tr>
    <td>
      <select name="hour">
      <option name="Any" value="Any">Any
      <?php
        for ($j = 0; $j < count($hourList); $j++) {
          echo '<option name=' . $j . ' value=' . $j;
          if (isset($_POST['hour']) && $_POST['hour'] == $j && $_POST['hour'] != 'Any') echo ' selected';
          echo '>' . $hourList[$j];
        }
      ?>
      </select>
    </td>
    <td></td>

    <td>
      <select name="day">
      <option name="Any" value="Any">Any
      <?php
	for ($j = 0; $j < count($dayList); $j++) {
	  echo '<option name=' . $dayList[$j] . ' value=' . $dayList[$j];
	  if (isset($_POST['day']) && $_POST['day'] == $dayList[$j]) echo ' selected';
	  echo '>' . $dayList[$j];
	}
      ?>
      </select>
    </td>
    <td></td>

    <td>
      <select name="month">
      <option name="Any" value="Any">Any
      <?php
	for ($j = 0; $j < count($monthList); $j++) {
	  echo '<option name="' . ($j+1) . '" value="' . ($j+1) . '"';
	  if (isset($_POST['day']) && $_POST['month'] == ($j+1)) echo ' selected';
	  echo '>' . $monthList[$j];
	}
      ?>
      </select>
    </td>
    <td></td>

    <td>
      <select name="year">
      <option name="Any" value="Any">Any
      <?php
	for ($j = 0; $j < count($yearList); $j++) {
          echo '<option name=' . $yearList[$j] . ' value=' . $yearList[$j];
	  if (isset($_POST['year']) && $_POST['year'] == $yearList[$j]) echo ' selected';
	  echo '>' . $yearList[$j];
        }
      ?>
      </select>
    </td>
    <td></td>

    <?php if ($cat < 7) { ?>
      <td>
	<select name="sortBy">
	<option name="Hits" value="Hits">Hits
        <option name="Visitors" value="Visitors"
	<?php if (isset($_POST['sortBy']) && $_POST['sortBy'] == 'Visitors') echo ' selected'; ?>
	>Unique Visitors
	<option name="Alphabetically" value="Alphabetically"
	<?php if (isset($_POST['sortBy']) && $_POST['sortBy'] == 'Alphabetically') echo ' selected'; ?>
	>Alphabetically
	</select>
      </td>
      <td><td>
    <?php } else { ?>
      <td>
	<select name="display">
	<option name="Years" value="Years">Years
        <option name="Months" value="Months"
        <?php if (isset($_POST['display']) && $_POST['display'] == 'Months') echo ' selected'; ?>
        >Months
	<option name="Days" value="Days"
	<?php if (isset($_POST['display']) && $_POST['display'] == 'Days') echo ' selected'; ?>
	>Days
        <option name="Hours" value="Hours"
        <?php if (isset($_POST['display']) && $_POST['display'] == 'Hours') echo ' selected'; ?>
        >Hours
	</select>
      </td>
      <td><td>
    <?php } ?>

    <td><input type="Submit" name="go" value="Go!"></td>
  </tr>
</table>
</form>
<br>
<?php
  if (isset($_POST['isSubmitted'])) {
    $hour = $_POST['hour'];
    $day = $_POST['day'];
    $month = $_POST['month'];
    $year = $_POST['year'];
    if ($cat < 7) {
      $sort = $_POST['sortBy'];
    } else $display = $_POST['display'];
  } else {
    $hour = "Any";
    $day = "Any";
    $month = "Any";
    $year = "Any";
    $sort = "Hits";
    $display = "Years";
  }

  $search = "";
  if ($hour != 'Any') $search .= " and HOUR(time)=" . $hour;
  if ($day != 'Any') $search .= " and DAY(time)=" . $day;
  if ($month != 'Any') $search .= " and MONTH(time)=" . $month;
  if ($year != 'Any') $search .= " and YEAR(time)=" . $year;
  
  $nPages = 0;
  $quantity = Array("page", "os", "browser", "res", "referrer", "username");
  if ($cat < 7) {
    $sql = "select distinct " . $quantity[$cat-1] . " from tracker where " . $quantity[$cat-1] . " != ''";
    $sql .= $search;
    $pages = Array();
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$pages[] = $row[$quantity[$cat-1]];
      }
    }
    $hits = Array();
    $unique = Array();
    for ($j = 0; $j < count($pages); $j++) {
      $sql = "select count(*) from tracker where " . $quantity[$cat-1] . "='" . $pages[$j] . "'";
      $sql .= $search;
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	  $hits[$pages[$j]] = $row[0];
	}
      }
      $sql = "select count(distinct ip) from tracker where " . $quantity[$cat-1] . "='" . $pages[$j] . "'";
      $sql .= $search;
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
          $unique[$pages[$j]] = $row[0];
        }
      }
    }
    $sql = "select count(distinct ip) from tracker where " . $quantity[$cat-1] . "!=''";
    $sql .= $search;
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $uniqueTot = $row[0];
      }
    }
  } else if ($cat == 7) {
    if ($display == "Years") {
      $func = "YEAR(time)-2009";
      $currList = $yearList;
    } else if ($display == "Months") {
      $func = "MONTH(time)-1";
      $currList = $monthList;
    } else if ($display == "Days") {
      $func = "DAY(time)-1";
      $currList = $dayList;
    } else if ($display == "Hours") {
      $func = "HOUR(time)";
      $currList = $hourList;
    }
    $search = "";
    if ($hour != 'Any') $search .= " and HOUR(time)=" . $hour;
    if ($day != 'Any') $search .= " and DAY(time)=" . $day;
    if ($month != 'Any') $search .= " and MONTH(time)=" . $month;
    if ($year != 'Any') $search .= " and YEAR(time)=" . $year;
    $hits = Array();
    $unique = Array();
    $nHits = 0;

    foreach ($currList as $key=>$value) {
      $sql = "select count(*) from tracker where " . $func . " = " . $key;
      $sql .= $search;
      $out = $mysqli_db->query($sql);
      if ($out != false) {
	if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	  $hits[$value] = $row[0];
	  if ($hits[$value] != 0) $nHits++;
        }
      }

      $sql = "select count(distinct ip) from tracker where " . $func . " = " . $key;
      $sql .= $search;
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
          $unique[$value] = $row[0];
        }
      }
    }
    $sql = "select count(distinct ip) from tracker where page != ''";
    $sql .= $search;
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $uniqueTot = $row[0];
      }
    }

    $max = max($hits);
    if ($max == 0) $max = 1;
    if ($hires) $scale = 450.0/$max; else $scale=300.0/$max;
    $scale = 450.0/$max;
    $pageTot = array_sum($hits);

    if ($nHits > 0) {
      $hitsAvg = $pageTot/$nHits;
      $uniqueAvg = array_sum($unique)/$nHits;
    } else {
      $hitsAvg = 0;
      $uniqueAvg = 0;
    }

    $resid = 0;
    foreach ($hits as $key => $value) {
      if ($value != 0) $resid+=pow($value-$hitsAvg, 2);
    }
    if ($nHits > 1) $stddev = sqrt($resid/($nHits-1)); else $stddev = 0;

    $resid = 0;
    foreach ($unique as $key => $value) {
      if ($value != 0) $resid+=pow($value-$uniqueAvg, 2);
    }
    if ($nHits > 1) $ustddev = sqrt($resid/($nHits-1)); else $ustddev = 0; 

    $hitsAvg = round($hitsAvg, 2);
    $uniqueAvg = round($uniqueAvg, 2);
    $stddev = round($stddev, 2);
    $ustddev = round($ustddev, 2);

    echo '<table border=0>';
    echo '<tr><th valign="top"><br>';
    if ($display == "Years") echo "Year";
    else if ($display == "Months") echo "Month";
    else if ($display == "Days") echo "Day";
    else if ($display == "Hours") echo "Hour";
    echo '</th>';

    echo '<th valign="top"></th>';
    echo '<th valign="top" style="color: blue;" align="center"><br>Hits</th>';
    echo '<th valign="top" width=25></th>';
    echo '<th valign="top" align="center"><br>Percent</th>';
    echo '<th valign="top" width=25></th>';
    echo '<th valign="top" style="color: green;" align="center">Unique<br>Visitors</th>';
    echo '</tr>';

    foreach ($hits as $key => $value) {
      $pct = sprintf("%01.1f", round(100.0*$hits[$key] / $pageTot,1));
      echo '<tr><td>' . $key . '</td>';
      echo '<td><img src="images/tracker_unique.png" width=';
      $uniqueWidth = max(round($scale*$unique[$key], 0), 1);
      echo $uniqueWidth . ' height=8>';
      if ($hits[$key] > $unique[$key]) {
	echo '<img src="images/tracker_base.png" width=';
	echo round($scale*($hits[$key]-$unique[$key]), 0);
	echo ' height=8>';
      }
      echo '</td>';
      echo '<td align="right">' . $hits[$key] . '</td>';
      echo '<td></td>';
      echo '<td align="right">' . $pct . '%</td>';
      echo '<td></td>';
      echo '<td align="right">' . $unique[$key] . '</td>';
      echo '</tr>';
    }
    echo '<tr><td>Avg</td>';
    echo '<td><img src="images/tracker_unique.png" width=';
    $uniqueWidth = max(round($scale*$uniqueAvg, 0), 1);
    echo $uniqueWidth . ' height=8>';
    if ($hitsAvg > $uniqueAvg) {
      echo '<img src="images/tracker_base.png" width=';
      echo round($scale*($hitsAvg-$uniqueAvg), 0);
      echo ' height=8>';
    }
    echo '</td>';
    echo '<td align="right">' . $hitsAvg . '</td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td align="right">' . $uniqueAvg . '</td>';
    echo '</tr>';

    
    echo '<tr><td>SDev</td>';
    echo '<td><img src="images/tracker_unique.png" width=';
    $uniqueWidth = max(round($scale*$ustddev, 0), 1);
    echo $uniqueWidth . ' height=8>';
    if ($stddev > $ustddev) {
      echo '<img src="images/tracker_base.png" width=';
      echo round($scale*($stddev-$ustddev), 0);
      echo ' height=8>';
    }
    echo '</td>';
    echo '<td align="right">' . $stddev . '</td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td align="right">' . $ustddev . '</td>';
    echo '</tr>';

    echo '<tr><td><b>Total</b></td>';
    echo '<td></td>';
    echo '<td align="right">' . $pageTot . '</td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td align="right">' . $uniqueTot . '</td>';
    echo '</tr>';
    echo '</table>';
  }

  if ($cat < 7) {
    $nPages = count($pages);
    if ($sort == 'Hits') {
      krsort($hits);
      arsort($hits);
      $idx = $hits;
    } else if ($sort == 'Visitors') {
      krsort($unique);
      arsort($unique);
      $idx = $unique;
    } else {
      asort($hits);
      ksort($hits);
      $idx = $hits;
    }
	
    if (count($hits) == 0) $max = 0; else $max = max($hits);
    if ($max == 0) $max = 1;
    if ($hires) $scale = 450.0/$max; else $scale=300.0/$max;
    $pageTot = array_sum($hits);

    echo '<table border=0 cellpadding=5 class="searchResultsTable" align="center" width=100%>';
    echo '<tr><td align="left" valign="top">';
    echo 'Displaying results ' . ($start+1) . ' - ' . min($nPages, $start+100) . ' of ' . $nPages;
    echo '</td>';
    echo '<td align="right" valign="top">';
    echo '<b>Results Page: </b>';
    $startPage = floor(max($start/100 - 5, 0))+1;
    $endPage = floor(min($startPage+99, ($nPages+99)/100));
    $currPage = floor($start/100)+1;
    if ($currPage != 1) {
      echo '<a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . ($start-100) . '" class="searchPage">Prev</a> ';
    }
    for ($j = $startPage; $j <= $endPage; $j++) {
      if ($j != $currPage) {
	echo ' <a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . (($j-1)*100) . '" class="searchPage">' . $j . '</a> ';
      } else {
	echo ' ' . $j . ' ';
      }
    }
    if ($currPage != floor(($nPages+99)/100)) {
      echo ' <a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . ($start+100) . '" class="searchPage">Next</a>';
    }
    echo '</td></tr></table>';
	
    echo '<table border=0>';
    echo '<tr><th valign="top" align="left"><br>';
    if ($cat == 1) echo 'Page';
    else if ($cat == 2) echo 'Operating System';
    else if ($cat == 3) echo 'Browser';
    else if ($cat == 4) echo 'Resolution';
    else if ($cat == 5) echo 'Referrer';
    else if ($cat == 6) echo 'User';
    echo '</th><th valign="top"></th>';
    echo '<th valign="top" style="color: blue;" align="center"><br>Hits</th>';
    echo '<th valign="top" width=25></th>';
    echo '<th valign="top" align="center"><br>Percent</th>';
    echo '<th valign="top" width=25></th>';
    echo '<th valign="top" style="color: green;" align="center">Unique<br>Visitors</th>';
    echo '</tr>';
       
    $curr = 0;
    foreach ($idx as $key => $value) {
      $pct = sprintf("%01.1f", round(100.0*$hits[$key] / $pageTot,1));
      $curr++;
      if ($curr <= $start or $curr > $start+100) continue;
      echo '<tr><td>';
      if ($cat == 1) {
	echo '<a href="' . $key . '" class="smallLink">' . $key . '</a>';
      } else if ($cat < 7) {
	echo $key;
      }
      echo '</td>';
      echo '<td><img src="images/tracker_unique.png" width=';
      $uniqueWidth = max(round($scale*$unique[$key], 0), 1);
      echo $uniqueWidth . ' height=8>';
      if ($hits[$key] > $unique[$key]) {
	echo '<img src="images/tracker_base.png" width=';
	echo round($scale*($hits[$key]-$unique[$key]), 0);
	echo ' height=8>';
      }
      echo '</td>';
      echo '<td align="right">' . $hits[$key] . '</td>';
      echo '<td></td>';
      echo '<td align="right">' . $pct . '%</td>';
      echo '<td></td>';
      echo '<td align="right">' . $unique[$key] . '</td>';
      echo '</tr>';
    }
    echo '<tr><td><b>Total</b></td>';
    echo '<td></td>';
    echo '<td align="right">' . $pageTot . '</td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td></td>';
    echo '<td align="right">' . $uniqueTot . '</td>';
    echo '</tr>';
    echo '</table>';

    echo '<table border=0 cellpadding=5 class="searchResultsTable" align="center" width=100%>';
    echo '<tr><td align="left" valign="top">';
    echo 'Displaying results ' . ($start+1) . ' - ' . min($nPages, $start+100) . ' of ' . $nPages;
    echo '</td>';
    echo '<td align="right" valign="top">';
    echo '<b>Results Page: </b>';
    $startPage = floor(max($start/100 - 5, 0))+1;
    $endPage = floor(min($startPage+99, ($nPages+99)/100));
    $currPage = floor($start/100)+1;
    if ($currPage != 1) {
      echo '<a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . ($start-100) . '" class="searchPage">Prev</a> ';
    }
    for ($j = $startPage; $j <= $endPage; $j++) {
      if ($j != $currPage) {
	echo ' <a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . (($j-1)*100) . '" class="searchPage">' . $j . '</a> ';
      } else {
	echo ' ' . $j . ' ';
      }
    }
    if ($currPage != floor(($nPages+99)/100)) {
      echo ' <a href="tracker.php?cat=' . $cat . '&year=' . $_POST['year'] . '&month=' . $_POST['month'] . '&day=' . $_POST['day'] . '&sortBy=' . $_POST['sortBy'] . '&isSubmitted=true&start=' . ($start+100) . '" class="searchPage">Next</a>';
    }
    echo '</td></tr></table>';
  }
?>
<!-- End body here -->
<?php require('footer.php'); ?>
