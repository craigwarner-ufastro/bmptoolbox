<?php
  if ($isLoggedIn) {
    $nsaved = 0;
    $savedRuns = array();
    $sql = 'select name from runs where uid = ' . $_SESSION['bmpid'];
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$savedRuns[$nsaved] = $row[0];
	$nsaved++;
      }
    }
    echo '<script language="javascript">';
    for ($j = 0; $j < $nsaved; $j++) {
      echo 'savedRuns[' . $j . '] = "' . $savedRuns[$j] . '";';
    }
    echo '</script>';
?>
    <tr><td colspan=10>
<?php if ($nsaved < 50) { ?>
      <table align="center">
      <tr><td align="right">
	<b>Optionally enter a name to save this run as:</b> 
	<input type="text" name="savename" id="savename" size=16 maxlength=16 onkeypress="return validChar(event);">
	<br>
	<span class="small">No spaces or special characters other than _, -, and . allowed.</span>
	<br>
      </td><td width=20></td>
      <td valign="top" width=200>
	<b>Make default:</b>
	<input type="checkbox" name="makedefault" id="makedefault">
      </td></tr></table>
      <br>
<?php
    } else { 
      echo '<i>You have reached the maximum of 50 saved runs!  Visit <a href="myaccount.php?cat=3">your account</a> to delete previously saved runs.</i><br><br>';
    }
?>
    </td></tr>
<?php
  } else {
    echo '<div class="reditalics">The tools on this website are free of charge, but user must login and ';
    echo '<a href="register.php">establish an account</a> to change and submit inputs other than default values.  By establishing ';
    echo 'an account, the user can also save and manage inputs and outputs from simulations.</div><br>';
  }
?>
