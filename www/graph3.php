<?php
  require('chart.php');
  $test = array(3,4,5);

$chart = new chart(300, 200, "example1");
$chart->plot($test);
$chart->stroke();
?>
