<?php
  #ini_set('session.cache_limiter', 'private');
  session_start();

  require('chart.php');
  $plotWidth=320;
  $plotHeight=240;
  if (isset($_SESSION['pw'])) $plotWidth = $_SESSION['pw'];
  if (isset($_SESSION['ph'])) $plotHeight = $_SESSION['ph'];
  if (isset($_GET['pw'])) $plotWidth = $_GET['pw'];
  if (isset($_GET['ph'])) $plotHeight = $_GET['ph'];
  $chart = new chart($plotWidth, $plotHeight);

  $plotbg = "white";
  if (isset($_SESSION['bg'])) $plotbg = $_SESSION['bg'];
  if (isset($_GET['bg'])) $plotbg = $_GET['bg'];
  $chart->set_background_color($plotbg, $plotbg);

  $plotfg = "black";
  if (isset($_SESSION['fg'])) $plotfg = $_SESSION['fg'];
  if (isset($_GET['fg'])) $plotfg = $_GET['fg'];

  if (isset($_GET['xtitle'])) {
    $xtitle = $_GET['xtitle'];
  } else $xtitle = ""; 
  if (isset($_GET['ytitle'])) {
    $ytitle = $_GET['ytitle'];
  } else $ytitle = ""; 
  $xs = array();
  $ys = array();

  if (isset($_GET['yvals'])) {
    $ys = explode(",", $_GET['yvals']);
  } 
  if (isset($_GET['xvals'])) { 
    $xs = explode(",", $_GET['xvals']);
  } 

  if (isset($_GET['xticks'])) {
    $xticks = explode(",", $_GET['xticks']);
  }
  if (isset($_GET['xtickv'])) {
    $xtickv = explode(",", $_GET['xtickv']);
  }
  if (isset($_GET['xextrema'])) {
    $xextrema = explode(",", $_GET['xextrema']);
  } 
  if (isset($_GET['ymin'])) {
    $ymin = $_GET['ymin'];
    if (min($ys) < $ymin) $ymin = min($ys);
  } else $ymin = array();
  if (isset($_GET['style'])) {
    if ($_GET['style'] == 1) {
      $style="fill";
    } else if ($_GET['style'] == 2) {
      $style = "square";
    } else if ($_GET['style'] == 3) {
      $style = "bar";
    } else $style = "lines";
  } else $style = "lines";


  //sort by xs
  for ($j = 0; $j < count($xs)-1; $j++) {
    for ($l = $j+1; $l < count($xs); $l++) {
      if ($xs[$j] > $xs[$l]) {
	$temp = $xs[$j];
	$xs[$j] = $xs[$l];
	$xs[$l] = $temp;
	$temp = $ys[$j];
	$ys[$j] = $ys[$l];
	$ys[$l] = $temp;
      }
    }
  }

  if ($style == "fill" or $style == "square") {
    for ($j = count($ys)-1; $j >=0; $j--) {
      $xs[$j+1] = $xs[$j];
      $ys[$j+1] = $ys[$j];
    }
    $ys[0] = 0; 
    $xs[0] = 0;
    $xs = array(); 
  } else if ($style == "bar") {
    for ($j = 0; $j < count($ys); $j++) {
      $newys[$j*2+2] = $ys[$j];
      $newys[$j*2+3] = 0;
    }
    $xs = array(); 
    $newys[0] = 0;
    $newys[1] = 0;
    $ys = $newys;
    $xextrema[1] = 2*$xextrema[1]+1;
    for ($j = 0; $j < count($xtickv); $j++) {
      $xtickv[$j] = floor(2*($xtickv[$j]+$j+1))/2.;
    }
  } 
  if ($style == "bar") $style = "fill";

  if (isset($xtickv)) $chart->set_xtickv($xtickv);
  if (isset($xticks)) $chart->set_xticknames($xticks);
  if (isset($xextrema)) $chart->set_extrema($ymin, array(), $xextrema[0], $xextrema[1]); 
  else $chart->set_extrema($ymin, array(), array(), array());

  $chart->set_axes("xy",$plotfg);
  $chart->plot($ys, $xs, $plotfg, $style); 
  #$chart->set_title("BMP Toolbox UF-IFAS  " . date("G:i M j, Y"), $plotfg);
  if ($plotWidth == 320) {
    $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS ". date("G:i M j, Y"), $plotfg);
  } else if ($plotWidth > 320) {
    $chart->set_title("CCROP v1.0    BMPToolbox.org    UF-IFAS    ". date("G:i M j, Y"), $plotfg);
  } else $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS", $plotfg);
  $chart->set_labels($x=$xtitle, $y=$ytitle);
  $chart->set_margins(50,10,20,35);
  if (max($ys) < 0.1) {
    $chart->set_margins(60,10,20,35);
  }
  if ($style == "lines") {
    //overplot with boxes
    $chart->plot($ys, $xs, $plotfg, "box");
  }
  //if ($smode == 1 || $smode == 2) $chart->set_x_ticks($xticks, "text");
  $chart->stroke();

  unset($xs);
  unset($ys);
?>
