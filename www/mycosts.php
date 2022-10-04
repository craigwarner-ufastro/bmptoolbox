<h2><center>Manage My Costs</center></h2>
<center><a href="createCosts.php">Create a new cost basis</a><br>

<?php
  $laborData =  file('labor/currentLabor.txt');
  $wageRate = (float)$laborData[0]/60.;  
  echo "<br>Current default wage rate ($/minute): $wageRate<br><br>";
  echo "</center>";

  if ($isLoggedIn) {
    if (isset($_POST['mode']) && $_POST['mode'] == "createcost") {
      $uid = $_SESSION['bmpid'];
      $trade = $_POST['trade'];
      $liner = $_POST['liner'];
      $container = $_POST['container'];
      $substrate = $_POST['substrate'];
      $fillContainer = $_POST['fillContainer'];
      $planting = $_POST['planting'];
      $tagging = $_POST['tagging'];
      $pruning = $_POST['pruning'];
      $spacing = $_POST['spacing'];
      $plantMovement = $_POST['plantMovement'];
      $fertTopdress = $_POST['fertTopdress'];
      $irrigationPumping = $_POST['irrigationPumping'];
      $allocSpace = $_POST['allocSpace'];
      $costName = $_POST['costName'];
      $fertCostPerPound = $_POST['fertCostPerPound'];
      #Add new cost 
      $sql = "insert into costs(uid, trade, liner, container, substrate, fillContainer, planting, tagging, pruning, spacing, plantMovement, fertTopdress, irrigationPumping, allocSpace, costName, fertCostPerPound)";
      $sql .= " VALUES ($uid, '$trade', $liner, $container, $substrate, $fillContainer, $planting, $tagging, $pruning, $spacing, $plantMovement, $fertTopdress, $irrigationPumping, $allocSpace, '$costName', $fertCostPerPound)";
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo '<center><span class="regreq">Failed to add cost!</span></center>';
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "editcost") {
      $cid = $_POST['cid'];
      $uid = $_SESSION['bmpid'];
      $trade = $_POST['trade'];
      $liner = $_POST['liner'];
      $container = $_POST['container'];
      $substrate = $_POST['substrate'];
      $fillContainer = $_POST['fillContainer'];
      $planting = $_POST['planting'];
      $tagging = $_POST['tagging'];
      $pruning = $_POST['pruning'];
      $spacing = $_POST['spacing'];
      $plantMovement = $_POST['plantMovement'];
      $fertTopdress = $_POST['fertTopdress'];
      $irrigationPumping = $_POST['irrigationPumping'];
      $allocSpace = $_POST['allocSpace'];
      $costName = $_POST['costName'];
      $fertCostPerPound = $_POST['fertCostPerPound'];
      #Edit cost
      $sql = "update costs set trade='$trade', liner=$liner, container=$container, substrate=$substrate, fillContainer=$fillContainer, planting=$planting, tagging=$tagging, pruning=$pruning, spacing=$spacing";
      $sql .= ", plantMovement=$plantMovement, fertTopdress=$fertTopdress, irrigationPumping=$irrigationPumping, allocSpace=$allocSpace, costName='$costName', fertCostPerPound=$fertCostPerPound";
      $sql .= " where uid=$uid and cid=$cid;";
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo '<center><span class="regreq">Failed to edit cost!</span></center>';
      }
    } else if (isset($_POST['mode']) && $_POST['mode'] == "costs") {
      foreach ($_POST as $key=>$value) {
        //delete costs 
	if (substr($key, 0, 4) == 'del-' && $value == 'on') {
	  $id = substr($key, 4);
	  $sql = 'delete from costs where cid="' . $id . '" AND uid=' . $_SESSION['bmpid'];
          $out = $mysqli_db->query($sql);
        } 
      }
    }
    $costs = Array();
    $sql = "select * from costs where uid = " . $_SESSION['bmpid'] . " or uid=0";
    $out = $mysqli_db->query($sql);
    $n = 0;
    if ($out != false) {
      while ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $costs[] = $row;
	$n++;
      }
    }
?>
    <form name="managecosts" action="myaccount.php" method="post">
    <input type="hidden" name="mode" value="costs">
    <div style="margin-left: 0px;" align="center">
    <table border=1 align="center" class="manage">
    <tr>
      <th valign="top" class="manageth-nobg">Cost Name</th>
      <th valign="top" class="manageth">Trade</th>
      <th valign="top" class="manageth">View</th>
      <th valign="top" class="manageth">Edit</th>
      <th valign="top" class="manageth">Delete</th>
    </tr>
<?php
      for ($j = 0; $j < $n; $j++) {
	if ($j%2 == 0) $td = '<td class="managetd">'; else $td = '<td class="managetdalt">';
	echo '<tr>';
	echo $td . $costs[$j]['costName'] . '</td>';
        echo $td . $costs[$j]['trade'] . '</td>';
        echo $td . '[<a href="javascript:void window.open(\'viewCosts.php?cid=' . $costs[$j]['cid'] . '\', \'View Cost\', \'width=600,height=500,toolbar=0,menubar=0,titlebar=0,location=0,status=0,scrollbars=1,resizable=1,left=0,top=0\');">view</a>]</td>';
        echo $td;
	if ($costs[$j]['uid'] != 0) echo '[<a href="editCosts.php?cid=' . $costs[$j]['cid'] . '">edit</a>]';
	echo '</td>';
        echo $td;
	if ($costs[$j]['uid'] != 0) echo '<input type="checkbox" name="del-' . $costs[$j]['cid'] . '">';
	echo '</td>';
	echo '</tr>';
      }
?>
      </table>
      <br>
      <input type="submit" name="submit" value="Submit">
      <input type="reset" name="reset" value="Reset">
      </div>
      </form>
<?php
  } else {
    echo 'You must <a href="login.php">login</a> to view your costs.';
  }
?>
