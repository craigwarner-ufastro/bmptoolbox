<?php
  $realtimeFlag = False;
  if (isset($_POST['savename']) && $_POST['savename'] != '' && isset($_SESSION['bmpid'])) {
    $update = false;
    $pfix = "data/" . $bmpusername . "/";
    echo '<script language="javascript">pfix="data/' . $bmpusername . '/";</script>';
    $origId = $id;
    $id = $_POST['savename'];
    $inid = $id;
    if ($pagename == "realtime" and $isSub == 0) {
      $id .= 'b';
      $realtimeFlag = True;
    }
    $runfile = "data/" . $bmpusername . "/run" . $id . ".txt";

    $sql = "select rid from runs where uid = " . $_SESSION['bmpid'] . " and name = '" . $id . "'";
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
	$update = true;
        $rid = $row[0];
      }
    }

    if ($update and !isset($_GET['editid'])) {
      system('rm ' . $pfix . '*' . $id . '*');
      $sql = "update runs set ";
      $sql .= "type = '" . $pagename . "'";
      $sql .= ", ncomp = " . $nruns;
      $sql .= " where rid=" . $rid;
    } else if (!isset($_GET['editid'])) {
      $sql = "insert into runs(uid, name, type, ncomp)"; 
      $sql .= " values (" . $_SESSION['bmpid'] . ", '";
      $sql .= $id . "', '";
      $sql .= $pagename . "', ";
      $sql .= $nruns . ")";
    }
    if ($realtimeFlag && isset($doSave) && $doSave && !isset($_GET['editid']) && $origId != $id) {
      $update = false;
      $sql = "select rid from runs where uid = " . $_SESSION['bmpid'] . " and name = '" . $inid . "'";
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
          $update = true;
          $rid = $row[0];
        }
      }
      if (!$update) {
	$sql = "insert into runs(uid, name, type, ncomp)"; 
	$sql .= " values (" . $_SESSION['bmpid'] . ", '";
	$sql .= $inid . "', '";
	$sql .= $pagename . "', ";
	$sql .= $nruns . ")";
	$realtimeFlag = false;
      }
    }
    if (!$realtimeFlag) {
      $out = $mysqli_db->query($sql);
      if ($out == false) {
        echo 'Failed to update database!';
      }
      if (isset($_POST['makedefault']) && $_POST['makedefault'] != '') {
        $sql = "update runs set defFile=0 where type='$pagename' and uid=" . $_SESSION['bmpid'];
        $out = $mysqli_db->query($sql);
        if ($out == false) {
          echo 'Failed to update database!';
        }
        $sql = "update runs set defFile=1 where name='$inid' and uid=" . $_SESSION['bmpid'];
        $out = $mysqli_db->query($sql);
        if ($out == false) {
	  echo 'Failed to update database!';
        }
      }
    }
  }

  #create input file from form POST array
  $infile = $pfix . $id . "_inputs.txt";
  $f = fopen($infile, 'wb');
  foreach ($_POST as $key=>$value) {
    fwrite($f, $key . "\t" . $value . "\n");
  }
  fclose($f);
  if (isset($_POST)) {
/*
    $_SESSION['postBack'] = Array();
    foreach ($_POST as $key=>$value) {
      $_SESSION['postBack'][$key] = $value;
    }
*/
    $_SESSION['postBack']['pagename'] = $pagename;
  }
?>
