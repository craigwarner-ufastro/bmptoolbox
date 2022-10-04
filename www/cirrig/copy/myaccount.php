<?php session_start(); ?>
<html>
<head>
<script language="javascript">
  function switchRes(x) {
    header = document.getElementById("header");
    var bigPlots = Array(2, 3, 5, 6);
    if (x == 0) {
      header.src = "images/top_banner_bmp_hires.png";
      for (j = 0; j < bigPlots.length; j++) {
	document.prefs.plotSize[bigPlots[j]].disabled=false;
      }
    } else {
      header.src = "images/top_banner_bmp_lores.png";
      for (j = 0; j < bigPlots.length; j++) {
	if (document.prefs.plotSize[bigPlots[j]].checked) document.prefs.plotSize[1].checked=true;
        document.prefs.plotSize[bigPlots[j]].disabled=true;
      }
    }
  }

  function checkUploaded() {
    var validExt = false;
    var ext = document.uploadfiles.userfile.value.substring(document.uploadfiles.userfile.value.length-4);
    if (ext == ".wth") validExt = true;
    if (ext == ".txt") validExt = true;
    if (ext == ".csv") validExt = true;
    if (!validExt) {
      alert('Invalid file type!  Must be .wth, .txt., or .csv.');
      return false;
    }
    return retVal;
  }
