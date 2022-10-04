<?php
      #Add economic data
      #Include function to return cost basis
      require('getCostOutput.php');
      #costs = array with keys = cost items, values = costs in $/plant
      #Loop over # of comparisons
      $costs = array();
      $rates = array();
      $quant = array();
      $units = array();
      for ($j = 0; $j < count($xplot); $j++) {
	$thisFert = false;
	if ($pagename == "fert_irr") {
	  if ($j == 0 || $j == 2) $thisFert = $_POST['FERT1']; else $thisFert = $_POST['FERT2'];
	} else if ($pagename == "fert") {
	  $thisFert = $_POST['FERT' . ($j+1)];
	}
        list($costs[],$rates[],$quant[],$units[]) = getCostOutput($pfix, $id . ($j+1), $mysqli_db, $thisFert=$thisFert);
      }
      #Setup arrays for charts of costs
      $ytitles2 = array_keys($costs[0]);
      $ytitles2[] = "Total Cost";
      $keys = array_keys($costs[0]);
      $costvals = "";
      echo '<script language="javascript">';
      for ($j = 0; $j < count($xplot); $j++) {
        echo 'costplot[' . $j . ']=new Array(' . count($ytitles2) . ');';
        for ($i = 0; $i < count($keys); $i++) {
          echo 'costplot[' . $j . '][' . $i . ']=' . $costs[$j][$keys[$i]] . ";\n";
        }
	echo 'costplot[' . $j . '][' . $i . ']=' . array_sum($costs[$j]) . ";\n";
        if ($j > 0) $costvals .= ",";
        $costvals .= array_sum($costs[$j]);
      }
      echo '</script>';

      #Setup array of titles for graphs
      $ytitles = array("Plant height (inch)", "Irrigation (inch)", "Runoff (inch)", "Runoff N (g/ container)", "N loss (% of applied)", "Runoff N conc. (ppm)", "Time to finish (weeks)", "Rain (inch)");
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
        $ytitles = array("Plant height (inch)", "Time to finish (weeks)", "Rain (inch)", "Irrigation (inch)", "Runoff (gal/ container)", "Runoff N (g/ container)", "N loss (% of applied)", "N applied (g/container)", "Runoff N conc. (ppm)", "Runoff P (g/ container)", "P loss (% of applied)", "P applied (g/container)", "Runoff P conc. (ppm)", "Final N sufficiency (0-1)");
      } else {
        $ytitles = array("Plant height (inch)", "Time to finish (weeks)", "Rain (inch)", "Irrigation (inch)", "Runoff (inch)", "Runoff N (g/ container)", "N loss (% of applied)", "N applied (g/container)", "Runoff N conc. (ppm)", "Runoff P (g/ container)", "P loss (% of applied)", "P applied (g/container)", "Runoff P conc. (ppm)", "Final N sufficiency (0-1)");
      }
      echo '<br>';
      showOutputButtons();

      #Report form
      echo '<div id="reportdiv" style="display:none;">';
        echo '<form name="reportForm" action="report.php" method="post" target="ccmt">';
        echo '<input type="hidden" name="runid" value="' . $id . '">';
        echo '<input type="hidden" name="pfix" value="' . $pfix . '">';
        echo '<input type="hidden" name="type" value="comparison">';
        echo '<input type="hidden" name="S_VOL" value="' . $subVol . '">';
        echo '<input type="hidden" name="pagename" value="' . $pagename . '">';
        echo '<center><h2>Generate a Report</h2></center>';
        echo '<div class="indent75">';
          echo '<span class="redhead">Select sections to display in report:</span><br>';
	  echo '<div class="indent25" valign="top">';
	    echo '<input type="checkbox" name="sumgraphs" onClick="setReportChecks(this.name)" checked>';
	    echo '<b>Summary Comparison Graphs:</b> ';
            echo '(<input type="checkbox" onClick="toggleReportState(\'sumgraphs\', 0,' . count($ytitles) . ', this.checked);"> All)<br>';
            echo '<div class="indent25">';
	    for ($j = 0; $j < count($ytitles); $j++) {
	      echo '<input id="sumgraphs_' . $j . '" type="checkbox" name="sumgraphs_' . $j . '" onClick="checkReport(\'sumgraphs\', 30, this, \'' . $id . '\', \'' . $ytitles[$j] . '\');" onMouseOver="showToolTipGraph(\'sumgraphs\', 30, this, \'' . $id . '\', \'' . $ytitles[$j] . '\');" onMouseOut="hideddrivetip();">';
	      echo $ytitles[$j] . '<br>';
	    }
	    echo '</div>';
            echo '<input type="checkbox" name="sumtables" onClick="setReportChecks(this.name)" checked>';
	    echo '<b>Summary Table</b><br>';
