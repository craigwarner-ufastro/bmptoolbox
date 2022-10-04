<?php session_start(); ?>
<html>
<head>
<?php
  if (isset($_GET['wsid']) && is_numeric($_GET['wsid'])) {
    $wsid = (int)$_GET['wsid'];
  } else {
    $wsid = -1;
  }
  $mode = '';
  if (isset($_POST['mode'])) {
    $mode = $_POST['mode'];
  }

  $showDaily = false;
  if (isset($_GET['daily']) && $_GET['daily'] == 1) $showDaily = true;
  $endDate = date("Y-m-d"); 
  $startDate = date("Y-m-d", time()-86400*7);
  if ($showDaily) $startDate = date("Y-m-d", time()-86400*28);
  if (isset($_POST['startDate'])) {
    $startDate = $_POST['startDate'];
  }
  if (isset($_POST['endDate'])) {
    $endDate = $_POST['endDate'];
  }
?>
<script language="javascript">
  var solarRads = Array();
  var minTemps = Array();
  var maxTemps = Array();
  var rains = Array();
  var currDateField = null;

  function editWeather(n) {
    var tdEdit = document.getElementById("tdEdit"+n);
    var tdSolarRad = document.getElementById("tdSolarRad"+n);
    solarRads[n] = tdSolarRad.innerHTML;
    var tdMinTemp = document.getElementById("tdMinTemp"+n);
    minTemps[n] = tdMinTemp.innerHTML;
    var tdMaxTemp = document.getElementById("tdMaxTemp"+n);
    maxTemps[n] = tdMaxTemp.innerHTML;
    var tdRain = document.getElementById("tdRain"+n);
    rains[n] = tdRain.innerHTML;
    tdEdit.innerHTML = '[<a href="javascript:cancelEdit(' + n + ');">cancel</a>]';
    tdSolarRad.innerHTML = '<input type="text" name="wSolarRad_' + n + '" size=12 value="' + solarRads[n]+'">';
    tdMinTemp.innerHTML = '<input type="text" name="wMinTemp_' + n + '" size=6 value="' + minTemps[n]+'" onkeypress="return validDec(event);">';
    tdMaxTemp.innerHTML = '<input type="text" name="wMaxTemp_' + n + '" size=6 value="' + maxTemps[n]+'" onkeypress="return validDec(event);">';
    tdRain.innerHTML = '<input type="text" name="wRain_' + n + '" size=6 value="' + rains[n]+'" onkeypress="return validDec(event);">';
  }

  function cancelEdit(n) {
    var tdEdit = document.getElementById("tdEdit"+n);
    var tdSolarRad = document.getElementById("tdSolarRad"+n);
    var tdMinTemp = document.getElementById("tdMinTemp"+n);
    var tdMaxTemp = document.getElementById("tdMaxTemp"+n);
    var tdRain = document.getElementById("tdRain"+n);
    tdEdit.innerHTML = '[<a href="javascript:editWeather(' + n + ');">edit</a>]';
    tdSolarRad.innerHTML = solarRads[n];
    tdMinTemp.innerHTML = minTemps[n];
    tdMaxTemp.innerHTML = maxTemps[n];
    tdRain.innerHTML = rains[n];
  }

  function updateWthCalendar(cal, dateField, anchor) {
    currDateField = dateField;
    var selDate = dateField.value;
    var startDate = new Date();
    var endDate = new Date();
    var sd = new Date();
    var ed = new Date();
    ed.setDate(ed.getDate()+1);
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(ed,"yyyy-MM-dd"), null);
    //cal.addDisabledDates(null, formatDate(sd,"yyyy-MM-dd"));
    cal.showCalendar(anchor, selDate);
    return false;
  }

  function setWthCalendarVals(y,m,d) {
    currDateField.value = ""+y+"-"+m+"-"+d;
  }

</script>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
<script language+"javascript">
  cal.setReturnFunction("setWthCalendarVals");
</script>
	<!-- Start body here -->
