<?php session_start(); ?>
<html>
<head>
<script language="javascript">
  function setOtherRole(isChecked) {
    document.register.role_other.disabled= !isChecked;
  }

function check() {
   var d = new Date();
   var a = true;
   var nroles = document.register.nroles.value;
   var roleSelected = false;
   for (j = 0; j < nroles-1; j++) {
      if (document.getElementById("role"+j).checked) roleSelected = true;
   }
   if (document.getElementById("roleO").checked) roleSelected = true;
   if (document.register.first.value == '') {
      alert('You must enter your first name.');
      a = false;
   } else if (document.register.last.value == '') {
      alert('You must enter your last name.');
      a = false;
   } else if (document.register.affiliation.value == '') {
      alert('You must enter your affiliation.');
      a = false;
   } else if (!roleSelected) {
      alert('You must select an affiliation role.');
      a = false;
   } else if (document.getElementById("roleO").checked && document.register.role_other.value == '') {
      alert('You must specify an affiliation role.');
      a = false;
   } else if (document.register.state.value == 'select') {
      alert('You must select a state.');
      a = false;
   } else if (document.register.state.value == 'None' && document.register.country.value == 'United States') {
      alert('You must select a state.');
      a = false;
   } else if (document.register.country.value == 'select') {
      alert('You must select a country.');
      a = false;
   } else if (document.register.email.value == '') {
      alert('You must enter your e-mail address.');
      a = false;
   } else if (document.register.email.value.indexOf('@') == -1) {
      alert('You must enter a VALID e-mail address.');
      a = false;
   } else if (document.register.id.value.length < 5) {
      alert('Your BMP Toolbox ID must be at least 5 characters.');
      a = false;
   } else if (document.register.id.value.indexOf(';') != -1) {
      alert('Your BMP Toolbox ID cannot contain a ;'); 
      a = false;
   } else if (document.register.id.value.indexOf(',') != -1) {
      alert('Your BMP Toolbox ID cannot contain a ,');
      a = false;
   } else if (document.register.id.value.indexOf(' ') != -1) {
      alert('Your BMP Toolbox ID cannot contain spaces');
      a = false;
   } else if (document.register.id.value.indexOf('"') != -1) {
      alert('Your BMP Toolbox ID cannot contain quotes');
      a = false;
   } else if (document.register.id.value.indexOf("'") != -1) {
      alert("Your BMP Toolbox ID cannot contain a '");
      a = false;
   } else if (document.register.id.value.indexOf(':') != -1) {
      alert('Your BMP Toolbox ID cannot contain a :');
      a = false;
   } else if (document.register.passwd1.value != document.register.passwd2.value) {
      alert('Passwords do not match.');
      a = false;
   } else if (document.register.passwd1.length < 5) {
      alert('Your password must be at least 5 characters.');
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
   } else if (document.register.verify.value != document.register.vnum.value) {
      alert('Verification failed!');
      a = false;
   }
   return a;
}
</script> 
<title>BMP Toolbox</title>
<?php require('header.php'); ?>
	<!-- Start body here -->
<center><h2>Register an account</h2>
<span class="small">Fields in red are required</span></center>
<br>
<form name="register" action="doregister.php" method="post" onSubmit='return check();'>
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr><td valign="top" align="right" class="regreq">
First Name:
</td>
<td valign="top" align="left">
<input type="text" size=15 name="first">
</td>
</tr>
<tr><td class="regreq" align="right">
Last Name:
</td>
<td>
<input type="text" size=15 name="last">
</td>
</tr>
<tr><td class="regreq" align="right">
Affiliation:
</td>
<td>
<input type="text" size=15 name="affiliation" maxlength=32>
</td>
</tr>

<tr><td class="regreq" align="right">
Affiliation Role<br>(select all that apply):
</td>
<td>
<?php
  for ($j = 0; $j < count($affiliations)-1; $j++) {
    echo '<input type="checkbox" name="role[]" id="role' . $j . '" value=' . pow(2, $j) . '>' . $affiliations[$j] . "<br>\n";
  }
  echo '<input type="hidden" name="nroles" value=' . count($affiliations) . '>';
?>
<input type="checkbox" name="role[]" id="roleO" value="other" onClick="setOtherRole(this.checked);">Other (specify) 
<input type="text" size=15 name="role_other" disabled><br>
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

<tr><td class="regreq" align="right">
E-mail address:
</td>
<td>
<input type="text" size=25 name="email">
</td>
</tr>

<tr><td class="regreq" align="right" valign="top">
BMP Toolbox ID:
</td>
<td>
<input type="text" size=15 name="id">
<br>
<span class="small">Must be at least 5 characters.</span>
</td>
</tr>

<tr><td class="regreq" align="right" valign="top">
Password:
</td>
<td>
<input type="password" size=15 name="passwd1">
<br><span class="small">Must be at least 5 characters.</span>
</td>
</tr>
<tr><td class="regreq" align="right">
Re-type Password:
</td>
<td>
<input type="password" size=15 name="passwd2">
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
<br><span class="small">In case you forget your password.</span>
</td>
</tr>
<?php
$words = array("first", "second", "third", "fourth", "fifth");
$nums = array();
for ($j = 0; $j < 5; $j++) {
   $nums[$j] = mt_rand(0, 100);
}
$i = mt_rand(0, 4);
echo '<input type="hidden" name="vnum" value="' . $nums[$i] . '">';
?>
<tr><td align="right" class="regreq" valign="top">
Verification:<br>
</td>
<td>
<input type="text" size=3 name="verify">
<br>
<span class="small">
For security purposes, five numbers between 0 and 100 will be listed below,
separated by spaces.
<br>Enter the <?php echo $words[$i]; ?> number in the
sequence into the Verification box above. 
<br>
<?php for ($j = 0; $j < 5; $j++) echo $nums[$j] . ' &nbsp;&nbsp;'; ?>
</span>
</td>
</tr>
<tr><td></td>
<td><input type="submit" name="submit" value="Register">
<input type="reset" name="reset" value="Reset">
</td></tr>
</table>
</div>
</form>
	<!-- End body here -->
<?php require('footer.php'); ?>
