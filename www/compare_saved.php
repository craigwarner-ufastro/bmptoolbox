<?php
      for ($n = 0; $n < $nruns; $n++) {
        if (strpos($pagename, "fert") !== false) $fert = $compvals[0][$n];

        $yearlyfiles = array("summaryoutput.txt", "summaryarea.txt");
        for ($i = 0; $i < count($yearlyfiles); $i++) {
           //read file and parse into arrays
           $yearly = file($pfix . $id . ($n+1) . "_" . $yearlyfiles[$i]);
           $temp = trim($yearly[count($yearly)-4]);
           $temp = preg_split("/\s+/", $temp);
           for ($k = 1; $k < count($temp); $k++) {
              $ymean[$k-1] = $temp[$k];
           }

           if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
              $idx = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15);
           } else {
              $idx = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
           }

           //calculate vals to plot
           if ($i == 0) {
              $xplot[$n] = ($n+1);
              if (strpos($pagename, "fert") !== false) {
                if (strpos($pagename, "irr") !== false) {
                  $fertplot[$n] = $fert*$pctn*16.86/$subVol;
                } else {
                  #comparion_fert
                  #Runoff (gal/container)
                  $xplot[$n] = $fert*$pctn*16.86/$subVol;
                  $yplot[$n][$idx[4]] = $ymean[6]*0.264; #Runoff (gal/ container)
                }
              }
              $yplot[$n][$idx[0]] = $ymean[9]/2.54; #Plant height (in)
              $yplot[$n][$idx[1]] = $ymean[1]/7; #Time to finish (weeks)
              $yplot[$n][$idx[5]] = $ymean[7]; #Runoff N (g/ container)
              $yplot[$n][$idx[6]] = $ymean[7]*100/($fert*$pctn/100.); #Nloss (%) 
              $yplot[$n][$idx[7]] = $fert*$pctn/100.; #N rate (g N/ container)
              $yplot[$n][$idx[8]] = $ymean[8]; #Runoff N conc.
              $yplot[$n][$idx[9]] = $ymean[13]; #Runoff P (g/ container)
              $yplot[$n][$idx[10]] = $ymean[13]*100/($fert*$pctp/100.); #Ploss (%) 
              $yplot[$n][$idx[11]] = $fert*$pctp/100.; #P rate (g N/ container)
              $yplot[$n][$idx[12]] = $ymean[14]; #Runoff P conc.
              $yplot[$n][$idx[13]] = $ymean[3]*0.264*$irrcost; #Irrig cost
              $yplot[$n][$idx[14]] = $ymean[16]; #Final NSUF

              if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
                #comparison_fert
                $yplot[$n][13] = $fert*$pctn/100.*$fertcost*2.205; #Fert cost
              }
           } else if ($i == 1) {
              $yplot[$n][$idx[3]] = $ymean[3]/2.54; #Irrig (in)
              if (strpos($pagename, "fert") === false || strpos($pagename, "irr") !== false) {
                #all but comparison_fert
                $yplot[$n][$idx[4]] = $ymean[6]/2.54; #Runoff (in)
              }
              $yplot[$n][$idx[2]] = $ymean[2]/2.54; #Rain (in)
           }
        }
      }
?>