</script>
<?php
   if (isset($_GET['cat'])) {
      $mycat = $_GET['cat'];
      if ($mycat < 1 or $mycat > 5) $mycat = 0;
   } else {
      $mycat = 0;
   }
   $mode = '';
   if (isset($_POST['mode'])) {
      $mode = $_POST['mode'];
   }
  if ($mode == "weather") $mycat = 3;
  if ($mode == "createzone" || $mode == "editzone" || $mode == "zones" || $mode == "globalzone" || $mode == "uploadzones") $mycat = 4;
   if ($mycat == 1 && $isLoggedIn) {
?>
<script language="javascript">
function checkPass() {
   var a = true;
   if (document.changepass.passwd1.value != document.changepass.passwd2.value) {
      alert('Passwords do not match.');
      a = false;
   } else if (document.changepass.passwd1.value.length < 5) {
      alert('Your password must be at least 5 characters.');
      a = false;
   }
   return a;
}
</script>
<?php
   } else if ($mycat == 2 && $isLoggedIn) {
?>
<script language="javascript">
function checkInfo() {
   var d = new Date();
   var a = true;
   var nroles = document.changeinfo.nroles.value;
   var roleSelected = false;
   for (j = 0; j < nroles-1; j++) {
      if (document.getElementById("role"+j).checked) roleSelected = true;
   }
   if (document.getElementById("roleO").checked) roleSelected = true;
   if (document.changeinfo.first.value == '') {
      alert('You must enter your first name.');
      a = false;
   } else if (document.changeinfo.last.value == '') {
      alert('You must enter your last name.');
      a = false;
   } else if (document.changeinfo.affiliation.value == '') {
      alert('You must enter your affiliation.');
      a = false;
   } else if (!roleSelected) {
      alert('You must select an affiliation role.');
      a = false;
   } else if (document.getElementById("roleO").checked && document.changeinfo.role_other.value == '') {
      alert('You must specify an affiliation role.');
      a = false;
   } else if (document.changeinfo.state.value == 'select') {
      alert('You must select a state.');
      a = false;
   } else if (document.changeinfo.state.value == 'None' && document.changeinfo.country.value == 'United States') {
      alert('You must select a state.');
      a = false;
   } else if (document.changeinfo.country.value == 'select') {
      alert('You must select a country.');
      a = false;
   } else if (document.changeinfo.email.value == '') {
      alert('You must enter your e-mail address.');
      a = false;
   } else if (document.changeinfo.email.value.indexOf('@') == -1) {
      alert('You must enter a VALID e-mail address.');
      a = false;
   } else if (document.changeinfo.month.value == 'select') {
      alert('You must enter your birthday.');
      a = false;
   } else if (isNaN(parseInt(document.changeinfo.day.value))) {
      alert('You must enter your birthday.');
      a = false;
   } else if (isNaN(parseInt(document.changeinfo.year.value))) {
      alert('You must enter your birthday.');
      a = false;
   }
   return a;
}
</script>
<?php
  } else if ($mycat == 3) {
?>
<script language="javascript">
  var dbNames = Array();
  var wsLoc = Array();
  var wsElev = Array();
  var wsLatt = Array();
  var wsLong = Array();

  function checkWS() {
    var a = true;
    for (var j = 0; j < dbNames.length; j++) {
      if (dbNames[j] == document.manageWS.wsLocation.value) {
        alert('A weather station with the name '+dbNames[j]+' already exists.');
        a = false;
      }
    }
    if (document.manageWS.wsLocation.value != '' || document.manageWS.elev.value != '' || document.manageWS.lattitude.value != '' || document.manageWS.longitude.value != '') {
      if (document.manageWS.wsLocation.value == '') {
	alert('You must enter a location name');
	a = false;
      } else if (document.manageWS.elev.value == '') {
        alert('You must enter an elevation');
        a = false;
      } else if (document.manageWS.lattitude.value == '') {
        alert('You must enter a lattitude');
        a = false;
      } else if (document.manageWS.longitude.value == '') {
        alert('You must enter a longitude');
        a = false;
      }
    }
    return a;
  }

  function editWS(n) {
    var tdEdit = document.getElementById("tdEdit"+n);
    var tdLoc = document.getElementById("tdLoc"+n);
    wsLoc[n] = tdLoc.innerHTML;
    var tdElev = document.getElementById("tdElev"+n);
    wsElev[n] = tdElev.innerHTML;
    var tdLatt = document.getElementById("tdLatt"+n);
    wsLatt[n] = tdLatt.innerHTML;
    var tdLong = document.getElementById("tdLong"+n);
    wsLong[n] = tdLong.innerHTML;
    tdEdit.innerHTML = '[<a href="javascript:cancelEdit(' + n + ');">cancel</a>]';
    tdLoc.innerHTML = '<input type="text" name="wsLocation_' + n + '" size=12 value="' + wsLoc[n]+'">';
    tdElev.innerHTML = '<input type="text" name="elev_' + n + '" size=6 value="' + wsElev[n]+'" onkeypress="return validDec(event);">';
    tdLatt.innerHTML = '<input type="text" name="lattitude_' + n + '" size=6 value="' + wsLatt[n]+'" onkeypress="return validDec(event);">';
    tdLong.innerHTML = '<input type="text" name="longitude_' + n + '" size=6 value="' + wsLong[n]+'" onkeypress="return validDec(event);">';
  }

  function cancelEdit(n) {
    var tdEdit = document.getElementById("tdEdit"+n);
    var tdLoc = document.getElementById("tdLoc"+n);
    var tdElev = document.getElementById("tdElev"+n);
    var tdLatt = document.getElementById("tdLatt"+n);
    var tdLong = document.getElementById("tdLong"+n);
    tdEdit.innerHTML = '[<a href="javascript:editWS(' + n + ');">edit</a>]';
    tdLoc.innerHTML = wsLoc[n];
    tdElev.innerHTML = wsElev[n];
    tdLatt.innerHTML = wsLatt[n];
    tdLong.innerHTML = wsLong[n];
  }
</script>
<?php
  } else if ($mycat == 5 && $isLoggedIn) {
?>
<script language="javascript">
  function updateWSType(type) {
    if (type == "custom") {
      document.uploadweather.col_date.value="";
      document.uploadweather.col_time.value="";
      document.uploadweather.col_minTemp.value="";
      document.uploadweather.col_maxTemp.value="";
      document.uploadweather.col_solarRad.value="";
      document.uploadweather.col_rain.value="";
      document.uploadweather.delimiter[0].click();
      document.uploadweather.units[0].click();
      if (document.uploadweather.lines_auto.checked) document.uploadweather.lines_auto.click();
    } else if (type == "davis") {
      document.uploadweather.col_date.value="1";
      document.uploadweather.col_time.value="2";
      document.uploadweather.col_minTemp.value="5";
      document.uploadweather.col_maxTemp.value="4";
      document.uploadweather.col_solarRad.value="20";
      document.uploadweather.col_rain.value="18";
      document.uploadweather.delimiter[0].click();
      document.uploadweather.units[0].click();
      if (! document.uploadweather.lines_auto.checked) document.uploadweather.lines_auto.click();
    }
  }
</script>
<?php
  }
