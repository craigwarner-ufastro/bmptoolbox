<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<script language="javascript">
  function check() {
    var a = true;
    var i = 0;
    var x = "";
    for (i = 0; i < document.editcost.elements.length; i++) {
      if (document.editcost.elements[i].value == '') {
	alert("You must enter a value for all fields.");
	return false;
     }
    }
    return a;
  }
</script>
<?php #require('header.php'); ?>
<center><h2>View a custom cost basis</h2>
</center>
<?php
  $isLoggedIn = true;
  require('dbcnx.php');
  #mysql_select_db("mybmp");
  if ($isLoggedIn && isset($_GET['cid'])) {
    $sql = "select * from costs where cid = " . $_GET['cid'];
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $costs = $row;
      }
    }
    if ($costs['uid'] == 0) {
      #Default value
      $laborData =  file('labor/currentLabor.txt');
      $wageRate = (float)$laborData[0]/60.; 
      $costs['fillContainer'] *= $wageRate;
      $costs['planting'] *= $wageRate;
      $costs['tagging'] *= $wageRate;
      $costs['pruning'] *= $wageRate;
      $costs['spacing'] *= $wageRate;
      $costs['plantMovement'] *= $wageRate;
      $costs['fertTopdress'] *= $wageRate;
    }

?>

<div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
  <td valign="top">Cost Basis Name:</td>
  <td valign="top" align="left"><?php echo $costs['costName'];?></td>
</tr>

<tr>
  <td valign="top">Container Type:</td>
  <td valign="top" align="left"><?php echo $costs['trade'];?></td>
  </td>
</tr>

<?php if ($costs['uid'] == 0) {
  echo '<tr>';
  echo '<td>Wage Rate ($/min):</td>';
  echo "<td>$wageRate</td>";
  echo '</tr>';
}?>

<tr>
  <td>Fertilizer ($/pound N):</td>
  <td><?php echo $costs['fertCostPerPound'];?></td>
</tr>


<tr>
  <td>Liner:</td>
  <td><?php echo $costs['liner'];?></td>
</tr>

<tr>
  <td>Container:</td>
  <td valign="top" align="left"><?php echo $costs['container'];?></td>
</tr>

<tr>
  <td>Substrate:</td>
  <td><?php echo $costs['substrate'];?></td>
</tr>

<tr>
  <td>Fill Container:</td>
  <td><?php echo $costs['fillContainer'];?></td>
</tr>

<tr>
  <td>Planting:</td>
  <td><?php echo $costs['planting'];?></td>
</tr>

<tr>
  <td>Tagging:</td>
  <td><?php echo $costs['tagging'];?></td>
</tr>

<tr>
  <td>Pruning:</td>
  <td><?php echo $costs['pruning'];?></td>
</tr>

<tr>
  <td>Spacing:</td>
  <td><?php echo $costs['spacing'];?></td>
</tr>

<tr>
  <td>Plant movement:</td>
  <td><?php echo $costs['plantMovement'];?></td>
</tr>

<tr>
  <td>Fertilizing topdress:</td>
  <td><?php echo $costs['fertTopdress'];?></td>
</tr>

<tr>
  <td>Irrigation pumping cost:</td>
  <td><?php echo $costs['irrigationPumping'];?></td>
</tr>

<tr>
  <td>Alloc space cost:</td>
  <td><?php echo $costs['allocSpace'];?></td>
</tr>

</table>
</div>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to view a cost basis.';
  }
?>
<?php #require('footer.php'); ?>
