<?php session_start(); ?>
<html>
<head>
<script language="javascript">
  function setOtherRole(isChecked) {
    document.changeinfo.role_other.disabled= !isChecked;
  }

  function updateChangeForm(state, country) {
    for (j = 0; j < document.changeinfo.state.options.length; j++) {
      if (document.changeinfo.state.options[j].value == state) document.changeinfo.state.selectedIndex = j; 
    }
    for (j = 0; j < document.changeinfo.country.options.length; j++) {
      if (document.changeinfo.country.options[j].value == country) document.changeinfo.country.selectedIndex = j;
    }
  }

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
    if (ext == ".plt") validExt = true;
    if (ext == ".sfn") validExt = true;
    if (ext == ".irr") validExt = true;
    if (!validExt) {
      alert('Invalid file type!');
      return false;
    }
    var retVal = true;
    for (j = 0; j < uploadedFiles.length; j++) {
      if (uploadedFiles[j] == document.uploadfiles.userfile.value) {
        retVal = confirm("A file already exists with the name "+uploadedFiles[j]+".  Are you sure you want to overwrite it?");
      }
    }
    return retVal;
  }

  function checkDefaults(type, name) {
    for (j = 0; j < document.manageruns.elements.length; j++) {
      if (document.manageruns.elements[j].name.indexOf("def-"+type) == 0 && document.manageruns.elements[j].name != "def-"+type+"-"+name) {
	document.manageruns.elements[j].checked = false;
      }
    }
  }
</script>
<?php
   if (isset($_GET['cat'])) {
      $mycat = $_GET['cat'];
      if ($mycat < 1 or $mycat > 7) $mycat = 0;
   } else {
      $mycat = 0;
   }
   $mode = '';
   if (isset($_POST['mode'])) {
      $mode = $_POST['mode'];
   }
   if ($mode == "createcost" || $mode == "editcost" || $mode == "costs") $mycat = 7;
   if ($mycat == 1) {
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
   } else if ($mycat == 2) {
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
   } else if ($mycat == 4) {
?>
<script language="javascript">
  function changeSize(w, h) {
    document.prefs.pw.value = w;
    document.prefs.ph.value = h;
    changePlot();
  }

  function changePlot() {
    w = document.prefs.pw.value;
    h = document.prefs.ph.value;
    bg = document.prefs.plotbg.options[document.prefs.plotbg.selectedIndex].value;
    fg = document.prefs.plotfg.options[document.prefs.plotfg.selectedIndex].value;
    graph = document.getElementById("prefsgraph");
    graph.src = "graph.php?pw="+w+"&ph="+h+"&bg="+bg+"&fg="+fg;
  }
</script>
<?php
   }
?>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<?php
   if (!$isLoggedIn) {
      $mycat = 0;
      $mode = '';
   }
   echo '<h2><center>My Account</center></h2>';
   echo '<center>';
   if ($mycat != 1) echo '<a href="myaccount.php?cat=1" class="myaccount">Change My Password</a> | '; else echo 'Change My Password | ';
   if ($mycat != 2) echo '<a href="myaccount.php?cat=2" class="myaccount">Update My Info</a> | '; else echo 'Update My Info | ';
   if ($mycat != 3) echo '<a href="myaccount.php?cat=3" class="myaccount">Manage Saved Runs</a> | '; else echo 'Manage Saved Runs | ';
   if ($mycat != 4) echo '<a href="myaccount.php?cat=4" class="myaccount">My Preferences</a> | '; else echo 'My Preferences | ';
   if ($mycat != 5) echo '<a href="myaccount.php?cat=5" class="myaccount">Upload / Manage Input Files</a> | '; else echo 'Upload / Manage Input Files | ';
   if ($mycat != 6) echo '<a href="myaccount.php?cat=6" class="myaccount">Automate Runs | </a>'; else echo 'Automate Runs | ';
   if ($mycat != 7) echo '<a href="myaccount.php?cat=7" class="myaccount">My Costs</a>'; else echo 'My Costs';
   echo '</center><br>';
?>