?>
	  </div>
	</div>
	<center>
	<input type="Submit" name="Submit" value="Submit">
	<input type="Reset" name="Reset" value="Reset">
	<input type="Button" name="Hide Report" value="Hide Report" onClick="javascript:hideReport();">
	</center>
	</form>
      </div>
<?php

      echo '<br><b>Inputs:</b><div class="indent75">';
      if (strpos($pagename, "location") === false) echo 'Location: ' . str_replace('_', ' ', $wfname) . '<br>';
      if (strpos($pagename, "date") === false) echo 'Plant Date: ' . dateFromDoy($pltdoy) . '<br>';
      if (strpos($pagename, "irr") === false) echo 'Irrigation: ' . ($sched == "MAD" ? "ET-based" : "Fixed") . '<br>';
      if (strpos($pagename, "fert") === false) echo 'Fertilizer Rate: ' . outFormat($fert*$pctn/($subVol*0.059325)+0.005, 2) . ' lb N per cubic yard<br>';
      echo '</div><br>';

      echo "<b>Select type of output:</b><ul>";

      echo '<form name="graphForm">';
      echo '<input type="radio" name="outtype" value="summarygraphs" onClick="selectCompOutputDiv(\'summarygraphs\');" checked><span id="summarygraphsspan" style="color: red; font-weight: bold">Summary Graphs</span><br>';
      echo '<input type="radio" name="outtype" value="summarytables" onClick="selectCompOutputDiv(\'summarytables\');"><span id="summarytablesspan" style="font-weight: bold">Summary Tables</span><br>';
      echo '<input type="radio" name="outtype" value="viewfiles" onClick="selectCompOutputDiv(\'viewfiles\');"><span id="viewfilesspan" style="font-weight: bold">View Output Files</span><br>';
      echo '</ul>';

      echo '<div id="viewfiles" class="indent75" style="display:none">';
      //list output files
      $outfiles = Array();
      $dataDir = substr($pfix, 0, strrpos($pfix, '/'));
      $f = opendir($dataDir);
      while ($temp = readdir($f)) {
	if (preg_match("/{$id}/i", $temp) && !preg_match("/run/i", $temp)) {
        #if (eregi($id, $temp) && !eregi('run', $temp)) {
           $outfiles[] = $temp;
        }
      }
      sort($outfiles);
      for ($j = 0; $j < count($outfiles); $j++) {
	if ($pfix == "data/i") {
	  $outname = substr($outfiles[$j], strpos($outfiles[$j], '_')-1);
          if (strpos($outname, 'inputs.txt') !== false or strpos($outname, 'table.csv') !== false) {
            $outname = substr($outname, 2);
          }
	} else $outname = $outfiles[$j];
        echo '<a href="' . $dataDir . '/' . $outfiles[$j] . '" target="ccmt">' . $outname .  '</a><br>';
      }
      //echo '<br>';
      //$wthfile = $_POST['WFNAME'] . '.wth';
      //echo '<a href="' . $wthfile . '">' . $wthfile . '</a><br>';
      echo '<br>';
      echo '<a href="Notes_input.txt" target="ccmt">Notes on inputs</a><br>';
      echo '<a href="Notes_output.txt" target="ccmt">Notes on outputs</a><br>';
      echo '</div>';

      echo '<div id="summarygraphs">';
      #show summary graphs
      echo '<br><b>Chart 1 - Summary comparison graphs:</b><br>';
      echo '<table border=0>';
      echo '<tr><td>Graphs of ' . $tablelabel . ' comparisons<br>';
      echo '<select name="varGraph1" onChange="updateGraph(\'imgGraph1\', 0, this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($ytitles); $j++) {
        echo '<option name="' . $ytitles[$j] . '" value="' . $ytitles[$j] . '">' . $ytitles[$j];
      }
      echo '</select></td>';

      #economic output
      echo '<td width=20></td>';
      echo '<td>Chart 2 -Economic data<br>';
      echo '<select name="varGraph2" onChange="updateGraph(\'imgGraph2\', 1, this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($ytitles2); $j++) {
        echo '<option name="' . $ytitles2[$j] . '" value="' . $ytitles2[$j] . ' ($/container)"';
	if ($j == count($ytitles2)-1) echo " selected";
	echo '>' . $ytitles2[$j];
      }
      echo '</select></td>';
      echo '</tr>';

      echo '<tr><td>';
      $xticks = $xlabels[0]; 
      for ($j = 1; $j < $nruns; $j++) {
        $xticks .= ',' . $xlabels[$j];
      }
      $xvals = $xplot[0];
      $yvals = $yplot[0][0];
      for ($j = 1; $j < count($xplot); $j++) {
	$xvals .= ',' . $xplot[$j];
	$yvals .= ',' . $yplot[$j][0];
      } 
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") === false) {
	$xextrema = '' . (min($xplot)-0.2) . ',' . (max($xplot)+0.2);
	$xticks = '';
	$xtickv = '';
      } else {
	$xticks = '&xticks=' . $xticks;
	$xtickv = '&xtickv=' . $xtickv;
      }
      echo '<script language="javascript">';
      for ($j = 0; $j < count($xplot); $j++) {
	echo 'xplot[' . $j . ']=' . $xplot[$j] . ";\n";
	echo 'yplot[' . $j . ']=new Array(' . count($yplot[$j]) . ');';
	for ($l = 0; $l < count($yplot[$j]); $l++) {
	  echo 'yplot[' . $j . '][' . $l . ']=' . $yplot[$j][$l] . ";\n";
	}
      }
      echo "xticks='" . $xticks . "';\n";
      echo 'var xtickv="' . $xtickv . '";';
      echo 'var xextrema="' . $xextrema . '";';
      echo '</script>';
      echo '<img name="imgGraph1" id="imgGraph1" src="compgraph.php?&xvals=' . $xvals . '&yvals=' . $yvals . $xticks . $xtickv . '&xextrema=' . $xextrema . '&ymin=0&style=' . $style . '&xtitle=' . $xtitle . '&ytitle=' . $ytitles[0] . '">';
      if ($pagename == "fert_irr") echo '<br><span class="regreq"><i>See Summary Table to identify run number.</i></span>';
      echo '</td>';

      #economic output
      echo '<td></td>';
      echo '<td>';
      echo '<img name="imgGraph2" id="imgGraph2" src="compgraph.php?&xvals=' . $xvals . '&yvals=' . $costvals . $xticks . $xtickv . '&xextrema=' . $xextrema . '&ymin=0&style=' . $style . '&xtitle=' . $xtitle . '&ytitle=Total Cost ($/container)">';
      echo '</td></tr>';


      echo '</tr></table>';
      echo '</div>';
      echo '<div id="summarytables" style="display:none">';
      echo '<br><b>Summary tables:</b><br>';
      //show summary table
      echo 'Table 1 - Summary output for ' . $tablelabel .  ' comparisons<br>';
      echo '<a href="' . $pfix . $id . '_table.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
      echo '<table border=1 width=720><tr>';
      echo '<th valign="top" class="comparehead">' . $xtitle . '</td>';
      $f = fopen($pfix . $id . "_table.csv", "wb");
      fwrite($f, $xtitle);
      for ($i = 0; $i < count($xplot); $i++) {
	echo '<th align="right" valign="top" class="comparehead">' . $xlabels[$i] . '</td>';
	fwrite($f, "," . $xlabels[$i]);
      }
      echo '</tr>';
      fwrite($f, "\n");
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") != false) {
	echo '<tr><td valign="top">Fertilizer rate (lb N/cu yd)</td>';
	fwrite($f, "Fertilizer rate (lb N/cu yd)");
	for ($i = 0; $i < count($xplot); $i++) {
	  echo '<td align="right">' . outFormat($fertplot[$i], 2) . '</td>';
          fwrite($f, "," . outFormat($fertplot[$i], 2));
	}
	echo '</tr>';
	fwrite($f, "\n");
        echo '<tr><td valign="top">Irrigation schedule</td>';
	fwrite($f, "Irrigation schedule");
        for ($i = 0; $i < count($xplot); $i++) {
          echo '<td align="right">';
          if ($i > 1) echo 'ET-based'; else echo 'Fixed';
          echo '</td>';
          if ($i > 1) fwrite($f, ",ET-based"); else fwrite($f, ",Fixed");
	}
	echo '</tr>';
	fwrite($f, "\n");
      }
      for ($j = 0; $j < count($ytitles); $j++) {
        $td = '<td '; 
        if ($j % 2 == 1) $td = '<td class="outputtdalt" ';
	echo '<tr>' . $td . ' valign="top">' . $ytitles[$j] . '</td>';
	fwrite($f, $ytitles[$j]);
	for ($i = 0; $i < count($xplot); $i++) {
	  echo $td . ' align="right">' . outFormat($yplot[$i][$j], 2) . '</td>';
	  //echo '<td align="right">' . outFormat($yplot[$i][$j], 2) . '</td>';
	  fwrite($f, "," .  outFormat($yplot[$i][$j], 2));
	}
        echo '</tr>';
        fwrite($f, "\n");
      }
