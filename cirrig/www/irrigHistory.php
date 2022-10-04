<?php session_start(); ?>
<html>
<head>
<!-- include CSS and JS files -->
<link rel=stylesheet href="driver.css" type="text/css">
<link rel=stylesheet href="calendar.css" type="text/css">
<style type="text/css">
  #dhtmltooltip{
    position: absolute;
    width: 150px;
    border: 2px solid black;
    padding: 2px;
    background-color: lightyellow;
    visibility: hidden;
    z-index: 100;
    /*Remove below line to remove shadow. Below line should always appear last within this CSS*/
    /*filter: progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135);*/
  }
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="CalendarPopup.js"></SCRIPT>

<?php
  #Define function outformat
  function outFormat($x, $dec) {
    if ($x === NULL) return '--';
    $dpos = stripos($x, '.');
    if ($dec > 0) {
      $x = (float)($x)*pow(10, $dec)+0.5;
      $x = (int)($x)/pow(10, $dec);
    } else {
      $x = (int)($x+0.5);
    }
    $x = (string)($x);
    $dpos = stripos($x, '.');
    if ($dpos === False && $dec > 0) {
      $x .= '.';
      for ($j = 0; $j < $dec; $j++) $x .= '0';
      return $x;
    } else if ($dec <= 0) return $x;
    for ($j = strlen($x); $j < $dpos+$dec+1; $j++) $x .= '0';
    return substr($x, 0, $dpos+$dec+1);
  }

?>

<script language="javascript">
  function check() {
    var a = true;
    if (document.reportForm.startmonth.value == 'select') {
      alert('You must enter a valid start date.');
      a = false;
    } else if (isNaN(parseInt(document.reportForm.startday.value))) {
      alert('You must enter a valid start date.');
      a = false;
    } else if (isNaN(parseInt(document.reportForm.startyear.value))) {
      alert('You must enter a valid start date.');
      a = false;
    } else if (document.reportForm.endmonth.value == 'select') {
      alert('You must enter a valid end date.');
      a = false;
    } else if (isNaN(parseInt(document.reportForm.endday.value))) {
      alert('You must enter a valid end date.');
      a = false;
    } else if (isNaN(parseInt(document.reportForm.endyear.value))) {
      alert('You must enter a valid end date.');
      a = false;
    } else {
      var d1 = new Date(parseInt(document.reportForm.startyear.value), parseInt(document.reportForm.startmonth.value)-1, parseInt(document.reportForm.startday.value));
      var d2 = new Date(parseInt(document.reportForm.endyear.value), parseInt(document.reportForm.endmonth.value)-1, parseInt(document.reportForm.endday.value));
      if (d2 < d1) {
        alert('Start date must be before end date!');
        a = false;
      }
    }
    return a;
  }

  var yForm = null;
  var mForm = null;
  var dForm = null;

  document.write(getCalendarStyles());
  var cal = new CalendarPopup("calDiv");
  cal.setCssPrefix("TEST");
  cal.setReturnFunction("setCalendarVals");
  cal.showYearNavigation();

  function setCalendarVals(y,m,d) {
     yForm.value=y;
     if (mForm.options.length == 12) {
       mForm.selectedIndex=m-1;
     } else if (mForm.options.length == 13) {
       mForm.selectedIndex=m;
     }
     dForm.value=d;
  }

  function updateCalendar(cal, anchor) {
    yForm = eval('document.forms[0].'+anchor+'year');
    mForm = eval('document.forms[0].'+anchor+'month');
    dForm = eval('document.forms[0].'+anchor+'day');
    selDate = '' + (mForm.selectedIndex+1);
    selDate += '/' + (dForm.value);
    selDate += '/' + yForm.value;

    d2 = new Date();
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(d2,"yyyy-MM-dd"), null);
    cal.showCalendar(anchor, selDate);
    return false;
  }

</script>
<title>Irrigation History</title>
</head>
<body>
<div id="calDiv" style="position:absolute;visibility:hidden;background-color:white;"></div>

