<?php 
session_start(); ?>
<html>
<head>
<script language="javascript">
  function updateChangeForm(state, country) {
    for (j = 0; j < document.moduser.state.options.length; j++) {
      if (document.moduser.state.options[j].value == state) document.moduser.state.selectedIndex = j; 
    }
    for (j = 0; j < document.moduser.country.options.length; j++) {
      if (document.moduser.country.options[j].value == country) document.moduser.country.selectedIndex = j;
    }
  }

function checkInfo() {
   var d = new Date();
   var a = true;
   var nroles = document.moduser.nroles.value;
   var roleSelected = false;
   for (j = 0; j < nroles-1; j++) {
      if (document.getElementById("role"+j).checked) roleSelected = true;
   }
   if (document.getElementById("roleO").checked) roleSelected = true;
   if (document.moduser.first.value == '') {
      alert('You must enter your first name.');
      a = false;
   } else if (document.moduser.last.value == '') {
      alert('You must enter your last name.');
      a = false;
   } else if (document.moduser.affiliation.value == '') {
      alert('You must enter your affiliation.');
      a = false;
   } else if (!roleSelected) {
      alert('You must select an affiliation role.');
      a = false;
   } else if (document.getElementById("roleO").checked && document.moduser.role_other.value == '') {
      alert('You must specify an affiliation role.');
      a = false;
   } else if (document.moduser.state.value == 'select') {
      alert('You must select a state.');
      a = false;
   } else if (document.moduser.state.value == 'None' && document.moduser.country.value == 'United States') {
      alert('You must select a state.');
      a = false;
   } else if (document.moduser.country.value == 'select') {
      alert('You must select a country.');
      a = false;
   } else if (document.moduser.email.value == '') {
      alert('You must enter your e-mail address.');
      a = false;
   } else if (document.moduser.email.value.indexOf('@') == -1) {
      alert('You must enter a VALID e-mail address.');
      a = false;
    } else if (document.moduser.verify.value != document.moduser.vnum.value) {
      alert('Verification failed!');
      a = false;
    }
    return a;
  }
</script>
<title>Container Crop Management Tool - Admin</title>
<?php require('header.php'); ?>
  <div align="right">
  <a href="manageusers.php">Manage Users</a>
  <br>
  </div>
  <br>

<?php
if (isset($_POST['isSubmitted'])) {
  #Verify that this was a correctly referred submit and not a bot
  $isValid = true; 
  if (strpos($_POST['first'],'@') !== false) $isValid=false;
  if (strpos($_POST['last'],'@') !== false) $isValid=false;
  if (!isset($_POST['verify']) || $_POST['verify'] == '') $isValid=false;

  $id = $_POST['id'];
  $admin = 'FALSE';
  if ($_POST['admin'] == "yes") $admin = 'TRUE';
  if (! $isValid) {
    header('HTTP/1.0 401 Unauthorized');
    echo '<b>Invalid request</b>';
  } else {
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
    $sql .= "email = '" . $_POST['email'] . "'";
    $sql .= " where uid = " . $id;
    $out = mysql_query($sql);
    if ($out != false) {
      echo '<center>Information successfully updated!</center>';
    } else {
      echo '<center><span class="regreq">Failed to update information!</span></center>';
    }
  }
} else if (isset($_GET['id'])) {
  $sql = "select * from users where uid = " . $_GET['id'];
  $out = mysql_query($sql);
  if ($out != false) {
    if ($row = mysql_fetch_array($out, MYSQL_BOTH)) {
      $info = $row;
    }
  }
?>
  <center>
  <h2>Modify a user</h2>
  <span class="small">Fields in red are required</span></center>
  <form name="moduser" action="<?php echo $_SERVER['PHP_SELF'];?>" method="POST" onSubmit='return checkInfo();'>
  <input type="hidden" name="isSubmitted" value=1>
  <input type="hidden" name="id" value="<?php echo $_GET['id']; ?>">
  <div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
<td valign="top" align="right" class="regreq">
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
<?php require('../stateSelect.php'); ?>
<br>
<span class="small">Select "None" if you are not in the USA.</span>
</td>
</tr>

<tr><td class="regreq" align="right">
Country
</td>
<td>
<?php require('../countrySelect.php'); ?>
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

<?php require('verify.php'); ?>
    <tr><td></td>
    <td>
      <input type="submit" name="submit" value="Update">
      <input type="reset" name="reset" value="Reset">
    </td></tr>
  </table>
  </div>
  </form>
<?php
}
?>
        <!-- End body here -->
<?php require('footer.php'); ?>