<?php
  if (!$isLoggedIn && !$doPreview) {
    $wsid = -1;
    echo 'You must <a href="login.php">login</a> to view and edit your weather.';
    exit(0);
  }

  if ($isLoggedIn) $uid = $_SESSION['bmpid']; else $uid=39;
  $sql = "select * from weatherStations where (uid = $uid or public=1) and wsid = $wsid";
  $out = $mysqli_db->query($sql);
  if ($out != false) {
    if ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $wsLocation = $row['location'];
    }
  } else if (!$isLoggedIn) {
    $wsid = -1;
    echo 'You must <a href="login.php">login</a> to view and edit your weather.';
    exit(0);
  }

  echo "<center><h2><center>Weather for: $wsLocation</h2>";
  if ($showDaily) {
    echo '<a href="viewWeather.php?wsid=' . $wsid . '&hourly=1" class="myaccount">Hourly Weather</a> | Daily Weather';
  } else {
    echo 'Hourly Weather | <a href="viewWeather.php?wsid=' . $wsid . '&daily=1" class="myaccount">Daily Weather</a>';
  }
  echo '</center><br>';

  if ($mode == 'weather' && $isLoggedIn) {
    if ($showDaily) {
      foreach ($_POST as $key=>$value) {
        //delete params
        if (substr($key, 0, 4) == 'del-' && $value == 'on') {
          $id = substr($key, 4);
          $sql = 'delete from weather where wid="' . $id . '" AND uid=' . $_SESSION['bmpid'];
        } else if (strpos($key, 'wSolarRad_') !== false) {
          $wid = substr($key, strrpos($key, '_')+1);
          $sql = "update weather set solar_radiation='$value' where wid=$wid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wMinTemp_') !== false) {
          $wid = substr($key, strrpos($key, '_')+1);
          $sql = "update weather set min_temp=$value where wid=$wid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wMaxTemp_') !== false) {
          $wid = substr($key, strrpos($key, '_')+1);
          $sql = "update weather set max_temp=$value where wid=$wid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wRain_') !== false) {
          $wid = substr($key, strrpos($key, '_')+1);
          $sql = "update weather set rain_in=$value where wid=$wid and uid=" . $_SESSION['bmpid'] . ";";
        }
        $out = $mysqli_db->query($sql);
      }
    } else {
      foreach ($_POST as $key=>$value) {
        //delete params
        if (substr($key, 0, 4) == 'del-' && $value == 'on') {
          $id = substr($key, 4);
          $sql = 'delete from hourlyWeather where hwid="' . $id . '" AND uid=' . $_SESSION['bmpid'];
        } else if (strpos($key, 'wSolarRad_') !== false) {
          $hwid = substr($key, strrpos($key, '_')+1);
          $sql = "update hourlyWeather set solar_radiation='$value' where hwid=$hwid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wMinTemp_') !== false) {
          $hwid = substr($key, strrpos($key, '_')+1);
          $sql = "update hourlyWeather set min_temp=$value where hwid=$hwid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wMaxTemp_') !== false) {
          $hwid = substr($key, strrpos($key, '_')+1);
          $sql = "update hourlyWeather set max_temp=$value where hwid=$hwid and uid=" . $_SESSION['bmpid'] . ";";
        } else if (strpos($key, 'wRain_') !== false) {
          $hwid = substr($key, strrpos($key, '_')+1);
          $sql = "update hourlyWeather set rain_in=$value where hwid=$hwid and uid=" . $_SESSION['bmpid'] . ";";
        }
        $out = $mysqli_db->query($sql);
      }
    }
  }

  $info = Array();
  #$sql = "select * from hourlyWeather where uid = " . $_SESSION['bmpid'] . " AND wsid = $wsid AND hour >= DATE('$startDate') AND hour <= TIMESTAMP('$endDate 23:59:59') ORDER BY hour DESC";
  $sql = "select * from hourlyWeather where wsid = $wsid AND hour >= DATE('$startDate') AND hour <= TIMESTAMP('$endDate 23:59:59') ORDER BY hour DESC";
  if ($showDaily) {
    #$sql = "select * from weather where uid = " . $_SESSION['bmpid'] . " AND wsid = $wsid AND date >= DATE('$startDate') AND date <= TIMESTAMP('$endDate 23:59:59') ORDER BY date DESC";
    $sql = "select * from weather where wsid = $wsid AND date >= DATE('$startDate') AND date <= TIMESTAMP('$endDate 23:59:59') ORDER BY date DESC";
  }
  $out = $mysqli_db->query($sql);
  $n = 0;
  if ($out != false) {
    while ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $info[$n] = $row;
      $n++;
    }
  }