?>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<?php
   if (!$isLoggedIn && !$doPreview) {
      $mycat = 0;
      $mode = '';
   }
   echo '<h2><center>My Account</center></h2>';
   echo '<center>';
   if ($mycat != 1) echo '<a href="myaccount.php?cat=1" class="myaccount">Change My Password</a> | '; else echo 'Change My Password | ';
   if ($mycat != 2) echo '<a href="myaccount.php?cat=2" class="myaccount">Update My Info</a> | '; else echo 'Update My Info | ';
   if ($mycat != 3) echo '<a href="myaccount.php?cat=3" class="myaccount">My Weather Stations</a> | '; else echo 'My Weather Stations | ';
   if ($mycat != 4) echo '<a href="myaccount.php?cat=4" class="myaccount">My Zones</a> | '; else echo 'My Zones | ';
   if ($mycat != 5) echo '<a href="myaccount.php?cat=5" class="myaccount">Upload Weather Data</a>'; else echo 'Upload Weather Data';
   echo '</center><br>';
?>

<?php 
   if ($mode == 'changepass' && $isLoggedIn) {
      if ($_POST['passwd1'] == $_POST['passwd2']) {
	$oldpass = sha1($_POST['oldpass']);
	$newpass = sha1($_POST['passwd1']);
	$sql = "select username, password from users where uid = " . $_SESSION['bmpid'];
	$out = $mysqli_db->query($sql);
	if ($out != false) {
           if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	      $dbuser = $row['username'];
	      $dbpass = $row['password'];
	      if ($dbuser == $bmpusername && $dbpass == $oldpass) {
	        $sql = "update users set password = '" . $newpass . "' where uid = " . $_SESSION['bmpid'];
	        $out = $mysqli_db->query($sql);
	        if ($out != false) {
		   echo '<center>Password successfully updated!</center>';
	        } else {
		   echo '<center><span class="regreq">Failed to change password!</span></center>';
		}
	      } else {
		echo '<center><span class="regreq">Failed to change password!</span></center>';
	      }
           }
        }
      }
   } else if ($mode == 'updateinfo' && $isLoggedIn) {
      $role = 0;
      $role_other = NULL;
      for ($j=0; $j < count($_POST['role']); $j++) {
	if ($_POST['role'][$j] != 'other') {
          $role += floor($_POST['role'][$j]);
        } else $role_other = $_POST['role_other'];
      }
      $sql = "update users set firstName = '" . $_POST['first'] . "', ";
      $sql .= "lastName = '" . $_POST['last'] . "', ";
      $sql .= "affiliation = '" . $_POST['affiliation'] . "', ";
      $sql .= "role = " . $role . ", ";
      $sql .= "role_other = '" . $role_other . "', ";
      $sql .= "state = '" . $_POST['state'] . "', ";
      $sql .= "country = '" . $_POST['country'] . "', ";
      $sql .= "email = '" . $_POST['email'] . "', ";
      $birthday = strtotime($_POST['day'] . ' ' . $_POST['month'] . ' ' . $_POST['year']);
      $sql .= "birthday = " . $birthday;
      $sql .= " where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      if ($out != false) {
	echo '<center>Information successfully updated!</center>';
      } else {
	echo '<center><span class="regreq">Failed to update information!</span></center>';
      }
  } else if ($mode == 'weather' && $isLoggedIn) {
    foreach ($_POST as $key=>$value) {
      //delete params
      if (substr($key, 0, 4) == 'del-' && $value == 'on') {
        $id = substr($key, 4);
        $sql = 'delete from weatherStations where location="' . $id . '" AND uid=' . $_SESSION['bmpid'];
      } else if (strpos($key, 'wsLocation_') !== false) {
        $wsid = substr($key, strrpos($key, '_')+1);
        $sql = "update weatherStations set location='$value' where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
      } else if (strpos($key, 'elev_') !== false) {
        $wsid = substr($key, strrpos($key, '_')+1);
        $sql = "update weatherStations set elevation_ft=$value where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
      } else if (strpos($key, 'lattitude_') !== false) {
        $wsid = substr($key, strrpos($key, '_')+1);
        $sql = "update weatherStations set lattitude=$value where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
      } else if (strpos($key, 'longitude_') !== false) {
        $wsid = substr($key, strrpos($key, '_')+1);
        $sql = "update weatherStations set longitude=$value where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
      } else if (strpos($key, 'wsTime_') !== false) {
        $wsid = substr($key, strrpos($key, '_')+1);
        $sql = "update weatherStations set weatherTime='$value' where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
        if (!isset($_POST['public-' . $wsid])) {
	  #Checkboxes aren't submitted if not checked.  If we get a wsTime but no public, it is unchecked.
	  $sql2 = "update weatherStations set public=false where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";"; 
	  $out = $mysqli_db->query($sql2); 
	}
      } else if (strpos($key, 'public_') !== false) {
	$wsid = substr($key, strrpos($key, '_')+1);
	$sql = "update weatherStations set public=true where wsid=$wsid and uid=" . $_SESSION['bmpid'] . ";";
      }
      $out = $mysqli_db->query($sql);
    }
    $wsLocation = trim($_POST['wsLocation']);
    $elev = trim($_POST['elev']);
    $lattitude = trim($_POST['lattitude']);
    $longitude = trim($_POST['longitude']);
    $wsTime = trim($_POST['weatherTime']);
    if ($wsLocation != '' && $elev != '' && $lattitude != '' && $longitude != '') {
      #Add new weather station 
      $sql = "insert into weatherStations(uid, location, elevation_ft, lattitude, longitude, weatherTime) values (" . $_SESSION['bmpid'] . ", '$wsLocation', $elev, $lattitude, $longitude, '$wsTime');";
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo '<center><span class="regreq">Failed to add weather station!</span></center>';
      }
    }
    $mycat = 3;
   } else if ($mode == 'uploadweather' && $isLoggedIn) {
      $wth = file($_FILES['weatherfile']['tmp_name']);
      $wsid = $_POST['weatherStation'];

      if (isset($_POST['lines_auto'])) {
	#Auto-detect # of lines
	$lskip = 0;
	$foundNum = false;
	while (!$foundNum) {
	  if (is_numeric(substr(trim($wth[$lskip]), 0, 1))) {
	    $foundNum = true;
	  } else $lskip++;
	}
      } else {
	$lskip = intVal($_POST['lines']);
      }

      $metric = false;
      if ($_POST['units'] == "metric") $metric = true;

      #Delimiter
      if ($_POST['delimiter'] == "space") {
	$delim = '/\s+/';
      } else if ($_POST['delimiter'] == "comma") {
	$delim = ',';
      } else if ($_POST['delimiter'] == "tab") {
	$delim = '/\t/';
      } else if ($_POST['delimiter'] == "other") {
	$delim = $_POST['delimiter-oth'];
      }

      $cols = Array();
      $headers = preg_split($delim, trim($wth[0]));
      $params = Array("date", "time", "minTemp", "maxTemp", "solarRad", "rain");
      for ($j = 0; $j < count($params); $j++) {
	if (isset($_POST['col_' . $params[$j]])) {
	  if (is_numeric($_POST['col_' . $params[$j]])) {
	    $cols[$j] = intVal($_POST['col_' . $params[$j]])-1;
	  } else {
	    $cols[$j] = array_search($_POST['col_' . $params[$j]], $headers); 
	  }
	}
      }

      $nupdate = 0;
      $ninsert = 0;
      for ($j = $lskip; $j < count($wth); $j++) {
	$currLine = preg_split($delim, trim($wth[$j]));
	#Parse date
	$d = $currLine[$cols[0]];
	if ($cols[1] !== false) $d .= " " . $currLine[$cols[1]];
	if (strtolower(substr($d, -1)) == "a" || strtolower(substr($d, -1)) == "p") $d .= "m";
	$t = strtotime($d);
	#Convert to sql format
	$hour = date("Y-m-d H:i:s", $t);
	#Parse weather values
        $solarRad = floatVal($currLine[$cols[4]]);
	$maxTemp = floatVal($currLine[$cols[3]]);
	$minTemp = floatVal($currLine[$cols[2]]);
	$rain = floatVal($currLine[$cols[5]]);
	if ($metric) {
	  #Convert to deg F and inches
	  $maxTemp = $maxTemp*1.8+32;
	  $minTemp = $minTemp*1.8+32;
	  $rain = $rain/2.54;
	}

	$update = false;
	$sql = "select * from hourlyWeather where wsid=$wsid AND uid=" . $_SESSION['bmpid'] . " AND hour='$hour';";
        $out = $mysqli_db->query($sql);
        if ($out != false) {
          if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	    $update = true;
	    $hwid = $row['hwid'];
	  }
	}

	if ($update) {
	  $sql = "update hourlyWeather set solar_radiation=$solarRad, max_temp=$maxTemp, min_temp=$minTemp, rain_in=$rain where hwid=$hwid;";
	} else {
	  $sql = "insert into hourlyWeather(wsid, uid, hour, solar_radiation, max_temp, min_temp, rain_in) values ($wsid, " . $_SESSION['bmpid'] . ", '$hour', $solarRad, $maxTemp, $minTemp, $rain);"; 
	}
	#echo $sql . "<br>";
	$out = $mysqli_db->query($sql);
	if ($out != false) {
	  if ($update) $nupdate++; else $ninsert++;
	}
      }
      echo "Skipped $lskip header lines.  Inserted $ninsert new weather datapoints and updated $nupdate datapoints.";
   } 
   if ($mycat == 1 && $isLoggedIn) { ?>
<form name="changepass" action="myaccount.php" method="post" onSubmit='return checkPass();'>
<input type="hidden" name="mode" value="changepass">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr><td class="regreq" align="right" valign="top">
Old Password:
</td>
<td>
<input type="password" size=15 name="oldpass">
</td>
</tr>
<tr><td class="regreq" align="right" valign="top">
New Password:
</td>
<td>
<input type="password" size=15 name="passwd1">
<br><span class="small">Must be at least 5 characters.</span>
</td>
</tr>
<tr><td class="regreq" align="right">
Re-type New Password:
</td>
<td>
<input type="password" size=15 name="passwd2">
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Change Password">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
</div>
</form>
<?php } else if ($mycat == 2 && $isLoggedIn) { 
      $sql = "select * from users where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      if ($out != false) {
	if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	   $info = $row;
	}
      }