<?php 
   if ($mode == 'changepass') {
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
   } else if ($mode == 'updateinfo') {
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
   } else if ($mode == 'manageruns') {
      $pfix = "data/" . $bmpusername . "/";
      $sql = "update runs set defFile=0 where uid=" . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      foreach ($_POST as $key=>$value) {
	//delete files
	if (substr($key, 0, 4) == 'del-' && $value == 'on') {
	  $id = substr($key, 4);
          system('rm ' . $pfix . '*' . $id . '*');
	  $sql = 'delete from runs where name="' . $id . '" AND uid=' . $_SESSION['bmpid'];
	  $out = $mysqli_db->query($sql);
	} else if (substr($key, 0, 4) == "def-" && $value == 'on') {
	  $name = substr($key, strpos($key, '-', 4)+1);
	  //update default
	  $sql = "update runs set defFile=1 where name='$name' AND uid = " . $_SESSION['bmpid'];
	  $out = $mysqli_db->query($sql);
	}
      }
      $mycat = 3;
   } else if ($mode == 'updateprefs' && $isLoggedIn) {
      $sql = "select count(*) from prefs where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      $update = false;
      if ($out != false) {
	if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	  if ($row[0] > 0) $update = true;
	}
      }
      if ($_POST['res'] == "hires") {
	$res = "TRUE";
	echo '<script language="javascript">switchRes(0);</script>';
      } else {
	$res="FALSE";
        echo '<script language="javascript">switchRes(1);</script>';
      }
      if ($update) {
	$sql = "update prefs set hires=" . $res . ", plotWidth = ";
	$sql .= $_POST['pw'] . ", plotHeight = " . $_POST['ph'];
	$sql .= ", plotbg='" . $_POST['plotbg'] . "', plotfg='";
	$sql .= $_POST['plotfg'] . "' where uid = " . $_SESSION['bmpid']; 
      } else {
	$sql = "insert into prefs (uid, hires, plotWidth, plotHeight,";
	$sql .= " plotbg, plotfg) values ( " . $_SESSION['bmpid'] . ", ";
	$sql .= $res . ", " . $_POST['pw'] . ", " . $_POST['ph'] . ", '";
	$sql .= $_POST['plotbg'] . "', '" . $_POST['plotfg'] . "')";
      }
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        echo '<center>Preferences successfully updated!</center>';
      } else {
        echo '<center><span class="regreq">Failed to update preferences!</span></center>';
      }
   } else if ($mode == 'managefiles' && $isLoggedIn) {
      $pfix = "data/" . $bmpusername . "/";
      foreach ($_POST as $key=>$value) {
        //delete files
        if (substr($key, 0, 4) == 'del-' && $value == 'on') {
          $id = substr($key, 4);
          system('rm ' . $pfix . '*' . $id . '*');
          $sql = 'delete from files where name="' . $id . '" AND uid=' . $_SESSION['bmpid'];
          $out = $mysqli_db->query($sql);
        }
      }
      $mycat = 5;
      if (isset($_FILES['userfile']) && isset($_POST['filetype'])) {
	$isValid = true;
	if ($_POST['filetype'] == 'wth') {
	  if (!isWthFile($_FILES['userfile']['tmp_name'])) {
	    echo '<center><span class="regreq">Invalid weather file!</span></center>';
	    $isValid = false;
	  }
	} else if ($_POST['filetype'] == 'plt') {
          if (!isPltFile($_FILES['userfile']['tmp_name'])) {
            echo '<center><span class="regreq">Invalid plant spec file!</span></center>';
            $isValid = false;
          }
        } else if ($_POST['filetype'] == 'sfn') {
          if (!isSfnFile($_FILES['userfile']['tmp_name'])) {
            echo '<center><span class="regreq">Invalid solution feed file!</span></center>';
            $isValid = false;
          }
        } else if ($_POST['filetype'] == 'irr') {
          if (!isIrrFile($_FILES['userfile']['tmp_name'])) {
            echo '<center><span class="regreq">Invalid irrigation file!</span></center>';
            $isValid = false;
          }
        } else {
          echo '<center><span class="regreq">Invalid file!</span></center>';
          $isValid = false;
	}
	if ($isValid) {
	  $fname = basename($_FILES['userfile']['name']);
	  $sql = "select fid from files where uid = " . $_SESSION['bmpid'] . " and name = '" . $fname . "'";
	  $update = false;
	  $out = $mysqli_db->query($sql);
	  if ($out != false) {
	    if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	      $update = true;
	      $fid = $row[0];
	    }
	  }
	  if ($update) {
	    system('rm ' . $pfix . $fname);
	    $sql = "files runs set ";
	    $sql .= "type = '" . $_POST['filetype'] . "'";
	    $sql .= " where fid=" . $fid;
	  } else {
	    $sql = "insert into files(uid, name, type)";
	    $sql .= " values (" . $_SESSION['bmpid'] . ", '";
	    $sql .= $fname . "', '";
	    $sql .= $_POST['filetype'] . "')";
	  }
	  $out = $mysqli_db->query($sql);
	  if ($out == false) {
	    echo 'Failed to update database!';
	  } else {
	    $uploadfile = $pfix . $fname; 
	    if (move_uploaded_file($_FILES['userfile']['tmp_name'], $uploadfile)) {
	      echo '<center><span class="regreq">Upload successful!</span></center>';
	    } else {
	      echo '<center><span class="regreq">Upload failed!</span></center>'; 
	    }
	  }
	}
	echo '<br>';
      }
   } else if ($mode == 'automate' && $isLoggedIn) {
      $pfix = "data/" . $bmpusername . "/";
      foreach ($_POST as $key=>$value) {
        if (substr($key, 0, 5) == 'auto-') {
          $id = substr($key, 5);
          if ($value == "on") {
            $sql = "update runs set auto=1 where rid=$id AND uid = " . $_SESSION['bmpid'];
            $out = $mysqli_db->query($sql);
          }
          $hour = floor($_POST['autoHour-' . $id]);
          $minute = floor($_POST['autoMin-' . $id]);
          $second = 0;
          if ($hour == 12) $hour = 0;
          if ($_POST['autoAmPm-' . $id] == "pm") $hour += 12;
          $sql = "update runs set autoRunTime=MAKETIME($hour, $minute, $second) where rid=$id AND uid=" . $_SESSION['bmpid'];
          $out = $mysqli_db->query($sql);
        } else if (substr($key, 0, 9) == "autoHour-") {
          $id = substr($key, 9);
          if (!isset($_POST['auto-' . $id])) {
            $sql = "update runs set auto=0 where rid=$id AND uid = " . $_SESSION['bmpid'];
            $out = $mysqli_db->query($sql);
            $hour = floor($_POST['autoHour-' . $id]);
            $minute = floor($_POST['autoMin-' . $id]);
            $second = 0;
            if ($hour == 12) $hour = 0;
            if ($_POST['autoAmPm-' . $id] == "pm") $hour += 12;
            $sql = "update runs set autoRunTime=MAKETIME($hour, $minute, $second) where rid=$id AND uid=" . $_SESSION['bmpid'];
            $out = $mysqli_db->query($sql);
          }
        }
      }
      $mycat = 6;
   }
   if ($mycat == 1) { ?>
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
<?php } else if ($mycat == 2) { 
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
      $sql = "select * from runs where uid = " . $_SESSION['bmpid'] . " ORDER BY rid";
      $out = $mysqli_db->query($sql);
      $n = 0;
      if ($out != false) {
        while ($row = $out->fetch_array(MYSQLI_BOTH)) {
           $info[$n] = $row;
	   $n++;
        }
      }
?>
      <form name="manageruns" action="myaccount.php" method="post">
      <input type="hidden" name="mode" value="manageruns">
      <div style="margin-left: 0px;" align="center">
      <table border=1 align="center" class="manage">
      <tr>
      <th valign="top" class="manageth-nobg">ID</th>
      <th valign="top" class="manageth">Run Name</th>
      <th valign="top" class="manageth">Type</th>
      <th valign="top" class="manageth">Edit</th>
      <th valign="top" class="manageth">Delete</th>
      <th valign="top" class="manageth">Default</th>
      </tr>
<?php
      $pages['grower'] = 'driver_grower.php';
      $pages['fert'] = 'comparison_fert.php';
      $pages['irr'] = 'comparison_irr.php';
      $pages['fert_irr'] = 'comparison_fert_irr';
      $pages['location'] = 'comparison_location.php';
      $pages['date'] = 'comparison_date.php';
      $pages['technical'] = 'driver_tech.php';
      $pages['realtime'] = 'realtime.php';
      for ($j = 0; $j < $n; $j++) {
	if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
	echo '<tr>';
	echo $td . $info[$j]['rid'] . '</td>';
        echo $td . '<a href="' . $pages[$info[$j]['type']] . '?runid=' . $info[$j]['name'] . '">' . $info[$j]['name'] . '</a></td>';
        echo $td . $info[$j]['type'] . '</td>';
	echo $td . '[<a href="' . $pages[$info[$j]['type']] . '?editid=' . $info[$j]['rid'] . '">edit</a>]</td>';
	echo $td . '<input type="checkbox" name="del-' . $info[$j]['name'] . '"></td>';
	echo $td . '<input type="checkbox" name="def-' . $info[$j]['type'] . '-' . $info[$j]['name'] . '"';
	echo ' onClick="checkDefaults(\'' . $info[$j]['type'] . '\', \'' . $info[$j]['name'] . '\');"';
	if ($info[$j]['defFile'] == 1) echo ' checked';
	echo '></td>'; 
	echo '</tr>';
      }
?>
      </table>
      <br>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
      </div>
      </form>
<?php } else if ($mycat == 4) {
      $plotWidth = 320;
      $plotHeight = 240;
      $hires = false;
      $plotbg = "white";
      $plotfg = "black";
      $sql = "select * from prefs where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      $n = 0;
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	  $plotWidth = $row['plotWidth'];
	  $plotHeight = $row['plotHeight'];
	  $plotbg = $row['plotbg'];
	  $plotfg = $row['plotfg'];
	  $hires = $row['hires'];
        }
      }