?>
  <form name="manageWeather" action="viewWeather.php?wsid=<?php echo $wsid; if ($showDaily) echo '&daily=1'; ?>" method="post">

  <table border=0 width=75% align="center">
  <tr>
    <td valign="top" align="left">
      Start Date: <input type="text" name="startDate" size=10 readonly value="<?php echo $startDate;?>">
      <a href="#" onClick="updateWthCalendar(cal, document.manageWeather.startDate, 'anchor1x'); return false;" name="anchor1x" id="anchor1x">select</a>
    </td>
    <td valign="top" width="100%" align="center">
      <input type="submit" name="submit" value="Submit">
    </td>
    <td valign="top" align="right">
      End Date: <input type="text" name="endDate" size=10 readonly value="<?php echo $endDate;?>">
      <a href="#" onClick="updateWthCalendar(cal, document.manageWeather.endDate, 'anchor2x'); return false;" name="anchor2x" id="anchor2x">select</a>
    </td>
  </tr></table>
  <br><br>

  <input type="hidden" name="mode" value="weather">
  <div style="margin-left: 0px;" align="center">
  <table border=1 align="center" class="manage">
  <tr>
    <th valign="top" class="manageth-nobg">ID</th>
    <th valign="top" class="manageth"><?php if ($showDaily) echo "Date"; else echo "Timestamp"; ?></th>
    <th valign="top" class="manageth">Solar Rad<br>(W/m<sup>2</sup>)</th>
    <th valign="top" class="manageth">Min Temp (F)</th>
    <th valign="top" class="manageth">Max Temp (F)</th>
    <th valign="top" class="manageth">Rain (in)</th>
<?php if ($isLoggedIn) {
?>
    <th valign="top" class="manageth">Edit</th>
    <th valign="top" class="manageth">Delete</th>
<?php } ?>
  </tr>
<?php
  if ($showDaily) {
    for ($j = 0; $j < $n; $j++) {
      if ($j%2 == 0) $td = '<td class="managetd"'; else $td = '<td class="managetdalt"';
      echo '<tr>';
      echo $td . '>' . $info[$j]['wid'] . '</td>';
      echo $td . ' id="tdTime' . $info[$j]['wid'] . '">' . $info[$j]['date'] . '</td>';
      echo $td . ' id="tdSolarRad' . $info[$j]['wid'] . '">' . $info[$j]['solar_radiation'] . '</td>';
      echo $td . ' id="tdMinTemp' . $info[$j]['wid'] . '">' . $info[$j]['min_temp'] . '</td>';
      echo $td . ' id="tdMaxTemp' . $info[$j]['wid'] . '">' . $info[$j]['max_temp'] . '</td>';
      echo $td . ' id="tdRain' . $info[$j]['wid'] . '">' . $info[$j]['rain_in'] . '</td>';
      if ($isLoggedIn) {
        echo $td . ' id="tdEdit' . $info[$j]['wid'] . '">[<a href="javascript:editWeather(' . $info[$j]['wid'] . ');">edit</a>]</td>';
        echo $td . '><input type="checkbox" name="del-' . $info[$j]['wid'] . '"></td>';
      }
      echo '</tr>';
    }
  } else {
    for ($j = 0; $j < $n; $j++) {
      if ($j%2 == 0) $td = '<td class="managetd"'; else $td = '<td class="managetdalt"';
      echo '<tr>';
      echo $td . '>' . $info[$j]['hwid'] . '</td>';
      echo $td . ' id="tdTime' . $info[$j]['hwid'] . '">' . $info[$j]['hour'] . '</td>';
      echo $td . ' id="tdSolarRad' . $info[$j]['hwid'] . '">' . $info[$j]['solar_radiation'] . '</td>';
      echo $td . ' id="tdMinTemp' . $info[$j]['hwid'] . '">' . $info[$j]['min_temp'] . '</td>';
      echo $td . ' id="tdMaxTemp' . $info[$j]['hwid'] . '">' . $info[$j]['max_temp'] . '</td>';
      echo $td . ' id="tdRain' . $info[$j]['hwid'] . '">' . $info[$j]['rain_in'] . '</td>';
      if ($isLoggedIn) {
        echo $td . ' id="tdEdit' . $info[$j]['hwid'] . '">[<a href="javascript:editWeather(' . $info[$j]['hwid'] . ');">edit</a>]</td>';
        echo $td . '><input type="checkbox" name="del-' . $info[$j]['hwid'] . '"></td>';
      }
      echo '</tr>';
    }
  }
?>
  </table>
  <br>
  <input type="submit" name="submit" value="Submit">
  <input type="reset" name="reset" value="Reset">
  </div>
  </form>
	<!-- End body here -->
<?php require('footer.php'); ?>
