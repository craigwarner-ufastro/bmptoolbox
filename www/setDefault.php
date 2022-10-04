<?php
  if ($isLoggedIn) {
    $rid = -1;
    $defFile = "";
    if (isset($_GET['editid']) && $isLoggedIn) {
      $rid = $_GET['editid'];
      $sql = 'select name from runs where rid = ' . $rid; 
    } else {
      $sql = "select name from runs where defFile=1 AND type='$pagename' AND uid=" . $_SESSION['bmpid'];
    }
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $defFile = "data/" . $bmpusername . '/' . $row[0] . '_inputs.txt';
	if ($pagename == "realtime" && isset($_GET['editid']) && $isLoggedIn) {
	  $defFile = "data/" . $bmpusername . '/' . $row[0] . 'b_inputs.txt';
	}
	$id = $row[0];
      }
    }
    if (file_exists($defFile)) {
      $spec = file($defFile);
      for ($l = 0; $l < count($spec); $l++) {
	$temp = preg_split("/\s+/", trim($spec[$l]));
        if (count($temp) > 1) $input[$temp[0]] = $temp[1];
      }
      $input['id'] = $id;
      echo '<script language="javascript">';
      echo 'if (document.paramForm) {';
      foreach ($input as $key=>$value) {
	if ($key == "savename" && $rid == -1) continue;
	echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");'; 
      }
      foreach ($input as $key=>$value) {
        if ($key == "WFNAME") {
	  echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
	}
      }
      echo '}';
      echo '</script>';
    }
  }
/*
  if (isset($_SESSION['postBack']) && !isset($_GET['editid'])) {
    if (isset($_SESSION['postBack']['pagename']) && $_SESSION['postBack']['pagename'] == $pagename) {
      echo '<script language="javascript">';
      echo 'if (document.paramForm) {';
      foreach ($_SESSION['postBack'] as $key=>$value) {
        if ($key == "savename" && $rid == -1) continue;
        echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
      }
      foreach ($_SESSION['postBack'] as $key=>$value) {
        if ($key == "WFNAME") {
          echo 'if (isValidKey("' . $key . '")) setFormElement("paramForm", "' . $key . '", "' . $value . '");';
        }
      }
      echo '}';
      echo '</script>';
      $_SESSION['postBack'] = Array();
    }
  }
*/
  echo '<script language="javascript">';
  echo 'if (document.paramForm) changeFert(-1,0);';
  if (!$isLoggedIn) {
    echo 'setFormState(document.paramForm, true);';
  }
  echo '</script>';
?>
