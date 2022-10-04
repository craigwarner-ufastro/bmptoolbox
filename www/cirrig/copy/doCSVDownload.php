<?php session_start(); ?>
<?php
  if (isset($_SESSION['bmpid']) && $_SESSION['bmpid'] == $_POST['uid']) {
    if (isset($_POST['isSubmitted'])) {
      require('dbcnx.php');
      #mysql_select_db("mybmp");
      // output headers so that the file is downloaded rather than displayed
      header('Content-Type: text/csv; charset=utf-8');
      header('Content-Disposition: attachment; filename=myZones.csv');
    
      // create a file pointer connected to the output stream
      $output = fopen('php://output', 'w');

      // output the column headings
      $cols = Array();
      foreach ($_POST as $key=>$value) {
	if ($key == 'isSubmitted' or $key == 'uid' or $key == "submit" or $key == "selectCols") continue;
	$cols[] = $key;
      }
      fputcsv($output, $cols);

      $clause = 'where uid=' . $_SESSION['bmpid'];
      if ($_POST['selectCols'] != 'All' && $_POST['selectCols'] != 'None' && $_POST['selectCols'] != 'Custom') $clause .= ' AND zoneType="' . $_POST['selectCols'] . '"';

      // fetch the data
      $out = $mysqli_db->query('SELECT ' . implode(",", $cols) . ' FROM zones ' . $clause);

      // loop over the rows, outputting them
      while ($row = $out->fetch_array(MYSQLI_ASSOC)) fputcsv($output, $row); 
    }
  }
?>