<?php
  function formatDateToken($x) {
    //make sure to pad date tokens with 0s, e.g. 01 not 1
    if (strlen($x) == 1) return "0$x";
    return $x;
  }

  $db = new SQLite3('/home/pi/irrigHistory.sdb');
  echo "<h2><center>Irrigation History</center></h2>";

  $endDate = date("Y-m-d");
  $startDate = date("Y-m-d", time()-86400*7);
  $dailySummary = true;
  $search = "";
  $order = "dateTime DESC, outlet";
  if (isset($_POST['isSubmitted'])) {
    $startDate = $_POST['startyear'] . '-' . formatDateToken($_POST['startmonth']) . '-' . formatDateToken($_POST['startday']);
    $endDate = $_POST['endyear'] . '-' . formatDateToken($_POST['endmonth']) . '-' . formatDateToken($_POST['endday']);
    if ($_POST['irrigators'][0] != 'all') {
      $search .= " and (irrigIp='" . $_POST['irrigators'][0] . "'";
      for ($j = 1; $j < count($_POST['irrigators']); $j++) {
        $search .= " or irrigIp='" . $_POST['irrigators'][$j] . "'";
      }
      $search .= ")";
    }
    if ($_POST['zoneGroups'][0] != 'all') {
      $search .= " and (zoneGroup=" . $_POST['zoneGroups'][0];
      for ($j = 1; $j < count($_POST['zoneGroups']); $j++) {
        $search .= " or zoneGroup=" . $_POST['zoneGroups'][$j];
      }
      $search .= ")";
    }
    if ($_POST['sortBy'] == 'date2') {
      $order = "dateTime, outlet";
    } else if ($_POST['sortBy'] == 'irrigIP') {
      $order = "irrigIp, dateTime DESC, outlet";
    } else if ($_POST['sortBy'] == 'outlet') {
      $order = "irrigIp, outlet, dateTime DESC";
    }
    if (isset($_POST['dailySummary'])) {
      $dailySummary = true;
    } else $dailySummary = false;
    if ($dailySummary) {
      $order = "d DESC, outlet";
      if ($_POST['sortBy'] == 'date2') {
        $order = "d, outlet"; 
      } else if ($_POST['sortBy'] == 'irrigIP') {
        $order = "irrigIp, d DESC, outlet";
      } else if ($_POST['sortBy'] == 'outlet') {
	$order = "irrigIp, outlet, d DESC";
      }
    }
  }

  $dateclause = " where d >= '$startDate' AND d <= '$endDate'" . $search;
  $query = "select date(dateTime, 'unixepoch', 'localtime') as d, * from irrigHistory";
  $query .= $dateclause . " ORDER BY " . $order; 
  if ($dailySummary) {
    $query = "select date(dateTime, 'unixepoch', 'localtime') as d, *, sum(runTime) as totRT, sum(water) as totWater from irrigHistory";
    $query .= $dateclause . " group by d, plc ORDER BY " . $order; 
  }
  #echo $query . "<br>";

  $res = $db->query($query);
  $info = Array();
  $n = 0;
  while ($row = $res->fetchArray()) {
    $info[$n] = $row;
    #print_r($row);
    $n++;
  }

  $irrigIPs = Array();
  $res = $db->query("select DISTINCT irrigIP from irrigHistory;");
  while ($row = $res->fetchArray()) {
    $irrigIPs[] = $row['irrigIP'];
  }

  $zoneGroups = Array();
  $res = $db->query("select DISTINCT zoneGroup from irrigHistory;");
  while ($row = $res->fetchArray()) {
    $zoneGroups[] = $row['zoneGroup'];
  }
?>

<form name="irrigHistoryForm" action="irrigHistory.php" method="post" onSubmit='return check();'>

