<?php
  #ini_set('session.cache_limiter', 'private');
  session_start();

  require('chart.php');

  function dateFromDoy($x) {
    while ($x > 365) {
      $x-=365;
    }
    $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");
    $days = array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
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
  if (isset($_GET['xcol'])) {
    $xcol = $_GET['xcol'];
  } else $xcol = -1;
  $ycol = $_GET['ycol'];
  $skipl = $_GET['skipl'];
  $temp = file($infile);
  $titles = preg_split("/\s+/", $temp[$skipl]);
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

  //show summary stats only
  if (isset($_GET['summary'])) {
    $smode = $_GET['summary'];
  } else $smode = 0;

  if (isset($_GET['style'])) {
    if ($_GET['style'] == 1) {
      $style="fill";
    } else if ($_GET['style'] == 2) {
      $style = "square";
    } else if ($_GET['style'] == 3) {
      $style = "bar";
    } else if ($_GET['style'] == 4) {
      $style = "hbar";
    } else $style = "lines";
  } else $style = "lines";

  if (isset($_GET['xticks'])) {
    $xticks = explode(",", $_GET['xticks']);
  }
  if (isset($_GET['xtickv'])) {
    $xtickv = explode(",", $_GET['xtickv']);
  }
  if (isset($_GET['yticks'])) {
    $yticks = explode(",", $_GET['yticks']);
  }
  if (isset($_GET['ytickv'])) {
    $ytickv = explode(",", $_GET['ytickv']);
  }
  if (isset($_GET['usedates'])) {
    $usedates = True;
  } else $usedates = False;

  if ($smode == 2) {
    if (isset($_GET['keepval'])) {
      $keepval = explode(",", $_GET['keepval']);
    } else $keepval = array();
    if (isset($_GET['yvals'])) {
      $yvals = explode(",", $_GET['yvals']);
    }
    for ($j = 0; $j < count($yvals); $j++) {
      if (array_search($j, $keepval) === False) {
	$isKeep[$j] = False;
      } else $isKeep[$j] = True;
    }
  }

  if ($smode == 3) {
    if (isset($_GET['keepval'])) {
      $keepval = explode(",", $_GET['keepval']);
    } else $keepval = array();
    if (isset($_GET['xvals'])) {
      $xvals = explode(",", $_GET['xvals']);
    }
    for ($j = 0; $j < count($xvals); $j++) {
      if (array_search($j, $keepval) === False) {
        $isKeep[$j] = False;
      } else $isKeep[$j] = True;
    }
  }

  $n = 0;
  if ($smode == 1 || $smode == 2 || $smode == 3) $n = 1;
  for ($j = $skipl+1; $j < count($temp); $j++) {
    $row = trim($temp[$j]);
    $row = preg_split("/\s+/", $row);
    if ($smode == 1) {
      if ($row[0] != 'Min' && $row[0] != 'Max' && $row[0] != 'Mean' && $row[0] != 'Median') continue;
    } else if ($smode == 2) {
      if ($row[0] == 'Mean') {
	for ($l = 0; $l < count($yvals); $l++) {
	  $xs[$n] = $n;
	  if ($isKeep[$l]) {
	    $ys[$n] = $yvals[$l];
	  } else {
	    $ys[$n] = $row[$yvals[$l]];
	    $ys[$n] *= $fac;
	    $ys[$n] += $off;
	  }
	  $n++;
	}
      }
      continue;
    } else if ($smode == 3) {
      if ($row[0] == 'Mean') {
        for ($l = 0; $l < count($xvals); $l++) {
          $ys[$n] = $n;
          if ($isKeep[$l]) {
            $xs[$n] = $xvals[$l];
          } else {
            $xs[$n] = $row[$xvals[$l]];
            $xs[$n] *= $fac;
            $xs[$n] += $off;
          }
          $n++;
        }
      }
      continue;
    }
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
  }
  if ($smode == 1) {
    $ys[0] = $ys[1] - ($ys[2]-$ys[1]); 
    $xs[0] = 0;
    //$xticks = array("0"=>"Min", "1.2"=>"Mean", "2.4"=>"Median", "3.6"=>"Max");
    $xticks = array("Min", "Mean", "Median", "Max");
    $xtickv = array(0.6, 1.6, 2.6, 3.6);
  } else if ($smode == 2) { 
    $temp = $ys;
    sort($temp);
    $ys[0] = max(0, $temp[0] - ($temp[1]-$temp[0]));
    $xs[0] = 0;
    //$xtitle = implode(" ",$xticks);
  } else if ($smode == 3) { 
    $temp = $xs;
    sort($temp);
    $xs[0] = max(0, $temp[0] - ($temp[1]-$temp[0]));
    $ys[0] = 0;
  }

  if (isset($_GET['ymin'])) {
    $ymin = $_GET['ymin'];
    if (min($ys) < $ymin) $ymin = min($ys);
  } else $ymin = array();

  if (isset($_GET['xmin'])) {
    $xmin = $_GET['xmin'];
    if (min($xs) < $xmin) $xmin = min($xs);
  } else $xmin = array();

  if ($smode == 1 || $smode == 2) {
    if ($style == "fill") { 
      //make bars reach bottom;
      $xs = array(); 
    } else if ($style == "bar") {
      if ($style == "bar") $style = "fill";
      for ($j = 0; $j < count($ys); $j++) {
        $newys[$j*2] = $ys[$j];
        $newys[$j*2+1] = 0;
      }
      $xs = array(); 
      $newys[0] = 0;
      $newys[1] = 0;
      $ys = $newys;
      $ymin = min($ys);
      //$xextrema[1] = 2*$xextrema[1]+1;
      for ($j = 0; $j < count($xtickv); $j++) {
        $xtickv[$j] = floor(2*($xtickv[$j]+$j+1))/2.;
      }
    } else $style = "square";
  }

  if ($smode == 3 && $style == "hbar") {
    for ($j = 1; $j < count($xs); $j++) {
      #$newxs[$j*2] = $xs[$j];
      #$newxs[$j*2+1] = 0;
      $newxs[$j*3-1] = $xs[$j];
      $newxs[$j*3] = $xs[$j];
      $newxs[$j*3+1] = 0;
    }
    $newxs[0] = 0;
    $newxs[1] = 0;
    $xs = $newxs;
    $ys = array();
    for ($j = 0; $j < count($xs); $j++) $ys[$j] = $j;
    $xmin = min($xs);
    for ($j = 0; $j < count($ytickv); $j++) {
      $ytickv[$j] = floor(2*($ytickv[$j]+$j+1))/2.;
    }
  }

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
      $xticks[] = dateFromDoy($day+$xoff);
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
  if (isset($ytickv)) {
    $chart->set_ytickv($ytickv);
  }
  if (isset($yticks)) {
    $chart->set_yticknames($yticks);
  }

  $chart->set_axes("xy",$plotfg);
  $chart->set_margins(45,10,20,35);
  if ($style == "hbar") {
    $chart->set_extrema($ymin, array(), $xmin, array());
    $chart->plot($ys, $xs, $plotfg, $style);
    $chart->set_margins(120,10,20,35);
  } else {
    $chart->set_extrema($ymin, array(), $xmin, array());
    $chart->plot($ys, $xs, $plotfg, $style); 
  }
  #$chart->set_title("BMP Toolbox UF-IFAS  " . date("G:i M j, Y"), $plotfg);
  if ($plotWidth == 320) {
    $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS ". date("G:i M j, Y"), $plotfg);
  } else if ($plotWidth > 320) {
    $chart->set_title("CCROP v1.0    BMPToolbox.org    UF-IFAS    ". date("G:i M j, Y"), $plotfg);
  } else $chart->set_title("CCROP v1.0 BMPToolbox.org UF-IFAS", $plotfg);
  $chart->set_labels($x=$xtitle, $y=$ytitle);
  //if ($smode == 1 || $smode == 2) $chart->set_x_ticks($xticks, "text");
  $chart->stroke();

  unset($temp);
  unset($xs);
  unset($ys);
?>
