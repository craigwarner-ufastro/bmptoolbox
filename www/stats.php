<?php
   function bubblesort($x) {
      for ($j = 0; $j < count($x)-1; $j++) {
	for ($l = $j+1; $l < count($x); $l++) {
	   if ($x[$j] > $x[$l]) {
	      $temp = $x[$j];
	      $x[$j] = $x[$l];
	      $x[$l] = $temp;
	   }
	}
      }
      return $x;
   }

   function arraymean($x) {
      return array_sum($x)/count($x);
   }

   function arraymedian($x) {
      //$x = bubblesort($x);
      sort($x);
      $n = count($x);
      if ($n % 2 == 1) {
	return $x[$n/2];
      } else {
	return ($x[$n/2]+$x[$n/2-1])/2.0;
      }
   }

   function arraystddev($x) {
      $n = count($x);
      if ($n == 1) {
	return 0.0;
      }
      $m = arraymean($x);

      $allsame = True;
      foreach ($x as $key => $value) {
        if ($value != $x[0]) {
	  $allsame = False;
	}
      }
      if ($allsame) {
	return 0.0;
      }

      foreach ($x as $key => $value) {
	$xsq[$key] = $value*$value;
      }
      $std = sqrt(array_sum($xsq)*(1./($n-1)) - $m*$m*$n/($n-1));
      return $std;
   }

   function outFormat($x, $dec) {
      if ($x === NULL) return '--';
      $dpos = stripos($x, '.');
/*
      if ($dpos === False) {
	$x .= '.';
	for ($j = 0; $j < $dec; $j++) $x .= '0';
	return $x;
      }
      for ($j = strlen($x); $j < $dpos+$dec+1; $j++) $x .= '0';
*/
      if ($dec > 0) {
	$x = (float)($x)*pow(10, $dec)+0.5;
	$x = (int)($x)/pow(10, $dec); 
      } else {
	$x = (int)($x+0.5);
      }
      $x = (string)($x);
      $dpos = stripos($x, '.');
      if ($dpos === False && $dec > 0) {
        $x .= '.';
        for ($j = 0; $j < $dec; $j++) $x .= '0';
        return $x;
      } else if ($dec <= 0) return $x;
      for ($j = strlen($x); $j < $dpos+$dec+1; $j++) $x .= '0';
      return substr($x, 0, $dpos+$dec+1);
   }
?>