<input type="hidden" name="isSubmitted" value=1>
<div style="margin-left: 0px;" align="left">
<table border=0 align="center">
  <tr><th colspan=5 style="background: #55bbff;"><center><u>Filters</u></center></th></tr>
  <tr>
  <td valign="top">
    <b>Select irrigator(s):</b>
  </td>
  <td valign="top">
    <select name="irrigators[]" size=4 multiple onchange="unselect(this);">
    <option name="all" value="all" <?php if (!isset($_POST['irrigators'])) echo 'selected'; else if ($_POST['irrigators'][0] == 'all') echo 'selected'; ?>>All Irrigators 
    <?php
      #Irrigators pulldown
      sort($irrigIPs);
      foreach ($irrigIPs as $key => $value) {
        echo '<option name=' . $value . ' value=' . $value;
        if (isset($_POST['irrigators']) && array_search($value, $_POST['irrigators']) !== false) echo ' selected';
        echo '>' . $value;
      }
    ?>
    </select>
    <br><br>
  </td>
  <td valign="top" width=30></td>
  <td valign="top">
    <b>Select Zone Groups:</b>
  </td>
  <td valign="top">
    <select name="zoneGroups[]" size=4 multiple onchange="unselect(this);">
    <option name="all" value="all" <?php if (!isset($_POST['zoneGroups'])) echo 'selected'; else if ($_POST['zoneGroups'][0] == 'all') echo 'selected'; ?>>All
    <?php
      #Irrigators pulldown
      sort($zoneGroups);
      foreach ($zoneGroups as $key => $value) {
        echo '<option name=' . $value . ' value=' . $value;
        if (isset($_POST['zoneGroups']) && array_search($value, $_POST['zoneGroups']) !== false) echo ' selected';
        echo '>' . $value;
      }
    ?>
    </select>
    <br><br>
  </td>
  </tr>

  <tr>
  <td valign="top">
    <b>Daily Summaries Only:</b>
  </td>
  <td valign="top">
    <input type="checkbox" name="dailySummary" value="dailySummary" <?php if ($dailySummary) echo 'checked'; ?>/>
  </td>
  <td valign="top" width=30></td>
  <td valign="top">
    <b>Sort By:</b>
  </td>
  <td valign="top">
    <select name="sortBy">
    <option name="date1" value="date1">Date (Desc) then Outlet
    <option name="date2" value="date2" <?php if (isset($_POST['sortBy']) && $_POST['sortBy'] == "date2") echo "selected";?>>Date (Asc) then Outlet
    <option name="irrigIP" value="irrigIP" <?php if (isset($_POST['sortBy']) && $_POST['sortBy'] == "irrigIP") echo "selected";?>>Irrigator then Date (Desc) 
    <option name="outlet" value="outlet" <?php if (isset($_POST['sortBy']) && $_POST['sortBy'] == "outlet") echo "selected";?>>Irrigator/Outlet then Date (Desc)
    </select>
    <br><br>
  </td>
  </tr>

  <tr><td valign="top"><b>Start Date:</b></td>
    <td>
      <select name="startmonth">
      <?php
	$t = strtotime($startDate); 
        $months = array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
        for ($j = 0; $j < 12; $j++) {
          echo '<option name="' . $months[$j] . '" value="' . ($j+1) . '"';
          if (date("M", $t) == $months[$j]) echo " selected";
          echo '>' . $months[$j];
        }
      ?>
      </select>
      <input type="text" name="startday" size=2 maxlength=2 value="<?php echo date('d', $t);?>" onFocus="this.value='';" onkeypress="return validNum(event);">
      <input type="text" name="startyear" size=4 maxlength=4 value="<?php echo date('Y', $t);?>" onFocus="this.value='';" onkeypress="return validNum(event);">
      <a href="#" onClick="updateCalendar(cal, 'start'); return false;" name="start" id="start">select</a>
      <br><br>
  </td>
  <td valign="top" width=30></td>
  <td valign="top"><b>End Date:</b></td>
    <td>
      <select name="endmonth">
      <?php
        $t = strtotime($endDate); 
        $months = array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
        for ($j = 0; $j < 12; $j++) {
          echo '<option name="' . $months[$j] . '" value="' . ($j+1) . '"';
          if (date("M", $t) == $months[$j]) echo " selected";
          echo '>' . $months[$j];
        }
      ?>
      </select>
      <input type="text" name="endday" size=2 maxlength=2 value="<?php echo date('d', $t);?>" onFocus="this.value='';" onkeypress="return validNum(event);">
      <input type="text" name="endyear" size=4 maxlength=4 value="<?php echo date('Y', $t);?>" onFocus="this.value='';" onkeypress="return validNum(event);">
      <a href="#" onClick="updateCalendar(cal, 'end'); return false;" name="end" id="end">select</a>
      <br><br>
  </td></tr>

  <tr><td colspan=5><center>
    <input type="submit" name="submit" value="Submit">
    <input type="reset" name="reset" value="Reset">
    <br><br>
  </center></td></tr>
