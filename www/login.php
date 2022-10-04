<?php session_start(); ?>
<html>
<head>
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<center><h2>Login</h2>
</center>
<form name="login" action="login.php" method="post">
<input type="hidden" name="isSubmitted" value="true">
<input type="hidden" name="referrer" value="<?php if (isset($_SERVER['HTTP_REFERER'])) echo $_SERVER['HTTP_REFERER']; ?>">
<div style="margin-left: 100px;">
<?php
   $validLogin = false;
   if (isset($_POST['isSubmitted'])) {
      $user = $mysqli_db->real_escape_string($_POST['id']);
      $passwd = sha1($_POST['passwd']);
      $sql = "select * from users where username='" . $user . "' and password='" . $passwd . "'";
      $out = $mysqli_db->query($sql);
      if ($out != false) {
	if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	   $validLogin = true;
	   $bmpid = $row['uid'];
	}
	else {
	   echo '<span class="regreq">Invalid username or password!  Try again.</span><br><br>';
	}
      }
   }
   if (!$validLogin) {
?>
<table border=0 align="top">
<tr><td class="regreq" align="right" valign="top">
BMP Toolbox ID:
</td>
<td>
<input type="text" size=15 name="id">
</td>
</tr>
<tr><td class="regreq" align="right" valign="top">
Password:
</td>
<td>
<input type="password" size=15 name="passwd">
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Login">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
<br>
Don't have an account?  <a href="register.php">Click here to register</a>.
<br>
<br>
Forget your password?  <a href="forgotpasswd.php">Click here to reset it</a>.
</div>
</form>
<script language="javascript">
  document.login.id.focus();
</script>
<?php
} else {
   if (!file_exists("data/" . $user)) {
      mkdir("data/" . $user);
   }
   $lastPage = $_POST['referrer']; 
   if (strpos($lastPage,'98.180.50.178') !== false and strpos($lastPage,'login.php') === false and strpos($lastPage,'logout.php') === false and strpos($lastPage, 'register') === false) {
      $nextPage = $lastPage; 
   } else if (strpos($lastPage,'bmptoolbox.org') !== false and strpos($lastPage,'login.php') === false and strpos($lastPage,'logout.php') === false and strpos($lastPage, 'register') === false) {
      $nextPage = $lastPage;
   } else $nextPage= "index.php";
   $_SESSION['bmpid'] = $bmpid;
   echo '<script language="javascript">document.location.href="' . $nextPage . '";</script>';
}
?>
	<!-- End body here -->
<?php require('footer.php'); ?>