/*
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") != false) {
	echo '<td valign="top">Fertilizer rate (lb N/cu yd)</td>';
	echo '<td valign="top">Irrigation schedule</td>';
	fwrite($f, ",Fertilizer rate (lb N/cu yd),Irrigation schedule");
      }
      for ($j = 0; $j < count($ytitles); $j++) {
        echo '<td valign="top">' . $ytitles[$j] . '</td>';
        fwrite($f, "," . $ytitles[$j]);
      }
      echo '</tr>';
      fwrite($f, "\n");
      for ($i = 0; $i < count($xplot); $i++) {
        echo '<tr><td align="right">';
	echo $xlabels[$i]; 
        echo '</td>';
	fwrite($f, $xlabels[$i]); 
	if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") != false) {
	  echo '<td align="right">' . outFormat($fertplot[$i], 2) . '</td>';
          echo '<td align="right">';
          if ($i > 1) echo 'ET-based'; else echo 'Fixed';
          echo '</td>';
          fwrite($f, "," . outFormat($fertplot[$i], 2) . ",");
          if ($i > 1) fwrite($f, "ET-based"); else fwrite($f, "Fixed");
	}
	for ($j = 0; $j < count($ytitles); $j++) {
	   echo '<td align="right">' . outFormat($yplot[$i][$j], 2) . '</td>';
           fwrite($f, "," .  outFormat($yplot[$i][$j], 2));
	} 
	echo '</tr>';
        fwrite($f, "\n");
      }
*/
      fclose($f);
      echo '</table><br>';

      #add economic data
      echo 'Table 2 - Average production costs<br>';
      echo '<a href="' . $pfix . $id . '_table2.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
      echo '<table border=1 width=720><tr>';
      echo '<th valign="top" class="comparehead">' . $xtitle . '</td>';
      $f = fopen($pfix . $id . "_table2.csv", "wb");
      fwrite($f, $xtitle);
      for ($i = 0; $i < count($xplot); $i++) {
        echo '<th align="right" valign="top" class="comparehead">' . $xlabels[$i] . '</td>';
        fwrite($f, "," . $xlabels[$i]);
      }
      echo '</tr>';
      fwrite($f, "\n");
      if (strpos($pagename, "fert") !== false && strpos($pagename, "irr") != false) {
        echo '<tr><td valign="top">Fertilizer rate (lb N/cu yd)</td>';
        fwrite($f, "Fertilizer rate (lb N/cu yd)");
        for ($i = 0; $i < count($xplot); $i++) {
          echo '<td align="right">' . outFormat($fertplot[$i], 2) . '</td>';
          fwrite($f, "," . outFormat($fertplot[$i], 2));
        }
        echo '</tr>';
        fwrite($f, "\n");
        echo '<tr><td valign="top">Irrigation schedule</td>';
        fwrite($f, "Irrigation schedule");
        for ($i = 0; $i < count($xplot); $i++) {
          echo '<td align="right">';
          if ($i > 1) echo 'ET-based'; else echo 'Fixed';
          echo '</td>';
          if ($i > 1) fwrite($f, ",ET-based"); else fwrite($f, ",Fixed");
        }
        echo '</tr>';
        fwrite($f, "\n");
      }
      $keys = array_keys($costs[0]);
      for ($j = 0; $j < count($keys); $j++) {
        echo '<tr>';
        $td = '<td align="right">';
	if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
	$td0 = '<td align="left">';
	if ($j % 2 == 0) $td0 = '<td align="left" class="outputtdalt">';
	echo $td0 . $keys[$j] . '</td>';
        fwrite($f, $keys[$j]);
	for ($i = 0; $i < count($xplot); $i++) {
	  echo $td . outFormat($costs[$i][$keys[$j]], 3) . '</td>'; 
          fwrite($f, "," . outFormat($costs[$i][$keys[$j]], 3));
	}
	echo '</tr>';
	fwrite($f, "\n");
      }
      $j++;
      echo '<tr><th align="left"';
      if ($j % 2 == 0) echo ' class="outputtdalt"';
      echo '>TOTAL</th>';
      $td = '<td align="right">';
      if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
      fwrite($f, "TOTAL");
      for ($i = 0; $i < count($xplot); $i++) {
	$totalCost = array_sum($costs[$i]);
	echo $td . outFormat($totalCost, 3) . '</td>';
        fwrite($f, "," . outFormat($totalCost, 3));
      }
      echo '</tr>';
      fclose($f);
      echo '</table><br>';

      echo '</div>';
      echo '<br><br>';
      showOutputButtons();
?>
