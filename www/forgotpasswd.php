<?php session_start(); ?>
<html>
<head>
<script language="javascript">
function check() {
   var d = new Date();
   var a = true;
   if (document.register.last.value == '') {
      alert('You must enter your last name.');
      a = false;
   } else if (document.register.email.value == '') {
      alert('You must enter your e-mail address.');
      a = false;
   } else if (document.register.email.value.indexOf('@') == -1) {
      alert('You must enter a VALID e-mail address.');
      a = false;
   } else if (document.register.month.value == 'select') {
      alert('You must enter your birthday.');
      a = false;
   } else if (isNaN(parseInt(document.register.day.value))) {
      alert('You must enter your birthday.');
      a = false;
   } else if (isNaN(parseInt(document.register.year.value))) {
      alert('You must enter your birthday.');
      a = false;
   }
   return a;
}
</script>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<?php if (!isset($_POST['isSubmitted'])) { ?>
<center><h2>Reset your password</h2>
</center>
<form name="register" action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post" onSubmit='return check();'>
<input type="hidden" name="isSubmitted" value="true">
<div style="margin-left: 100px;">
Enter your last name, e-mail address, and birthday and if these match our records, your username and a new random password will be sent to your e-mail address.
<table border=0 align="left">
<tr><td class="regreq" align="right" valign="top">
Last Name:
</td>
<td valign="top">
<input type="text" size=15 name="last">
</td>
</tr>
<tr><td class="regreq" align="right">
E-mail address:
</td>
<td>
<input type="text" size=25 name="email">
</td>
</tr>
<tr><td class="regreq" align="right" valign="top">
Birthday:
</td>
<td>
<select name="month"><option name="select" value="select">[Select]
<?php
   $months = array("Jan","Feb","Mar","Apr","May","June","July","Aug","Sept","Oct","Nov","Dec");
   for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value="' . $months[$j] . '">' . $months[$j];
   }
?>
</select>
<input type="text" name="day" size=2 value="dd" onFocus="this.value='';">
<input type="text" name="year" size=4 value="yyyy" onFocus="this.value='';">
<br>
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Submit">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
</div>
</form>
<?php } else {
   $birthday = $mysqli_db->real_escape_string(strtotime($_POST['day'] . ' ' . $_POST['month'] . ' ' . $_POST['year']));
   $email = $mysqli_db->real_escape_string($_POST['email']);
   $lastName = $mysqli_db->real_escape_string($_POST['last']);
   $sql = "select * from users where lastName = '" . $lastName . "' and email = '" . $email . "' and birthday = " . $birthday;
   $out = $mysqli_db->query($sql);
   $isValidAccout = false;
   if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$isValidAccount = true;
	$bmpid = $row['uid'];
        $username = $row['username'];
      }
   }
   if ($isValidAccount) {
      $newPass = '';
      $letters = array_merge(range('A','H'), range('J','N'), range('P','Z'), range('a','k'), range('m','z'), range(2,9));
      for ($j = 0; $j < 6; $j++) {
	$n = mt_rand(0, count($letters)-1);
        $newPass .= $letters[$n];
      }
      echo '<div style="margin-left: 100px;">An e-mail has been sent to ' . $_POST['email'] . ' with your username and new password.</div>'; 
      $message = "Your BMP Toolbox account password has been reset to a random value.  It is recommended that you change this to something you will remember.  Simply visit the Change Password link under My Account the next time you log in.\r\n\r\n";
      $message .= 'username: ' . $username . "\r\n";
      $message .= 'password: ' . $newPass;
      mail($_POST['email'], "BMP Toolbox password reset", $message, "From: admin@mybmp.com");
      $newPass = sha1($newPass);
      $sql = "update users set password = '" . $newPass . "' where uid = " . $bmpid;
      $out = $mysqli_db->query($sql);
   } else {
      echo '<div style="margin-left: 100px;">';
      echo 'The last name, e-mail, and birthday you submitted do not match our records.<br>';
      echo '<a href="forgotpasswd.php">Click here</a> to try again.  <a href="register.php">Click here to register an account.</a>';
      echo '</div>';
   }
}
?>
	<!-- End body here -->
<?php require('footer.php'); ?>