?>
<form name="changeinfo" action="myaccount.php" method="post" onSubmit='return checkInfo();'>
<input type="hidden" name="mode" value="updateinfo">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr><td valign="top" align="right" class="regreq">
First Name:
</td>
<td valign="top" align="left">
<input type="text" size=15 name="first" value="<?php echo $info['firstName'];?>">
</td>
</tr>
<tr><td class="regreq" align="right">
Last Name:
</td>
<td>
<input type="text" size=15 name="last" value="<?php echo $info['lastName'];?>">
</td>
</tr>

<tr><td class="regreq" align="right">
Affiliation:
</td>
<td>
<input type="text" size=15 maxlength=32 name="affiliation" value="<?php echo $info['affiliation'];?>">
</td>
</tr>

<tr><td class="regreq" align="right">
Affiliation Role<br>(select all that apply):
</td>
<td>
<?php
  $roleSelected = Array();
  $roleTot = $info['role'];
  for ($j = count($affiliations)-2; $j >= 0; $j--) {
    if ($roleTot >= pow(2, $j)) {
      $roleSelected[$j] = True;
      $roleTot -= pow(2, $j);
    } else $roleSelected[$j] = False;
  }
  for ($j = 0; $j < count($affiliations)-1; $j++) {
    echo '<input type="checkbox" name="role[]" id="role' . $j . '" value=' . pow(2, $j);
    if ($roleSelected[$j]) echo ' checked';
    echo '>' . $affiliations[$j] . "<br>\n";
  }
  echo '<input type="hidden" name="nroles" value=' . count($affiliations) . '>';