?>
      <form name="prefs" action="myaccount.php" method="post">
      <input type="hidden" name="mode" value="updateprefs">
      <input type="hidden" name="pw" value="<?php echo $plotWidth;?>">
      <input type="hidden" name="ph" value="<?php echo $plotHeight;?>">
      <div style="margin-left: 100px;">
	<table border=0 align="left">
	  <tr><td valign="top" align="left">Screen resolution:</td>
	  <td valign="top" align="left">
	    <input type="radio" name="res" value="hires" onClick="switchRes(0);" <?php if ($hires) echo 'checked'; ?>>Hi-res
	    <input type="radio" name="res" value="lores" onClick="switchRes(1);" <?php if (!$hires) echo 'checked'; ?>>Lo-res
	    <script language="javascript">
	      var header = document.getElementById("header");
	      if (header.src.indexOf("top_banner_bmp_hires.png") != -1) document.prefs.res[0].checked=true; else document.prefs.res[1].checked=true;
	    </script>
	    <br><br>
	  </td>
	  </tr>
	  <tr><td></td><td><center>
	  <?php
	    echo '<img id="prefsgraph" src="graph.php?pw=' . $plotWidth . '&ph=' . $plotHeight . '&bg=' . $plotbg . '&fg=' . $plotfg . '">';
	  ?>
	  </center><br></td></tr>
	  <tr><td valign="top" align="left">Plot Size:</td>
	  <td valign="top" align="left">
	    <ul>
            <input type="radio" name="plotSize" value="small" onClick="changeSize(240,180);" <?php if ($plotHeight == 180) echo 'checked'; ?>>Small<br>
            <input type="radio" name="plotSize" value="medium" onClick="changeSize(320,240)" <?php if ($plotHeight == 240) echo 'checked'; ?>>Medium<br>
            <input type="radio" name="plotSize" value="large" onClick="changeSize(400,300)" <?php if ($plotHeight == 300) echo 'checked'; ?>>Large<br>
            <input type="radio" name="plotSize" value="large" onClick="changeSize(456,342)" <?php if ($plotHeight == 342) echo 'checked'; ?>>Giant<br>
            <input type="radio" name="plotSize" value="wide-med" onClick="changeSize(320,200)" <?php if ($plotHeight == 200) echo 'checked'; ?>>Wide-Medium<br>
            <input type="radio" name="plotSize" value="wide-large" onClick="changeSize(400,250)" <?php if ($plotHeight == 250) echo 'checked'; ?>>Wide-Large<br>
            <input type="radio" name="plotSize" value="wide-large" onClick="changeSize(456,285)" <?php if ($plotHeight == 285) echo 'checked'; ?>>Wide-Giant<br>
	    </ul>
	  </td>
          </tr>
	  <?php
	    $colors = file("colors.txt");
	    $colorNames = Array();
	    $colorVals = Array();
	    for ($j = 0; $j < count($colors); $j++) {
	      $temp = explode("\t",$colors[$j]);
	      $colorVals[] = trim($temp[0]);
	      $colorNames[] = trim($temp[1]);
	    }
	  ?>
	  <tr><td valign="top" align="left">Plot Background Color:</td>
          <td valign="top" align="left">
	    <select name="plotbg" onChange="changePlot();">
	    <?php
	      for ($j = 0; $j < count($colorVals); $j++) {
		echo '<option name="' . $colorVals[$j] . '" value="' . $colorVals[$j] . '"';
		if ($plotbg == $colorVals[$j]) echo " selected";
		echo '>' . $colorNames[$j] . "\n";
	      }
	    ?>
	    </select>
          </td>
          </tr>
          <tr><td valign="top" align="left">Plot Foreground Color:</td>
          <td valign="top" align="left">
            <select name="plotfg" onChange="changePlot();">
            <?php
              for ($j = 0; $j < count($colorVals); $j++) {
                echo '<option name="' . $colorVals[$j] . '" value="' . $colorVals[$j] . '"';
                if ($plotfg == $colorVals[$j]) echo " selected";
                echo '>' . $colorNames[$j] . "\n";
              }
            ?>
            </select>
          </td>
          </tr>
	  <tr><td></td>
	  <td><br><br>
	    <input type="submit" name="submit" value="Update">
	    <input type="reset" name="reset" value="Reset">
	  </td></tr>
	</table>
      </div>
      </form>
