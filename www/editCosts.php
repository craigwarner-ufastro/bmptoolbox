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
<?php require('header.php'); ?>
<center><h2>Edit a custom cost basis</h2>
Edit your custom cost basis by entering your costs in $/unit for each of the items listed below.  Select whether this cost basis applies to trade #1 or trade #3 containers and give this cost basis a name to save it.
</center>
<?php
  if ($isLoggedIn && isset($_GET['cid'])) {
    $sql = "select * from costs where cid = " . $_GET['cid'];
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $costs = $row;
      }
    }

?>

<form name="editcost" action="myaccount.php" method="post" onSubmit='return check();'>
<input type="hidden" name="mode" value="editcost">
<input type="hidden" name="cid" value="<?php echo $costs['cid']; ?>">
<div style="margin-left: 100px;">
<table border=0 align="left">
<tr>
  <td valign="top">Cost Basis Name:</td>
  <td valign="top" align="left"><input type="text" size=15 name="costName" value="<?php echo $costs['costName'];?>"></td>
</tr>

<tr>
  <td valign="top">Container Type:</td>
  <td valign="top" align="left">
  <input type="radio" name="trade" value="Trade #1" <?php if ($costs['trade'] == "Trade #1") echo 'checked';?>>Trade #1 
  <input type="radio" name="trade" value="Trade #3" <?php if ($costs['trade'] == "Trade #3") echo 'checked';?>>Trade #3
  </td>
</tr>

<tr>
  <td>Fertilizer ($/pound N):</td>
  <td><input type="text" size=5 name="fertCostPerPound" onkeypress="return validDec(event);" value="<?php echo $costs['fertCostPerPound'];?>"></td>
</tr>


<tr>
  <td>Liner:</td>
  <td><input type="text" size=5 name="liner" onkeypress="return validDec(event);" value="<?php echo $costs['liner'];?>"></td>
</tr>

<tr>
  <td>Container:</td>
  <td valign="top" align="left"><input type="text" size=5 name="container" onkeypress="return validDec(event);" value="<?php echo $costs['container'];?>"></td>
</tr>

<tr>
  <td>Substrate:</td>
  <td><input type="text" size=5 name="substrate" onkeypress="return validDec(event);" value="<?php echo $costs['substrate'];?>"></td>
</tr>

<tr>
  <td>Fill Container:</td>
  <td><input type="text" size=5 name="fillContainer" onkeypress="return validDec(event);" value="<?php echo $costs['fillContainer'];?>"></td>
</tr>

<tr>
  <td>Planting:</td>
  <td><input type="text" size=5 name="planting" onkeypress="return validDec(event);" value="<?php echo $costs['planting'];?>"></td>
</tr>

<tr>
  <td>Tagging:</td>
  <td><input type="text" size=5 name="tagging" onkeypress="return validDec(event);" value="<?php echo $costs['tagging'];?>"></td>
</tr>

<tr>
  <td>Pruning:</td>
  <td><input type="text" size=5 name="pruning" onkeypress="return validDec(event);" value="<?php echo $costs['pruning'];?>"></td>
</tr>

<tr>
  <td>Spacing:</td>
  <td><input type="text" size=5 name="spacing" onkeypress="return validDec(event);" value="<?php echo $costs['spacing'];?>"></td>
</tr>

<tr>
  <td>Plant movement:</td>
  <td><input type="text" size=5 name="plantMovement" onkeypress="return validDec(event);" value="<?php echo $costs['plantMovement'];?>"></td>
</tr>

<tr>
  <td>Fertilizing topdress:</td>
  <td><input type="text" size=5 name="fertTopdress" onkeypress="return validDec(event);" value="<?php echo $costs['fertTopdress'];?>"></td>
</tr>

<tr>
  <td>Irrigation pumping cost:</td>
  <td><input type="text" size=5 name="irrigationPumping" onkeypress="return validDec(event);" value="<?php echo $costs['irrigationPumping'];?>"></td>
</tr>

<tr>
  <td>Alloc space cost:</td>
  <td><input type="text" size=5 name="allocSpace" onkeypress="return validDec(event);" value="<?php echo $costs['allocSpace'];?>"></td>
</tr>

<tr>
  <td></td>
  <td><br><br><input type="submit" name="submit" value="Edit Cost Basis">
  <input type="reset" name="reset" value="Reset">
  </td>
</tr>
</table>
</div>
</form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to create a cost basis.';
  }
?>
<?php require('footer.php'); ?>
