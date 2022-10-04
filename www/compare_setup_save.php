<?php
      $id = $_GET['runid'];
      $pfix = "data/" . $bmpusername . "/";
      $nruns = 0;
      if (file_exists($pfix . $id . '_inputs.txt')) {
	$inpfl = file($pfix . $id . '_inputs.txt');
	for ($l = 0; $l < count($inpfl); $l++) {
	  $temp = trim($inpfl[$l]);
	  $tempvals = preg_split("/\s+/", $temp);
	  if (strpos($tempvals[0], "cost") !== false) {
	    $input[$tempvals[0]] = $tempvals[1];
	  }
	}
      }
      for ($j = 0; $j < 4; $j++) {
        if (file_exists($pfix . $id . ($j+1) . '_P_spec.txt')) {
          $nruns+=1;
          $spec = file($pfix . $id . ($j+1) . '_P_spec.txt');
          for ($l = 0; $l < $npspec; $l++) {
            $temp = trim($spec[$l*4+1]);
            $tempnames = preg_split("/\s+/", $temp);
            $temp = trim($spec[$l*4+2]);
            $tempvals = preg_split("/\s+/", $temp);
            for ($i = 0; $i < count($tempnames); $i++) {
              $x = array_search($tempnames[$i], $compnames);
              if ($x === false) {
                $input[$tempnames[$i]] = $tempvals[$i];
              } else {
                $input[$tempnames[$i] . ($j+1)] = $tempvals[$i];
              }
            }
          }
        }
      }
      $runfile = "data/run" . $id . ".txt";
      if ($nruns == 4) {
        $xtickv='0.6,1.6,2.6,3.6';
        $xextrema='0,4';
      } else if ($nruns == 3) {
        $xtickv='0.6,1.6,2.6';
        $xextrema='0,3';
      } else {
        $xtickv='0.6,1.6';
        $xextrema='0,2';
      }

      $style = 3;
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) $style=0;
      if (strpos($pagename, "fert") === false) $fert = $input['FERT'];
      $pctn = $input['PCT_N'];
      $pctp = $input['PCT_P'];
      #$irrcost = $input['irrcost']/100.;
      #if ($pagename == "fert") $fertcost = $input['fertcost'];
      if (strpos($pagename, "location") === false) $wfname = $input['WFNAME'];
      if (strpos($pagename, "date") === false) $pltdoy = $input['PLT_DOY'];
      if (strpos($pagename, "irr") === false) $sched = $input['SCHED'];
?>
