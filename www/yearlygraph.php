<?php
  ini_set('session.cache_limiter', 'private');
  session_start();
  require('chart.php');

  function dateFromDoy($x, $year) {
    $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");
    $days = array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    if ($year % 4 == 0) {
      $days[1] = 29;
      if ($x > 366) {
	$x-=366;
      }
    } else {
      while ($x > 365) {
	$x-=365;
      }
    }
    $n = 0;
    while ($x > $days[$n]) {
      $x-=$days[$n];
      $n++;
    }
    $s = $months[$n] . ' ' . $x;
    return $s;
  }

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

  if (isset($_GET['file'])) {
    $infile = $_GET['file'];
  }
  if (isset($_GET['year'])) {
    $year = $_GET['year'];
  }
  if (isset($_GET['xcol'])) {
    $xcol = $_GET['xcol'];
  } else $xcol = -1;
  $ycol = $_GET['ycol'];
  $skipl = $_GET['skipl'];
  $temp = file($infile);
  $titles = preg_split("/\s+/", trim($temp[$skipl]));
  if (isset($_GET['xtitle'])) {
    $xtitle = $_GET['xtitle'];
  } else $xtitle = $titles[$xcol];
  if (isset($_GET['ytitle'])) {
    $ytitle = $_GET['ytitle'];
  } else $ytitle = $titles[$ycol];
  $xs = array();
  $ys = array();

  //second column to factor into equation
  if (isset($_GET['ycol2'])) {
    $ycol2 = $_GET['ycol2'];
    $doy2 = True;
  } else $doy2 = False;
  if (isset($_GET['op'])) {
    $op = $_GET['op'];
  } else $op = 'mult';

  //multiplicative factor
  if (isset($_GET['fac'])) {
    $fac = $_GET['fac'];
  } else $fac=1;

  //additive offset
  if (isset($_GET['off'])) {
    $off = $_GET['off'];
  } else $off=0;

  if (isset($_GET['style'])) {
    if ($_GET['style'] == 1) {
      $style="fill";
    } else if ($_GET['style'] == 2) {
      $style = "square";
    } else if ($_GET['style'] == 3) {
      $style = "bar";
    } else $style = "lines";
  } else $style = "lines";

  if (isset($_GET['xticks'])) {
    $xticks = explode(",", $_GET['xticks']);
  }
  if (isset($_GET['xtickv'])) {
    $xtickv = explode(",", $_GET['xtickv']);
  }
  if (isset($_GET['usedates'])) {
    $usedates = True;
  } else $usedates = False;

  $n = 0;
  $isYear = False;
  $j = $skipl+1;
  while (! $isYear && $j < count($temp)) {
    if (substr($temp[$j], 0, 4) == $year && substr($temp[$j], 9, 3) == "  1") $isYear = True; else $j++;
  }
  while ($isYear && $j < count($temp)) {
    if (substr($temp[$j], 0, 4) != $year && substr($temp[$j], 9, 3) == "  1") {
      $isYear = False;
      continue;
    }
    $row = trim($temp[$j]);
    $row = preg_split("/\s+/", $row);
    if ($xcol == -1) {
      $xs[$n] = $n;
    } else {
      $xs[$n] = $row[$xcol];
    }
    $ys[$n] = $row[$ycol];
    if ($doy2) {
      $ys2 = $row[$ycol2];
      if ($op == 'add') {
	$ys[$n] += $ys2;
      } else if ($op == 'sub') {
	$ys[$n] -= $ys2;
      } else if ($op == 'mult') {
	$ys[$n] *= $ys2;
      } else if ($op == 'div') {
	$ys[$n] /= $ys2;
      }
    }
    $ys[$n] *= $fac;
    $ys[$n] += $off;
    $n++;
    $j++;
  }

  if (isset($_GET['ymin'])) {
    $ymin = $_GET['ymin'];
    if (min($ys) < $ymin) $ymin = min($ys);
  } else $ymin = array();

  if ($usedates) {
    $xoff = $xs[0];
    for ($j = 0; $j < count($xs); $j++) {
      $xs[$j] = $j;
    }
    $xmax = max($xs);
    $xticks = array();
    $xtickv = array();
    $day = 0;
    $xmajor = 28;
    $xminor = 7;
    if (count($xs) < 35) {
      $xmajor = 7;
      $xminor = 1;
    } else if (count($xs) > 182) {
      $xmajor = 56;
    }
    while ($day <= $xmax) {
      $xtickv[] = $day;
      $xticks[] = dateFromDoy($day+$xoff, $year);
      $day+=$xmajor;
    }
    $xtitle = "Date";
    $chart->set_xtickinterval($xminor);
  }

  if (isset($xtickv)) {
    $chart->set_xtickv($xtickv);
  }
  if (isset($xticks)) {
    $chart->set_xticknames($xticks);
  }

  $chart->set_axes("xy",$plotfg);
  $chart->set_extrema($ymin, array(), array(), array());
  $chart->plot($ys, $xs, $plotfg, $style); 
  #$chart->set_title("BMP Toolbox UF-IFAS  " . date("G:i M j, Y"), $plotfg);
  if ($plotWidth == 320) {
    $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS ". date("G:i M j, Y"), $plotfg);
  } else if ($plotWidth > 320) {
    $chart->set_title("CCROP v1.0    BMPToolbox.org    UF-IFAS    ". date("G:i M j, Y"), $plotfg);
  } else $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS", $plotfg);
  $chart->set_labels($x=$xtitle . ' ' . $year, $y=$ytitle);
  $chart->set_margins(45,10,20,35);
  $chart->stroke();

  unset($temp);
  unset($xs);
  unset($ys);
?>
