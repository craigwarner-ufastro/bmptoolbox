<?php
  #ini_set('session.cache_limiter', 'private');
  session_start();
  $isValid = true;
  if (!isset($_POST['first']) || strpos($_POST['first'],'@') !== false || strpos($_POST['first'], ':') !== false) $isValid=false;
  if (!isset($_POST['last']) || strpos($_POST['last'],'@') !== false || strpos($_POST['first'], ':') !== false) $isValid=false;
  if (!isset($_POST['verify']) || $_POST['verify'] == '') $isValid=false;
  if (! $isValid) {
     header('HTTP/1.0 401 Unauthorized');
  }
?>
<html>
<head>
<script language="javascript">
function check() {
   var d = new Date();
   var a = true;
   if (document.register.id.value.length < 5) {
      alert('Your BMP Toolbox ID must be at least 5 characters.');
      a = false;
   }
}
</script>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<div style="margin-left: 100px;">
<?php
   $isUser= false;
   $isEmail = false;
   $user = $mysqli_db->real_escape_string($_POST['id']);
   $email = $mysqli_db->real_escape_string($_POST['email']);
   $role = 0; 
   $role_other = NULL;
   for ($j=0; $j < count($_POST['role']); $j++) {
      if ($_POST['role'][$j] != 'other') {
	$role += floor($_POST['role'][$j]);
      } else $role_other = $_POST['role_other'];
   }
   $sql = "select * from users where username = '" . $user . "'";
   $out = $mysqli_db->query($sql);
   if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$isUser = true;
      }
   }
   $sql = "select * from users where email = '" . $email . "'";
   $out = $mysqli_db->query($sql);
   if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $isEmail = true;
      }
   }
   if (! $isValid) {
      echo '<b>Invalid request</b>';
   } else if ($isEmail) {
      echo '<span class="regreq">There is already an account registered to the e-mail address ' . $email . '</span><br>';
      echo 'Try <a href="login.php">logging in</a> or <a href="forgotpasswd.php">click here</a> if you forgot your password.';
   } else if ($isUser) {
?>
<span class="regreq">The user id you requested is not available.  Hint:
Try adding a number to the end of your desired username.</span>
<form name="register" action="doregister.php" method="post" onSubmit='return check();'>
<table border=0 align="left">
<tr><td class="regreq" align="right" valign="top">
BMP Toolbox ID:
</td>
<td>
<input type="text" size=15 name="id" <?php echo 'value="' . $_POST['id'] . $_POST['year'] . '"';?>>
<br>
<span class="small">Must be at least 5 characters.</span>
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Register">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
<?php
   foreach($_POST as $key => $value) {
      if ($key != "id") {
	echo '<input type="hidden" name="' . $key . '" value="' . $value . '">';
      }
   }
?>
</form>
<?php
   } else {
      $passwd = sha1($_POST['passwd1']);
      $birthday = strtotime($_POST['day'] . ' ' . $_POST['month'] . ' ' . $_POST['year']);
      $sql = "insert into users (firstName, lastName, affiliation, role, role_other, state, country, username, email, password, birthday) values (";
      $sql .= "'" . $mysqli_db->real_escape_string($_POST['first']) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['last']) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['affiliation']) . "'";
      $sql .= ", " . $role;
      $sql .= ", '" . $mysqli_db->real_escape_string($role_other) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['state']) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['country']) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['id']) . "'";
      $sql .= ", '" . $mysqli_db->real_escape_string($_POST['email']) . "'";
      $sql .= ", '" . $passwd . "'";
      $sql .= ", " . $mysqli_db->real_escape_string($birthday) . ")";
      $out = $mysqli_db->query($sql);
      if ($out != false) {
	echo '<span class="regreq">You have successfully registered an account with BMP Toolbox!  A confirmation e-mail has been sent to ' . $_POST['email'] . '.</span>';
	$message = 'Congratulations on successfully registering an account with BMP Toolbox!  Your unique user id is ' . $_POST['id'] . '.  Save this e-mail for your records.';
	mail($_POST['email'], "BMP Toolbox account info", $message, "From: admin@mybmp.com");
	$_SESSION['bmpid'] = $_POST['id']; 
      } else {
	echo '<span class="regreq">Registration failed.  Please try again later.</span>';
      }
   }
    
?>
</div>
	<!-- End body here -->
<?php require('footer.php'); ?>
