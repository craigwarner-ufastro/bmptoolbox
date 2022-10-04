<?php
  if ($isLoggedIn && (!isset($_POST['savename']) || $_POST['savename'] == '') && isset($_SESSION['bmpid'])) {
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
    if ($nsaved < 50) { ?>
      <form name="outputSave" action="<?php echo $_SERVER['PHP_SELF'];?>" method="POST" onSubmit="return checkRunsOutput();" target="ccmt">
      <input type="hidden" name="saveSubmitted" value=1>
      <input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
      <input type="hidden" name="id" value="<?php echo $id; ?>">
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
      </td></tr>
      <tr><td colspan=2>
      <br>
      <center>
      <input type="Submit" name="Submit" value="Save">
      </center>
      </td></tr></table>
      </form>
      <br>
<?php
    } else { 
      echo '<i>You have reached the maximum of 50 saved runs!  Visit <a href="myaccount.php?cat=3">your account</a> to delete previously saved runs.<br><br>';
    }
  }
?>