<?php } else if ($mycat == 5) {
      $info = Array();
      $sql = "select * from files where uid = " . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      $nsaved = 0;
      if ($out != false) {
        while ($row = $out->fetch_array(MYSQLI_BOTH)) {
           $info[$nsaved] = $row;
           $nsaved++;
        }
      }
      echo '<script language="javascript">';
      for ($j = 0; $j < $nsaved; $j++) {
	echo 'uploadedFiles[' . $j . '] = "' . $info[$j]['name'] . '";';
      }
      echo '</script>';
?>
      <form name="managefiles" action="myaccount.php" method="post">
      <input type="hidden" name="mode" value="managefiles">
      <div style="margin-left: 0px;" align="center">
      <table border=1 align="center" class="manage">
      <tr>
      <th valign="top" class="manageth-nobg">ID</th>
      <th valign="top" class="manageth">File Name</th>
      <th valign="top" class="manageth">Type</th>
      <th valign="top" class="manageth">Delete</th>
      </tr>
<?php
      for ($j = 0; $j < $nsaved; $j++) {
        if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . $info[$j]['fid'] . '</td>';
        echo $td . '<a href="source/sourcecode.php?file=' . $info[$j]['name'] . '&type=user&cat=4">' . $info[$j]['name'] . '</a></td>';
        echo $td . $info[$j]['type'] . '</td>';
        echo $td . '<input type="checkbox" name="del-' . $info[$j]['name'] . '"></td>';
        echo '</tr>';
      }
?>
      </table>
      <br>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
      </form>
      <br><br>
<?php if ($nsaved < 25) { ?>
	<form name="uploadfiles" action="myaccount.php" method="post" enctype="multipart/form-data" onSubmit="return checkUploaded();">
	<input type="hidden" name="mode" value="managefiles">
        <input type="hidden" name="MAX_FILE_SIZE" value="1000000">
        <b>Upload a new </b>
        <select name="filetype" onChange="selectExampleDiv(this.options[selectedIndex].value+'example');">
        <option name="irr" value="irr">Irrigation File
        <option name="plt" value="plt">Plant Spec File
        <option name="sfn" value="sfn">Solution Feed File
        <option name="wth" value="wth" selected>Weather File
        </select>
        <input type="file" name="userfile">
        <input type="submit" name="Upload" value="Upload">
        </div>
        </form>
	<br><br>
	<div id="wthexample">
	  <b>Note:</b> Weather files must be formatted in a specific style,
	  shown below.  The first line must begin with *WEATHER.  The second
	  line must be blank.  The third line must contain the headers exactly
	  as displayed below.  Overwrite the fourth line with sensible values.
	  The only three that matter are lattitude, longitude, and elevation.
	  The fifth line again must contain the headers exactly as below and
	  the weather data must consist of five columns in the format
	  displayed below.  <span class="regreq">It is recommended that you
	  copy and paste the first five lines from below and then edit lines
	  1 and 4.</span>
	  <br><pre>
*WEATHER DATA : Dept. of Agronomy Forage Research Unit,Alachua

@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
   260   29.803  -82.410   38. -99.9 -99.9 -99.9 -99.9
@DATE  SRAD  TMAX  TMIN  RAIN
1999288   2.3  27.1  19.3  13.2
1999289   4.6  24.2  21.1   4.3
1999290  14.5  28.1  17.9   0.3
	  </pre>
	</div>
	<div id="pltexample" style="display:none">
	  <b>Note:</b> Plant spec files must be formatted in a specific style,
	  shown below.  The headers and blank lines must all match what is
	  below.  <span class="regreq">It is recommended that you copy and
	  paste this into a new file and then edit the numbers.</span>
	  <br><pre>
*CROP Parameters*
CROPEC IF_MAX IF_INFL KINPUT TDMIN TDOPTMIN TDOPTMAX TDMAX
0.01   2.5    7       0.6    6     20       34       38

*LEAF Growth Parameters*
RTPF  LGC1  LGC2  LGC3  LGC4   LGC5  LGC6  LGC7   LGC8   RUE  CINT
0.10  189   0.392 14.6  0.3    100   0.7   0.015  0.0029 2.8  0.72

*Nitrogen Supply Parameters*
NSUP_MAX1 NSUP_MAX2 NSUP_RF NSUP_C1 NSUP_C2 NSUF_C1 NSUF_C2 NSUF_TF
0.00009   0.7215    0.1     90      2       2.441   4       0.8

*Nitrogen concentration parameters*
TW_Noptmax TW_Noptmin DT_Nmax DT_Nmin RW_Noptdiff TW_Nmin
0.0225     0.012      420     80      0.006       0.006

*Size Parameters*
WDC1  WDC2   WDC3   WDC4   HTC1   HTC2   HTC31  HTC32
0.04  0.105  0.042  0.012  0.0357 0.0245 0.008  0.0033 

*Photosynthesis temperature factors*
PHOTOTEMP1 PHOTOTEMP2 PHOTOTEMP3
1.2        0.006      22.0

*Pruning factors*
PRTWFAC PRLAFAC
1.15    1.45 
	  </pre>
	</div> 
	<div id="sfnexample" style="display:none">
          <b>Note:</b> Solution feed files must be formatted in a specific
	  style, shown below.  The header line must exactly match what is
          below and the solution feed data must consist of two columns in the
	  format displayed below.</span>
          <br><pre>
@DATE  SFN
2008087     0
2008088    50
2008089   100
2008090    50
2008091     0
2008092    50
          </pre>
	</div>
        <div id="irrexample" style="display:none">
          <b>Note:</b> Irrigation files must be formatted in a specific
          style, shown below.  The header line must exactly match what is
          below and the irrigation data must consist of two columns in the  
          format displayed below.</span>
          <br><pre>
@DATE  IRR
2004093 0.96
2004094 0.99
2004095 0.92
2004096 0.93
2004097 0.98
          </pre>
        </div>
<?php
      } else {
	echo '<i>You have reached the maximum of 25 uploaded files!  You must delete an old file before you can upload a new one!</i><br><br>';
      }
   } else if ($mycat == 6) {
      $info = Array();
      $sql = "select * from runs where uid = " . $_SESSION['bmpid'] . " and type='realtime' ORDER BY rid";
      $out = $mysqli_db->query($sql);
      $n = 0;
      $noneSelected = true;
      $hour = 4;
      $minute = 30;
      if ($out != false) {
        while ($row = $out->fetch_array(MYSQLI_BOTH)) {
          $info[$n] = $row;
	  if ($row['auto'] == 1) $noneSelected = false;
          $n++;
        }
      }
?>
      <form name="automate" action="myaccount.php" method="post">
      <input type="hidden" name="mode" value="automate">

      <div style="margin-left: 100px;">
You can configure CCROP to automatically re-run a realtime model at a set
time every day, giving you an updated irrigation recommendation for that day.
This irrigation value can then be downloaded using the CCROP client 
package and sent to your PLC Agent to automatically irrigate by the
recommended amount.<br><br>
      </div>
      <br><br>

      <div style="margin-left: 0px;" align="center">
      <table border=1 align="center" class="manage">
      <tr>
      <th valign="top" class="manageth-nobg">ID</th>
      <th valign="top" class="manageth">Run Name</th>
      <th valign="top" class="manageth">Type</th>
      <th valign="top" class="manageth">Edit</th>
      <th valign="top" class="manageth">Automate</th>
      </tr>

<?php
      $pages['grower'] = 'driver_grower.php';
      $pages['fert'] = 'comparison_fert.php';
      $pages['irr'] = 'comparison_irr.php';
      $pages['fert_irr'] = 'comparison_fert_irr';
      $pages['location'] = 'comparison_location.php';
      $pages['date'] = 'comparison_date.php';
      $pages['technical'] = 'driver_tech.php';
      $pages['realtime'] = 'realtime.php';
      for ($j = 0; $j < $n; $j++) {
        if ($j%2 == 1) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . $info[$j]['rid'] . '</td>';
        echo $td . '<a href="' . $pages[$info[$j]['type']] . '?runid=' . $info[$j]['name'] . '">' . $info[$j]['name'] . '</a></td>';
        echo $td . $info[$j]['type'] . '</td>';
        echo $td . '[<a href="' . $pages[$info[$j]['type']] . '?editid=' . $info[$j]['rid'] . '">edit</a>]</td>';
        echo $td . '<input type="checkbox" name="auto-' . $info[$j]['rid'] . '"';
        if ($info[$j]['auto'] == 1) echo ' checked';
        echo '><br>';
        $hour = 5;
        $minute = 0;
        if ($info[$j]['autoRunTime'] != NULL) {
          $pos1 = strpos($info[$j]['autoRunTime'], ":");
          $pos2 = strpos($info[$j]['autoRunTime'], ":", $pos1+1);
          $hour = floor(substr($info[$j]['autoRunTime'], 0, $pos1));
          $minute = floor(substr($info[$j]['autoRunTime'], $pos1+1, $pos2));
        }
        echo '<select name="autoHour-' . $info[$j]['rid'] . '">';
        for ($h = 1; $h <= 12; $h++) {
          echo '<option name="' . $h . '" value="' . $h . '"';
          if ($hour % 12 == $h%12) echo " selected";
          echo '>' . $h . "\n";
        }
        echo '</select> : <select name="autoMin-' . $info[$j]['rid'] . '">';
        for ($m = 0; $m < 60; $m++) {
          echo '<option name="' . $m . '" value="' . $m . '"';
          if ($minute == $m) echo " selected";
          echo '>';
          if ($m < 10) echo '0';
          echo  $m . "\n";
        }
        echo '</select>';
        echo '<select name="autoAmPm-' . $info[$j]['rid'] . '">';
        echo '<option name="am" value="am"';
        if ($hour < 12) echo " selected";
        echo '>am';
        echo '<option name="pm" value="pm"';
        if ($hour >= 12) echo " selected";
        echo '>pm';
        echo '</select>';
        echo '</td>';

        echo '</tr>';
      }
?>
      </table>
      <br>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
      </div>
      </form>
<?php } else if ($mycat == 7) {
        require('mycosts.php');
      } ?>
	<!-- End body here -->
<?php require('footer.php'); ?>
