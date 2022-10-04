<?php
      if (strpos($pagename, "realtime") === false) {
        #Only add economic data for non-real time
        #Include function to return cost basis
        require('getCostOutput.php');
        #costs = array with keys = cost items, values = costs in $/plant
        list($costs,$rates,$quant,$units) = getCostOutput($pfix, $id, $mysqli_db);
      }

      #Setup arrays of titles for graphs and charts
      $ytitles1 = array("Daily maximum air temperature (F)", "Daily minimum air temperature (F)", "Leaf area (cm2)", "Leaf area index (cm2/cm2)", "N Release (mg/day)", "Plant height (inch)", "Plant N demand (mg/plant)", "Plant N supply (mg/plant)", "Plant N uptake (mg/plant)", "Shoot biomass (g/plant)", "Shoot N concentration (%)", "Max Solar radiation (MJ/day)", "Mean Solar radiation (MJ/day)" , "P release (mg/day)" , "Plant P demand (mg/plant)" , "Plant P supply (mg/plant)" , "Plant P uptake (mg/plant)" , "Shoot P concentration (%)" , "Water sufficiency (0-1)" , "N sufficiency (0-1)" , "P sufficiency (0-1)");
      $ytitles2 = array("Container ET (inch/container)", "Drainage (inch)", "ET (inch)", "Interception factor", "Mean Ir (inch)", "Max Ir (inch)", "Runoff N (mg/container)", "Rain (inch)", "Runoff (inch)", "Runoff N concentration (mg/L)", "Runoff P (mg/container)", "Runoff P concentration (mg/L)");
      $ytitles3 = array("Cumulative N release (mg)", "Daily N Release (mg)", "Unreleased fertilizer N (mg/container)", "Cumulative P release (mg)", "Daily P Release (mg)", "Unreleased fertilizer P (mg/container)");
      $sumytitles1 = array("Time to finish (weeks)", "Rain (gal/container)", "Irrigation (gal/container)", "ET (gal/container)", "Drainage (gal/container)", "Runoff (gal/container)", "Runoff N (mg/container)", "N loss (% of applied)", "Avg. area (sq ft/100 containers)", "Runoff P (mg/container)" , "P loss (% of applied)", "Final N sufficiency (0-1)");
      $sumytitles2 = array("Rain (inch)", "Irrigation water (inch)", "ET (inch)", "Container drainage (inch)", "Runoff (inch)", "Runoff N (lb/acre)", "Runoff P (lb/acre)", "Runoff N conc (ppm)", "Runoff P conc (ppm)");
      $sumytitles4 = array("Nitrogen Efficiency", "Phosphorus Efficiency");
      $ny1 = count($ytitles1);
      $ny2 = $ny1+count($ytitles2);
      $ny3 = $ny2+count($ytitles3);
      $nsy1 = count($sumytitles1);
      $nsy2 = $nsy1+count($sumytitles2);
      $nsy4 = $nsy2+count($sumytitles4)+1;

      #Report form
      echo '<div id="reportdiv" style="display:none;">';
        echo '<form name="reportForm" action="report.php" method="post" target="ccmt">';
        echo '<input type="hidden" name="runid" value="' . $id . '">';
        echo '<input type="hidden" name="pfix" value="' . $pfix . '">';
        echo '<input type="hidden" name="type" value="' . $pagename . '">';
        echo '<input type="hidden" name="S_VOL" value="' . $subVol . '">';
        echo '<input type="hidden" name="pagename" value="' . $pagename . '">';
	if (strpos($pagename, "realtime") !== false) {
          echo '<input type="hidden" name="todayIr" value="' . $todayIr . '">';
          echo '<input type="hidden" name="todayDate" value="' . $todayDate . '">';
	  echo '<input type="hidden" name="location" value="' . str_replace('_', ' ', $wfname) . '">'; 
	  echo '<input type="hidden" name="irrig" value="' . ($sched == "MAD" ? "ET-based" : "Fixed") . '">';
	}
        echo '<center><h2>Generate a Report</h2></center>';
        echo '<div class="indent75">';
          echo '<span class="redhead">Select sections to display in report:</span><br>';
          echo '<div class="indent25" valign="top">';
            echo '<input type="checkbox" name="timeplots" onClick="setReportChecks(this.name)" checked>';
            echo '<b>Daily Time Plots</b><br>';
            echo '<div class="indent25">';
	      echo 'Plant-related responses: ';
	      echo '(<input type="checkbox" onClick="toggleReportState(\'timeplots\', 0,' . $ny1 . ', this.checked);"> All)<br>'; 
              echo '<div class="indent25">';
	      $i = 0;
              for ($j = 0; $j < count($ytitles1); $j++) {
                echo '<input id="timeplots_' . $i . '" type="checkbox" name="timeplots_' . $i . '" onClick="checkReport(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles1[$j] . '\');" onMouseOver="showToolTipGraph(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles1[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $ytitles1[$j] . '<br>';
	        $i++;
              }
              echo '</div>';
              echo 'Water-related responses: ';
              echo '(<input type="checkbox" onClick="toggleReportState(\'timeplots\', ' . $ny1 . ', ' . $ny2 . ', this.checked);"> All)<br>';
              echo '<div class="indent25">';
              for ($j = 0; $j < count($ytitles2); $j++) {
                echo '<input id="timeplots_' . $i . '" type="checkbox" name="timeplots_' . $i . '" onClick="checkReport(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles2[$j] . '\');" onMouseOver="showToolTipGraph(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles2[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $ytitles2[$j] . '<br>';
		$i++;
              }
              echo '</div>';
              echo 'Nutrient Release: ';
              echo '(<input type="checkbox" onClick="toggleReportState(\'timeplots\', ' . $ny2 . ', ' . $ny3 . ', this.checked);"> All)<br>';
              echo '<div class="indent25">';
              for ($j = 0; $j < count($ytitles3); $j++) {
                echo '<input id="timeplots_' . $i . '" type="checkbox" name="timeplots_' . $i . '" onClick="checkReport(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles3[$j] . '\');" onMouseOver="showToolTipGraph(\'timeplots\', 30, this, \'' . $id . '\', \'' . $ytitles3[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $ytitles3[$j] . '<br>';
                $i++;
              }
              echo '</div>';
            echo '</div>';

            echo '<input type="checkbox" name="sumcharts" onClick="setReportChecks(this.name)" checked>';
            echo '<b>Summary Bar Charts</b><br>';
            echo '<div class="indent25">';
              echo 'Summary output on a per-container basis: ';
              echo '(<input type="checkbox" onClick="toggleReportState(\'sumcharts\', 0,' . $nsy1 . ', this.checked);"> All)<br>';
              echo '<div class="indent25">';
              $i = 0;
              for ($j = 0; $j < count($sumytitles1); $j++) {
                echo '<input id="sumcharts_' . $i . '" type="checkbox" name="sumcharts_' . $i . '" onClick="checkReport(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles1[$j] . '\');" onMouseOver="showToolTipGraph(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles1[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $sumytitles1[$j] . '<br>';
                $i++;
              }
              echo '</div>';
              echo 'Summary output on a per-area basis: ';
              echo '(<input type="checkbox" onClick="toggleReportState(\'sumcharts\', ' . $nsy1 . ', ' . $nsy2 . ', this.checked);"> All)<br>';
              echo '<div class="indent25">';
              for ($j = 0; $j < count($sumytitles2); $j++) {
                echo '<input id="sumcharts_' . $i . '" type="checkbox" name="sumcharts_' . $i . '" onClick="checkReport(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles2[$j] . '\');" onMouseOver="showToolTipGraph(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles2[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $sumytitles2[$j] . '<br>';
                $i++;
              }
              echo '</div>';
	      echo '<input id="sumcharts_' . $i . '" type="checkbox" name="sumcharts_' . $i . '" onClick="checkReport(\'sumcharts\', 30, this, \'' . $id . '\', \'Water Efficiency\');" onMouseOver="showToolTipGraph(\'sumcharts\', 30, this, \'' . $id . '\', \'Water Efficiency\');" onMouseOut="hideddrivetip();">';
	      echo 'Water Efficiency<br>';
	      $i++;
              echo 'Nutrient Efficiency: ';
              echo '(<input type="checkbox" onClick="toggleReportState(\'sumcharts\', ' . ($nsy2+1) . ', ' . $nsy4 . ', this.checked);"> All)<br>';
              echo '<div class="indent25">';
              for ($j = 0; $j < count($sumytitles4); $j++) {
		echo '<input id="sumcharts_' . $i . '" type="checkbox" name="sumcharts_' . $i . '" onClick="checkReport(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles4[$j] . '\');" onMouseOver="showToolTipGraph(\'sumcharts\', 30, this, \'' . $id . '\', \'' . $sumytitles4[$j] . '\');" onMouseOut="hideddrivetip();">';
                echo $sumytitles4[$j] . '<br>';
                $i++;
              }
              echo '</div>';
            echo '</div>';

            echo '<input type="checkbox" name="sumtables" onClick="setReportChecks(this.name)" checked>';
            echo '<b>Summary Tables</b><br>';
            echo '<div class="indent25">';
	      $i = 0;
              echo '<input id="sumtables_' . $i . '" type="checkbox" value="Summary output on per-container basis<br>" name="sumtables_' . $i . '">';
              echo 'Summary output on per-container basis<br>';
              $i++;
              echo '<input id="sumtables_' . $i . '" type="checkbox" value="Summary output on per-area basis<br>" name="sumtables_' . $i . '">';
              echo 'Summary output on per-area basis<br>';
            echo '</div>';
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
      echo 'Location: ' . str_replace('_', ' ', $wfname) . '<br>';
      echo 'Plant Date: ' . dateFromDoy($pltdoy) . '<br>';
      echo 'Irrigation: ' . ($sched == "MAD" ? "ET-based" : "Fixed") . '<br>';
      echo 'Fertilizer Rate: ' . outFormat($fert*$pctn/($subVol*0.059325)+0.005, 2) . ' lb N per cubic yard<br>';
      echo '</div><br>';

      echo "<b>Select type of output:</b><ul>";

      echo '<form name="graphForm">';
      echo '<input type="hidden" name="gFert" value=' . $fert . '>';
      echo '<input type="hidden" name="gPctn" value=' . $pctn . '>';
      $fertn = $fert*$pctn/100;
      echo '<input type="hidden" name="gPctP" value=' . $pctp . '>';
      $fertp = $fert*$pctp/100;
      echo '<input type="radio" name="outtype" value="dailygraphs" onClick="selectOutputDiv(\'dailygraphs\');" checked><span id="dailygraphsspan" style="color: red; font-weight: bold">Time Plots</span><br>';
      echo '<input type="radio" name="outtype" value="summarycharts" onClick="selectOutputDiv(\'summarycharts\');"><span id="summarychartsspan" style="font-weight: bold">Summary Charts</span><br>';
      echo '<input type="radio" name="outtype" value="summarytables" onClick="selectOutputDiv(\'summarytables\');"><span id="summarytablesspan" style="font-weight: bold">Summary Tables</span><br>';
      echo '<input type="radio" name="outtype" value="viewfiles" onClick="selectOutputDiv(\'viewfiles\');"><span id="viewfilesspan" style="font-weight: bold">View Output Files</span><br>';
      echo '</ul>';

      echo '<div id="dailygraphs">';
      #show daily graphs
      echo '<br><b>Daily graphs:</b><br><br>Select an individual year or the mean of all years to display ';
      echo '<select name="yearPlot" onChange="updateAllGraphs(\'' . $id . '\');"><option name="mean" value="mean">Mean';
      for ($j = $startYr; $j <= $endYr; $j++) {
	if (strtotime(dateFromDoy($pltdoy) . " " . $j) < strtotime("now")) {
          echo '<option name=' . $j . ' value=' . $j . '>' . $j;
	}
      }
      echo '</select>';
      echo '<br><br>';
      echo '<table border=0>';
      echo '<tr><td>Graph 1 - Plant-related responses<br>';
      echo '<select name="varGraph1" onChange="updateGraph(\'imgGraph1\', 0, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($ytitles1); $j++) {
        echo '<option name="' . $ytitles1[$j] . '" value="' . $ytitles1[$j] . '">' . $ytitles1[$j];
      }
      echo '</select></td>';
      echo '<td width=20></td>';
      echo '<td>Graph 2 - Water-related responses<br>';
      echo '<select name="varGraph2" onChange="updateGraph(\'imgGraph2\', 1, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($ytitles2); $j++) {
        echo '<option name="' . $ytitles2[$j] . '" value="' . $ytitles2[$j] . '">' . $ytitles2[$j];
      }
      echo '</select></td>';
      echo '</tr>';

      echo '<tr><td>';
      echo '<img name="imgGraph1" id="imgGraph1" src="graph.php?file=' . $pfix . $id . '_dailystats.txt&ymin=0&xcol=0&ycol=26&skipl=3&ytitle=Daily maximum air temperature (F)&fac=1.8&off=32&usedates=1">';
      echo '</td>';
      echo '<td></td>';
      echo '<td>';
      echo '<img name="imgGraph2" id="imgGraph2" src="graph.php?file=' . $pfix . $id . '_dailystats.txt&ymin=0&xcol=0&ycol=53&skipl=3&ytitle=Container ET (inch/container)&fac=0.3937&usedates=1">';
      echo '</td></tr>';

      echo '<tr><td>Graph 3 - Nutrient Release<br>';
      echo '<select name="varGraph3" onChange="updateGraph(\'imgGraph3\', 2, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($ytitles3); $j++) {
        echo '<option name="' . $ytitles3[$j] . '" value="' . $ytitles3[$j] . '">' . $ytitles3[$j];
      }
      echo '</select></td>';
      echo '<td width=20></td>';
      echo '<td>';
      echo '<img name="imgGraph3" id="imgGraph3" src="graph.php?file=' . $pfix . $id . '_NRelstats.txt&ymin=0&xcol=0&ycol=20&skipl=3&ytitle=Cumulative N release (mg)&fac=1000&usedates=1">';
      echo '</td></tr>';
      echo '</table>';
      echo '</div>';

      echo '<div id="summarycharts" style="display:none">';
      #show summary charts
      echo '<br><b>Summary bar charts:</b><br>';
      echo '<table border=0>';
      echo '<tr><td>Chart 1 - Summary output on a per-container basis<br>';
      if (strpos($pagename, "realtime") !== false) {
        echo '<select name="varBar1" onChange="updateBarRT(\'imgBar1\', 0, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      } else echo '<select name="varBar1" onChange="updateBar(\'imgBar1\', 0, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($sumytitles1); $j++) {
        echo '<option name="' . $sumytitles1[$j] . '" value="' . $sumytitles1[$j] . '">' . $sumytitles1[$j];
      }
      echo '</select></td>';
      echo '<td width=20></td>';
      echo '<td>Chart 2 - Summary output on a per-area basis<br>';
      if (strpos($pagename, "realtime") !== false) {
        echo '<select name="varBar2" onChange="updateBarRT(\'imgBar2\', 1, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      } else echo '<select name="varBar2" onChange="updateBar(\'imgBar2\', 1, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($sumytitles2); $j++) {
        echo '<option name="' . $sumytitles2[$j] . '" value="' . $sumytitles2[$j] . '">' . $sumytitles2[$j];
      }
      echo '</select></td>';
      echo '</tr>';

      echo '<tr><td>';
      if (strpos($pagename, "realtime") !== false) {
	echo '<img name="imgBar1" id="imgBar1" src="graph.php?file=' . $pfix . $id . '_summaryoutput.txt&ymin=0&yvals=2&skipl=3&ytitle=Time to finish(weeks)&xticks=Time to finish&xtickv=1.5&fac=0.142857&style=3&summary=2">';
      } else echo '<img name="imgBar1" id="imgBar1" src="graph.php?file=' . $pfix . $id . '_summaryoutput.txt&ymin=0&ycol=2&skipl=3&ytitle=Time to finish(weeks)&fac=0.142857&style=3&summary=1">';
      echo '</td>';
      echo '<td></td>';
      echo '<td>';
      if (strpos($pagename, "realtime") !== false) {
	echo '<img name="imgBar2" id="imgBar2" src="graph.php?file=' . $pfix . $id . '_summaryarea.txt&ymin=0&yvals=3&skipl=3&ytitle=Rain (inch)&fac=0.3937&xticks=Rain&xtickv=1.5&style=3&summary=2">';
      } else echo '<img name="imgBar2" id="imgBar2" src="graph.php?file=' . $pfix . $id . '_summaryarea.txt&ymin=0&ycol=3&skipl=3&ytitle=Rain (inch)&fac=0.3937&style=3&summary=1">';
      echo '</td></tr>';

      echo '<tr><td>Chart 3 - Water Efficiency</td>';
      echo '<td></td>';
      echo '<td>Chart 4 - Nutrient Efficiency<br>';
      echo '<select name="varBar4" onChange="updateBar(\'imgBar4\', 3, \'' . $id . '\', this.selectedIndex, this.options[this.selectedIndex].value);">';
      for ($j = 0; $j < count($sumytitles4); $j++) {
        echo '<option name="' . $sumytitles4[$j] . '" value="' . $sumytitles4[$j] . '">' . $sumytitles4[$j];
      }
      echo '</select></td>';
      echo '</tr>';


      echo '<tr><td>';
      echo '<img name="imgBar3" id="imgBar3" src="graph.php?file=' . $pfix . $id . '_summaryarea.txt&ymin=0&yvals=3,4,5,6,7&skipl=3&ytitle=Inches&xticks=Rain,Irr,ET,Drain,Runoff&xtickv=0.5,1.5,2.5,3.5,4.5&fac=0.3937&style=3&summary=2">';
      echo '</td>';
      echo '<td width=20></td>';
      echo '<td>';
      echo '<img name="imgBar4" id="imgBar4" src="graph.php?file=' . $pfix . $id . '_summaryoutput.txt&ymin=0&yvals=' . $fertn . ',8,13&keepval=0&skipl=3&ytitle=g/plant&xticks=N applied,Runoff N,Plant N uptake&xtickv=0.5,1.5,2.55&fac=1&style=3&summary=2">';
      echo '</td></tr>';


      if (strpos($pagename, "realtime") === false) {
	#Only add economic data for non-real time
        echo '<tr><td>Chart 5 - Economic Data</td>';
	echo '</tr>';
	echo '<tr><td colspan=3>';
	$xvals = implode(",",$costs);
        $yticks = implode(",",array_keys($costs));
        $keepval = "0";
	$ytickv = "1";
        for ($j = 1; $j < count($costs); $j++) {
	  $keepval .= ",$j";
	  $ytickv .= "," . (2*$j+1);
        }
        echo '<img name="imgBar5" id="imgBar5" src="graph.php?file=' . $pfix . $id . '_summaryarea.txt&xmin=0&xvals=' . $xvals . '&keepval=' . $keepval . '&skipl=3&xtitle=$/plant&yticks=' . $yticks . '&ytickv=' . $ytickv . '&style=4&summary=3&ph=400&pw=600">';
        echo '</td></tr>';
      }
      echo '</table>';
      echo '</div>';

      echo '<div id="summarytables" style="display:none">';
      //show summary tables
      $ytitles1 = array("Year", "Time to finish (weeks)", "Total Rain (gal/ container)", "Total Irrigation (gal/ container)", "Total ET (gal/ container)", "Total Drainage (gal/ container)", "Total Runoff (gal/ container)", "Avg. area (sq ft/100 containers)");
      $ytitles2 = array("Year", "Total Runoff N (g/ container)", "N loss (% of applied)", "Shoot biomass (g/plant)", "Plant N Uptake (g/plant)", "Total Runoff P (g/ container)", "P loss (% of applied)", "Plant P Uptake (g/plant)", "Final N sufficiency (0-1)");
      $ytitles3 = array("Year", "Rain (inch)", "Irrigation water (inch)", "ET (inch)", "Container drainage (inch)", "Runoff (inch)", "Runoff N (lb/acre)", "Runoff P (lb/acre)");
      echo '<br><b>Summary tables:</b><br>';
      //read summaryoutput.txt
      echo 'Table 1 - Summary output on per-container basis<br>';
      echo '<a href="' . $pfix . $id . '_table1.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
      $yearly = file($pfix . $id . "_summaryoutput.txt");
      echo '<table border=1 width=640><tr>';
      $f = fopen($pfix . $id . "_table1.csv", "wb");
      for ($j = 0; $j < count($ytitles1); $j++) {
        echo '<td valign="top">' . $ytitles1[$j] . '</td>';
        fwrite($f, $ytitles1[$j]);
        if ($j < count($ytitles1)-1) {
           fwrite($f, ",");
        } else fwrite($f, "\n");
      }
      echo '</tr>';
      for ($j = 3; $j < count($yearly); $j++) {
        echo '<tr>';
        $temp = trim($yearly[$j]);
        $temp = preg_split("/\s+/", $temp);
	$td = '<td align="right">';
	if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
        if (substr($temp[0], 0, 1) == 'M' or substr($temp[0], 0, 1) == 'S') {
	  $td = '<td align="right" style="background: #C0C0C0">'; 
	}
        echo $td . $temp[0] . '</td>';
        echo $td . outFormat($temp[2]/7, 1) . '</td>';
        echo $td . outFormat($temp[3]*0.2642, 1) . '</td>';
        echo $td . outFormat($temp[4]*0.2642, 1) . '</td>';
        echo $td . outFormat($temp[5]*0.2642, 1) . '</td>';
        echo $td . outFormat($temp[6]*0.2642, 1) . '</td>';
        echo $td . outFormat($temp[7]*0.2642, 1) . '</td>';
        echo $td . outFormat($temp[12]*0.1076, 1) . '</td>';
        fwrite($f, $temp[0] . ",");
        fwrite($f, outFormat($temp[2]/7, 1) . ",");
        fwrite($f, outFormat($temp[3]*0.2642, 1) . ",");
        fwrite($f, outFormat($temp[4]*0.2642, 1) . ",");
        fwrite($f, outFormat($temp[5]*0.2642, 1) . ",");
        fwrite($f, outFormat($temp[6]*0.2642, 1) . ",");
        fwrite($f, outFormat($temp[7]*0.2642, 1) . ",");
        fwrite($f, outFormat($temp[12]*0.1076, 1) . "\n");
        echo '</tr>';
      }
      fclose($f);
      echo '</table><br>';

      //still summaryoutput.txt
      echo 'Table 2 - Summary output on per-container basis<br>';
      echo '<a href="' . $pfix . $id . '_table2.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
      echo '<table border=1 width=640><tr>';
      $f = fopen($pfix . $id . "_table2.csv", "wb");
      for ($j = 0; $j < count($ytitles2); $j++) {
        echo '<td valign="top">' . $ytitles2[$j] . '</td>';
        fwrite($f, $ytitles2[$j]);
        if ($j < count($ytitles2)-1) {
           fwrite($f, ",");
        } else fwrite($f, "\n");
      }
      echo '</tr>';
      for ($j = 3; $j < count($yearly); $j++) {
        echo '<tr>';
        $temp = trim($yearly[$j]);
        $temp = preg_split("/\s+/", $temp);
        $td = '<td align="right">';
        if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
        if (substr($temp[0], 0, 1) == 'M' or substr($temp[0], 0, 1) == 'S') {
          $td = '<td align="right" style="background: #C0C0C0">';
        }
        echo $td . $temp[0] . '</td>';
        echo $td . outFormat($temp[8], 2) . '</td>';
        echo $td . outFormat($temp[8]*10000/($fert*$pctn), 1) . '</td>';
        echo $td . outFormat($temp[11], 1) . '</td>';
        echo $td . outFormat($temp[13], 2) . '</td>';
        echo $td . outFormat($temp[14], 2) . '</td>';
        echo $td . outFormat($temp[14]*10000/($fert*$pctp), 1) . '</td>';
        echo $td . outFormat($temp[16], 2) . '</td>';
        echo $td . outFormat($temp[17], 2) . '</td>';
        fwrite($f, $temp[0] . ",");
        fwrite($f, outFormat($temp[8], 2) . ",");
        fwrite($f, outFormat($temp[8]*10000/($fert*$pctn), 1) . ",");
        fwrite($f, outFormat($temp[11], 1) . ",");
        fwrite($f, outFormat($temp[13], 2) . ",");
        fwrite($f, outFormat($temp[14], 2) . ",");
        fwrite($f, outFormat($temp[14]*10000/($fert*$pctp), 1) . ",");
        fwrite($f, outFormat($temp[16], 2) . ",");
        fwrite($f, outFormat($temp[17], 2) . "\n");
        echo '</tr>';
      }
      fclose($f);
      echo '</table><br>';

      //read summaryarea.txt
      echo 'Table 3 - Summary output on per-area basis<br>';
      echo '<a href="' . $pfix . $id . '_table3.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
      $yearly = file($pfix . $id . "_summaryarea.txt");
      echo '<table border=1 width=640><tr>';
      $f = fopen($pfix . $id . "_table3.csv", "wb");
      for ($j = 0; $j < count($ytitles3); $j++) {
        echo '<td valign="top">' . $ytitles3[$j] . '</td>';
        fwrite($f, $ytitles3[$j]);
        if ($j < count($ytitles3)-1) {
           fwrite($f, ",");
        } else fwrite($f, "\n");
      }
      echo '</tr>';
      for ($j = 3; $j < count($yearly); $j++) {
        echo '<tr>';
        $temp = trim($yearly[$j]);
        $temp = preg_split("/\s+/", $temp);
        $td = '<td align="right">';
        if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
        if (substr($temp[0], 0, 1) == 'M' or substr($temp[0], 0, 1) == 'S') {
          $td = '<td align="right" style="background: #C0C0C0">';   
        }
        echo $td . $temp[0] . '</td>';
        echo $td . outFormat($temp[3]*0.3937, 1) . '</td>';
        echo $td . outFormat($temp[4]*0.3937, 1) . '</td>';
        echo $td . outFormat($temp[5]*0.3937, 1) . '</td>';
        echo $td . outFormat($temp[6]*0.3937, 1) . '</td>';
        echo $td . outFormat($temp[7]*0.3937, 1) . '</td>';
        echo $td . outFormat($temp[8]*8.9, 1) . '</td>';
        echo $td . outFormat($temp[9]*8.9, 1) . '</td>';
        fwrite($f, $temp[0] . ",");
        fwrite($f, outFormat($temp[3]*0.3937, 1) . ",");
        fwrite($f, outFormat($temp[4]*0.3937, 1) . ",");
        fwrite($f, outFormat($temp[5]*0.3937, 1) . ",");
        fwrite($f, outFormat($temp[6]*0.3937, 1) . ",");
        fwrite($f, outFormat($temp[7]*0.3937, 1) . ",");
        fwrite($f, outFormat($temp[8]*8.9, 1) . ",");
        fwrite($f, outFormat($temp[9]*8.9, 1) . "\n");
        echo '</tr>';
      }
      fclose($f);
      echo '</table><br>';

      if (strpos($pagename, "realtime") === false) {
        #Only add economic data for non-real time
        echo 'Table 4 - Average production costs<br>';
        echo '<a href="' . $pfix . $id . '_table4.csv"><img src="images/download_table.png" border=0 title="Download table as a .csv file that can be imported into Excel or Open Office"></a><br><br>';
        $ytitles4 = array("Cost Item", "unit", "$/unit", "No. units", "Total costs ($)", "% of Total");
        echo '<table border=1 width=560><tr>';
        $f = fopen($pfix . $id . "_table4.csv", "wb");
        for ($j = 0; $j < count($ytitles4); $j++) {
	  echo '<th valign="top" align="center">' . $ytitles4[$j] . '</td>';
	  fwrite($f, $ytitles4[$j]);
	  if ($j < count($ytitles4)-1) {
	    fwrite($f, ",");
	  } else fwrite($f, "\n");
	}
        $keys = array_keys($costs);
        $totalCost = array_sum($costs);
        for ($j = 0; $j < count($costs); $j++) {
          echo '<tr>';
          $td = '<td align="right">';
          if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
	  $td0 = '<td align="left">';
	  if ($j % 2 == 0) $td0 = '<td align="left" class="outputtdalt">';
          echo $td0 . $keys[$j] . '</td>';
          echo $td . $units[$keys[$j]] . '</td>';
          echo $td . outFormat($rates[$keys[$j]], 4) . '</td>';
          echo $td . outFormat($quant[$keys[$j]], 4) . '</td>';
          echo $td . outFormat($costs[$keys[$j]], 3) . '</td>';
          echo $td . outFormat($costs[$keys[$j]]/$totalCost*100, 1) . '</td>';
          fwrite($f, $keys[$j] . ",");
          fwrite($f, $units[$keys[$j]] . ",");
          fwrite($f, outFormat($rates[$keys[$j]],4) . ",");
          fwrite($f, outFormat($quant[$keys[$j]],4) . ",");
          fwrite($f, outFormat($costs[$keys[$j]],3) . ",");
          fwrite($f, outFormat($costs[$keys[$j]]/$totalCost*100, 1) . "\n");
          echo '</tr>';
        }
	$j++;
	echo '<tr><th align="left" colspan=4';
	if ($j % 2 == 0) echo ' class="outputtdalt"';
	echo '>TOTAL</th>';
        $td = '<td align="right">';
        if ($j % 2 == 0) $td = '<td align="right" class="outputtdalt">';
	echo $td . outFormat($totalCost, 3) . '</td>';
	echo $td . '100.0</td>';
	fwrite($f, "TOTAL,,,$totalCost,100.0\n");
	echo '</tr>';
        fclose($f);
        echo '</table><br>';
      }
      echo '</div>';

      echo '<div id="viewfiles" class="indent75" style="display:none;">';
      //list output files
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
          $outname = substr($outfiles[$j], strpos($outfiles[$j], '_')+1);
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

      echo '</form>';
      unset($yearly);
      echo '<br><br>';
      showOutputButtons();
      if ($isIE) {
        //screw you Bill Gates!
        echo '<script language="javascript">billGatesSucks();</script>';
      }
?>
