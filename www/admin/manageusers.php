<?php 
session_start(); ?>
<html>
<head>
<script language="javascript">
  function check() {
    var a = confirm('Are you sure you want to delete the selected users?');
    return a;
  }
</script>
<title>Container Crop Management Tool - Admin</title>
<?php require('header.php'); ?>
<?php
if (isset($_POST['isSubmitted'])) {
  #Verify that this was a correctly referred submit and not a bot
  $isValid = false;
  if (strpos($_SERVER['HTTP_REFERER'], "manageusers.php") !== false) $isValid = true;
  if (! $isValid) {
    header('HTTP/1.0 401 Unauthorized');
    echo '<b>Invalid request</b>';
  } else {
    #Delete is only operation performed here.  Look at checkboxes
    foreach ($_POST as $key=>$value) {
      if (substr($key, 0, 4) == 'del-' && $value == 'on') {
	$uid = substr($key, 4);
	$sql = 'delete from users where uid=' . $uid;
	$out = mysql_query($sql);
      }
    }
  }
} 
#No else here ... display table after operation performed too!
$sql = "select * from users order by lastName, firstName;";
$out = mysql_query($sql);
$n = 0;
if ($out != false) {
  while ($row = mysql_fetch_array($out, MYSQL_BOTH)) {
    $info[$n] = $row;
    $n++;
  }
}
?>
  <center>
  <h2>Manage Users</h2>
  <form name="manageusers" action="<?php echo $_SERVER['PHP_SELF'];?>" method="post" onSubmit='return check();';>
  <input type="hidden" name="isSubmitted" value=1>
  <div style="margin-left: 0px;" align="center">
  <table border=1 align="center" class="manage">
    <tr>
    <th valign="top" class="manageth">Name</th>
    <th valign="top" class="manageth">E-mail</th>
    <th valign="top" class="manageth">Affiliation</th>
    <th valign="top" class="manageth">Role</th>
    <th valign="top" class="manageth">State</th>
    <th valign="top" class="manageth">Country</th>
    <th valign="top" class="manageth">Edit</th>
    <th valign="top" class="manageth">Delete</th>
    </tr>
    <?php
      for ($j = 0; $j < $n; $j++) {
	$roleSelected = Array();
	$roleTot = $info[$j]['role'];
	for ($i = count($affiliations)-2; $i >= 0; $i--) {
	  if ($roleTot >= pow(2, $i)) {
	    $roleSelected[$i] = True;
	    $roleTot -= pow(2, $i);
	  } else $roleSelected[$i] = False;
	}
        if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
        echo '<tr>';
        echo $td . $info[$j]['lastName'] . ", " . $info[$j]['firstName'] . '</td>';
        echo $td . $info[$j]['email'] . '</td>';
        echo $td . $info[$j]['affiliation'] . '</td>';
        echo $td;
	for ($i = 0; $i < count($affiliations)-1; $i++) {
	  if ($roleSelected[$i]) echo $affiliations[$i] . '<br>';
	}
	if ($info[$j]['role_other'] != NULL && $info[$j]['role_other'] != 'NULL') echo $info[$j]['role_other'];
        echo $td . $info[$j]['state'] . '</td>';
        echo $td . $info[$j]['country'] . '</td>';
        echo $td . '[<a href="moduser.php?id=' . $info[$j]['uid'] . '">edit</a>]</td>';
        echo $td . '<input type="checkbox" name="del-' . $info[$j]['uid'] . '"></td>';
        echo '</tr>';
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