?>
<input type="checkbox" name="role[]" id="roleO" value="other" onClick="setOtherRole(this.checked);" <?php if ($info['role_other'] != "NULL" and $info['role_other'] != NULL) echo ' checked'; ?>>Other (specify)
<input type="text" size=15 name="role_other" <?php if ($info['role_other'] == "NULL" or $info['role_other'] == NULL) echo ' disabled'; else echo 'value="' . $info['role_other'] . '"'; ?>><br>
</td>
</tr>

<tr><td class="regreq" align="right">
State
</td>
<td>
<?php require('stateSelect.php'); ?>
<br>
<span class="small">Select "None" if you are not in the USA.</span>
</td>
</tr>

<tr><td class="regreq" align="right">
Country
</td>
<td>
<?php require('countrySelect.php'); ?>
</td>
</tr>

<?php
  echo "<script language=\"javascript\">updateChangeForm('{$info['state']}', '{$info['country']}');</script>";
?>

<tr><td class="regreq" align="right">
E-mail address:
</td>
<td>
<input type="text" size=25 name="email" value="<?php echo $info['email'];?>">
</td>
</tr>

<tr><td class="regreq" align="right" valign="top">
Birthday:
</td>
<td>
<select name="month"><option name="select" value="select">[Select]
<?php
   $bmonth = date("F",$info['birthday']);
   $bday = date("d",$info['birthday']);
   $byear = date("Y", $info['birthday']);
   $months = array("Jan","Feb","Mar","Apr","May","June","July","Aug","Sept","Oct","Nov","Dec");
   for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value="' . $months[$j] . '"';
      if (strpos($bmonth, $months[$j]) !== false) echo ' selected';
      echo '>' . $months[$j];
   }