</table>
</div>
</form>

  <div style="margin-left: 0px;" align="center">
  <table border=1 align="center" class="smalltbl">
  <tr>
    <th valign="top" class="smalltblth-nobg" width=50>Day</th>
    <th valign="top" class="smalltblth-nobg" width=50>Date</th>
    <th valign="top" class="smalltblth" title="Irrigator">Irrigator</th>
    <th valign="top" class="smalltblth" title="Group">Group</th>
<?php
if (!$dailySummary) {
?>
    <th valign="top" class="smalltblth" title="Time Finished">Time<br>Finished</th>
<?php
}
?>
    <th valign="top" class="smalltblth" title="PLC">PLC</th>
    <th valign="top" class="smalltblth" title="Outlet">Outlet</th>
    <th valign="top" class="smalltblth" title="Zone">Zone</th>
    <th valign="top" class="smalltblth" title="Name">Name</th>
    <th valign="top" class="smalltblth" title="Plant">Plant</th>
<?php
if ($dailySummary) {
?>
    <th valign="top" class="smalltblth" title="Total Run Time (minutes)" align="right">Tot RT<br>(min)</th>
<?php
} else {
?>
    <th valign="top" class="smalltblth" title="Run Time (minutes)" align="right">RT<br>(min)</th>
<?php
}
?>
    <th valign="top" class="smalltblth" title="Water" align="right">Water</th>
  </tr>
<?php
  for ($j = 0; $j < $n; $j++) {
    if ($j%2 == 0) $td = '<td class="smalltbltd"'; else $td = '<td class="smalltbltdalt"';
    echo '<tr>';
    echo $td . '>' . date('D', $info[$j]['dateTime']) . '</td>';
    echo $td . '>' . $info[$j]['d'] . '</td>';
    echo $td . '>' . $info[$j]['irrigIP'] . '</td>';
    echo $td . '>' . $info[$j]['zoneGroup'] . '</td>';
    if (!$dailySummary) {
      echo $td . '>' . date('H:i:s', $info[$j]['dateTime']) . '</td>';
    }
    echo $td . '>' . $info[$j]['plc'] . '</td>';
    echo $td . '>' . $info[$j]['outlet'] . '</td>';
    echo $td . '>' . $info[$j]['zoneNumber'] . '</td>';
    echo $td . '>' . $info[$j]['zoneName'] . '</td>';
    echo $td . '>' . $info[$j]['plant'] . '</td>';
    if ($dailySummary) {
      echo $td . ' align="right">' . outFormat($info[$j]['totRT']/60.0, 2) . '</td>';
      echo $td . ' align="right">' . outFormat($info[$j]['totWater'], 3) . ' ' . $info[$j]['units'] . '</td>';
    } else {
      echo $td . ' align="right">' . outFormat($info[$j]['runTime']/60.0, 2) . '</td>';
      echo $td . ' align="right">' . outFormat($info[$j]['water'], 3) . ' ' . $info[$j]['units'] . '</td>';
    }
    echo '</tr>';
  }
?>
  </table>
  </div>
</body>