?>
</select>
<input type="text" name="day" size=2 value="<?php echo $bday; ?>">
<input type="text" name="year" size=4 value="<?php echo $byear; ?>">
<br><span class="small">In case you forget your password.</span>
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Update">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
</div>
</form>
<?php } else if ($mycat == 3) {
      $info = Array();
      if ($isLoggedIn) $uid = $_SESSION['bmpid']; else if ($doPreview) $uid=39;
      $sql = "select * from weatherStations where (uid = $uid or public=1) ORDER BY wsid";
      $out = $mysqli_db->query($sql);
      $n = 0;
      if ($out != false) {
        while ($row = $out->fetch_array(MYSQLI_BOTH)) {
           $info[$n] = $row;
	   $n++;
        }
      }
      echo '<script language="javascript">';
      echo 'dbNames = Array();';
      for ($j = 0; $j < count($info); $j++) {
        echo 'dbNames[' . $j . '] = "' . $info[$j]['location'] . '";';
      }
      echo '</script>';
?>
      <form name="manageWS" action="myaccount.php" method="post" onSubmit="return checkWS();">
      <input type="hidden" name="mode" value="weather">
      <div style="margin-left: 0px;" align="center">
      <table border=1 align="center" class="manage">
      <tr>
      <th valign="top" class="manageth-nobg">ID</th>
      <th valign="top" class="manageth">Name/<br>Location</th>
      <th valign="top" class="manageth">Elev (ft)</th>
      <th valign="top" class="manageth">Latt</th>
      <th valign="top" class="manageth">Long</th>
      <th valign="top" class="manageth">Default<br>Cirrig</th>
      <th valign="top" class="manageth">Public</th>
      <th valign="top" class="manageth">View</th>
      <th valign="top" class="manageth">Edit</th>
      <th valign="top" class="manageth">Delete</th>
      </tr>
<?php
      for ($j = 0; $j < $n; $j++) {
	if ($j%2 == 0) $td = '<td class="managetd"'; else $td = '<td class="managetdalt"';
	echo '<tr>';
	echo $td . '>' . $info[$j]['wsid'] . '</td>';
        echo $td . ' id="tdLoc' . $info[$j]['wsid'] . '">' . $info[$j]['location'] . '</td>';
        echo $td . ' id="tdElev' . $info[$j]['wsid'] . '">' . $info[$j]['elevation_ft'] . '</td>';
        echo $td . ' id="tdLatt' . $info[$j]['wsid'] . '">' . $info[$j]['lattitude'] . '</td>';
        echo $td . ' id="tdLong' . $info[$j]['wsid'] . '">' . $info[$j]['longitude'] . '</td>';
        echo $td . ' id="tdTime' . $info[$j]['wsid'] . '">';
	//Default cirrig time select
	echo '<select name="wsTime_' . $info[$j]['wsid'] . '">';
        echo '<option name="live" value="live">Live';
        echo '<option name="auto" value="auto"';
        if ($info[$j]['weatherTime'] == 'auto') echo ' selected';
	echo '>Auto';
	for ($h = 0; $h < 24; $h++) {
	  if ($h%12 == 0) {
	    $time = "12:00";
	  } else {
	    $time = (string)($h%12) . ":00";
	  }
	  if ($h < 12) $time .= " AM"; else $time .= " PM";
	  echo '<option name="' . $h . '" value="' . $h . '"';
	  if ($info[$j]['weatherTime'] == (string)$h) echo ' selected';
	  echo '>' . $time;
        }
	echo '</td>';
	echo $td . '><input type="checkbox" name="public_' . $info[$j]['wsid'] . '"';
        if ($info[$j]['public'] == 1) echo ' checked';
        echo '></td>';
        echo $td . ' id="tdView' . $info[$j]['wsid'] . '">[<a href="viewWeather.php?wsid=' . $info[$j]['wsid'] . '">view</a>]</td>';
	if ($isLoggedIn && $uid == $info[$j]['uid']) echo $td . ' id="tdEdit' . $info[$j]['wsid'] . '">[<a href="javascript:editWS(' . $info[$j]['wsid'] . ');">edit</a>]</td>'; else echo $td . '>[edit]</td>';
	if ($isLoggedIn && $uid == $info[$j]['uid']) echo $td . '><input type="checkbox" name="del-' . $info[$j]['location'] . '"></td>'; else echo $td . '></td>';
	echo '</tr>';
      }
?>
      <tr>
      <td></td>
      <td><input type="text" name="wsLocation" size=12></td>
      <td><input type="text" name="elev" size=6 onkeypress="return validDec(event);"></td>
      <td><input type="text" name="lattitude" size=6 onkeypress="return validDec(event);"></td>
      <td><input type="text" name="longitude" size=6 onkeypress="return validDec(event);"></td>
      <td><select name="weatherTime">
	<option name="live" value="live">Live
	<option name="auto" value="auto">Auto
	<?php
	  for ($h = 0; $h < 24; $h++) {
	    if ($h%12 == 0) {
	      $time = "12:00";
	    } else {
	      $time = (string)($h%12) . ":00";
	    }
	    if ($h < 12) $time .= " AM"; else $time .= " PM";
	    echo '<option name="' . $h . '" value="' . $h . '">' . $time;
	  }
	?>
      </select></td>
      <td colspan=3 onMouseOver="ddrivetip('Enter a new weather station on this row', '99ffaa', 250);" onMouseOut="hideddrivetip();">New Station</td>
      </tr>
      </table>
      <br>
<?php if ($isLoggedIn) { ?>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
<?php } ?>
      </div>
      </form>
<?php } else if ($mycat == 5 && $isLoggedIn) {
      $info = Array();
      $sql = "select * from weatherStations where uid = " . $_SESSION['bmpid'] . " ORDER BY wsid";
      $out = $mysqli_db->query($sql);
      $n = 0;
      if ($out != false) {
        while ($row = $out->fetch_array(MYSQLI_BOTH)) {
           $info[$n] = $row;
           $n++;
        }
      }
?>
      <form name="uploadweather" action="myaccount.php" method="post" enctype="multipart/form-data" onSubmit="return checkUploaded();">
      <input type="hidden" name="mode" value="uploadweather">
      <input type="hidden" name="MAX_FILE_SIZE" value="1000000">
      <div style="margin-left: 100px;">
      <center><b>Upload weather data</b></center><br>
      <table border=0>
      <tr>
        <td valign="top">Weather Station</td>
        <td><select name="weatherStation">
<?php
        for ($j = 0; $j < $n; $j++) {
	  echo '<option name="' . $info[$j]['wsid'] . '" value="' . $info[$j]['wsid'] . '">' . $info[$j]['location'];
        }
?>
        </select>
        </td>
      </tr>

      <tr>
        <td valign="top">Weather Station Type</td>
        <td><select name="weatherStationType" onChange="updateWSType(this.options[this.selectedIndex].value);">
	  <option name="custom" value="custom">Custom
          <option name="davis" value="davis">Davis
        </select>
        </td>
      </tr>

      <tr>
	<td># of header lines to skip</td>
        <td><input type="text" name="lines" size=2 onkeypress="return validNum(event);"> Auto-detect <input type="checkbox" name="lines_auto" value="lines_auto" onClick="document.uploadweather.lines.disabled=this.checked;">
      </tr>

      <tr>
	<td>Delimiter</td>
	<td>
	  <input type="radio" name="delimiter" value="space">Whitespace
          <input type="radio" name="delimiter" value="comma">Comma
          <input type="radio" name="delimiter" value="tab">Tab
          <input type="radio" name="delimiter" value="other">Other: <input type="text" name="delimiter-oth" size=4>
      </tr>

      <tr>
        <td>Units</td>
        <td>
          <input type="radio" name="units" value="english" checked>English 
          <input type="radio" name="units" value="metric">Metric
      </tr>

      <tr>
	<td valign="top" onMouseOver="ddrivetip('Enter either a column number (starting with 1) or a column header value (such as MinTemp) for each column.','99ffaa',450);" onMouseOut="hideddrivetip();"><b>Column numbers<br>or headers</b></td>
	<td>
          Date <input type="text" name="col_date" size=6><br>
          Time <input type="text" name="col_time" size=6><br>
	  Min Temp <input type="text" name="col_minTemp" size=6><br>
	  Max Temp <input type="text" name="col_maxTemp" size=6><br>
	  Solar Rad <input type="text" name="col_solarRad" size=6><br>
	  Rainfall <input type="text" name="col_rain" size=6><br>
	</td>
      </tr>

      <tr><td>File to Upload</td>
      <td>
      <input type="file" name="weatherfile">
      <input type="submit" name="Upload" value="Upload">
      </td></tr>

      </table>
      </div>
      </form>
<?php } else if ($mycat == 4) {
	require('myzones.php');
      } ?>
	<!-- End body here -->
<?php require('footer.php'); ?>
