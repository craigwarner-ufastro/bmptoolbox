<?php
  echo '<script language="javascript">';
  echo 'pagename="' . $pagename . '";';
  echo '</script>';
  #Get cost info
  $costs = array();
  $sql = "select * from costs where uid=0";
  if ($isLoggedIn && isset($_SESSION['bmpid'])) $sql .= " OR uid=" . $_SESSION['bmpid']; 
  $out = $mysqli_db->query($sql);
  if ($out != false) {
    while ($row = $out->fetch_array(MYSQLI_BOTH)) {
      $costs[] = $row;
    }
  }
  echo '<script language="javascript">';
  echo 'var costNamesT1 = Array();';
  echo 'var costIdsT1 = Array();';
  echo 'var costNamesT3 = Array();';
  echo 'var costIdsT3 = Array();';
  $nT1 = 0;
  $nT3 = 0;
  for ($j = 0; $j < count($costs); $j++) {
    if ($costs[$j]['trade'] == 'Trade #1') {
      echo "costNamesT1[$nT1] = '" . $costs[$j]['costName'] . "';";
      echo "costIdsT1[$nT1] = '" . $costs[$j]['cid'] . "';";
      $nT1++;
    } else {
      echo "costNamesT3[$nT3] = '" . $costs[$j]['costName'] . "';";
      echo "costIdsT3[$nT3] = '" . $costs[$j]['cid'] . "';";
      $nT3++;
    }
  }
  echo '</script>';
?>
<?php #if (isComp($pagename)) { ?>
<!--
***Commented out 8/27/13
<tr>
<td id="irrig_cost" name="irrig_cost" align="left" valign="top">
  <b>Enter irrigation pumping cost ($/1000 gal):</b>
  <select name="irrcost">
  <?php
#    for ($j = 5; $j <= 35; $j++) {
#      if ($j == 20) $selTxt = " selected"; else $selTxt = "";
#      echo '<option name="' . $j . '" value = "' . $j . '" ' . $selTxt . '>$' . outFormat($j/100., 2);
#    }
  ?>
  </select><br>
</td>
</tr>

<?php #if ($pagename == "fert") { ?>
<tr>
<td>
  <b>Enter price per pound of N in the fertilizer ($/lb N):</b>
  <select name="fertcost">
  <?php
#    for ($j = 5; $j <= 10; $j+=0.25) {
#      if ($j == 7.5) $selTxt = " selected"; else $selTxt = "";
#      echo '<option name="' . $j . '" value = "' . $j . '" ' . $selTxt . '>$' . outFormat($j, 2);
#    }
  ?>
  </select><br>
</td>
</tr>
<?php #} ?>

<tr><td colspan=4><hr><br></td></tr>
-->
<?php #} ?>

<tr>
<td id="species" name="species" align="left" valign="top">
  <input type="hidden" name="template" value="1-gal_default.txt">
  <b>Select the plant species:</b>
  <ul style="margin-top: 3px;">
    <input type="radio" name="species" value="vibur" checked onClick="changeSpecies('vibur');"><i>Viburnum odoratissimum</i> (fast-growing, large leaves, upright-spreading habit)<br>
    <input type="radio" name="species" value="ilex" onClick="changeSpecies('ilex');"><i>Ilex vomitoria</i> (slow-growing, small leaves, semi-broad spreading habit)<br>
    <span class="regreq"><i>Note: Changing plant species automatically changes default settings for finish height below.</i>
    </span><br>
  </ul>
</td>
</tr>

<tr>
<td id="container_size" name="container_size" align="left" valign="top">
  <input type="hidden" name="S_VOL" value=2400>
  <input type="hidden" name="POTDIAM" value=16>
  <input type="hidden" name="NOPMAX" value=1>
  <b>Select the container size:</b>
  <ul style="margin-top: 3px;">
    <input type="radio" name="container_size" value="1" checked onClick="changeContSize(1);">Trade #1<br>
    <input type="radio" name="container_size" value="3" onClick="changeContSize(3);">Trade #3<br>
    <span class="regreq"><i>Note: Changing container size automatically changes
    default settings for cost basis, finish height, and fertilizer rate menus below.</i>
    </span><br>
  </ul>
</td>
</tr>

<?php if ($pagename != "realtime") { ?>
<tr>
<td id="costBais" name="costBasis" align="left" valign="top">
  <b>Select a cost basis:</b>
  <select name="costBasis">
  <?php
    for ($j = 0; $j < count($costs); $j++) {
      if ($costs[$j]['trade'] == 'Trade #1') {
	echo '<option name="' . $costs[$j]['cid'] . '" value="' . $costs[$j]['cid'] . '">' . $costs[$j]['costName'];  
      }
    }
  ?>
  </select>
</td>
</tr>
<?php } ?>

<?php if (strpos($pagename, "location") === false) { ?>
<tr>
<td name="WFNAME">
  <b>Select a location nearest your nursery:</b>
  <select name="WFNAME" onChange="updateYear(); updateUserWth();">
  <?php
    $selwth = 0;
    for ($j = 0; $j < count($fawn); $j++) {
      if ($j == $selwth) $selTxt = " selected"; else $selTxt = "";
      echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '" ' . $selTxt . '>' . str_replace("fawn/", "", str_replace("_"," ",$fawn[$j]));
    }
  ?>
  </select>
  <?php if (strpos($pagename, "realtime") === false) { ?>
    <ul style="margin-top: 3px;">
    <input type="radio" name="wtype" value="FAWN" checked onClick="changeWeather(0);">Florida Automated Weather Network (FAWN) (35 locations) 
    <br>
    <input type="radio" name="wtype" value="HIS" onClick="changeWeather(1);">Long-term (&gt;30 yrs) historical weather courtesy of AgroClimate (9 locations)
    <?php if ($isLoggedIn && count($userWth) > 0) {
      echo '<br>';
      echo '<input type="radio" name="wtype" value="USER" onClick="changeWeather(2);">My uploaded weather files';
    } ?>
    </ul>
  <?php } else {
      if ($isLoggedIn && count($userWth) > 0) {
	echo '<ul style="margin-top: 3px;">';
	echo '<input type="radio" name="wtype" value="FAWN" checked onClick="changeWeather(0);">Florida Automated Weather Network (FAWN) (35 locations)';
        echo '<br>';
        echo '<input type="radio" name="wtype" value="USER" onClick="changeWeather(2);">My uploaded weather files';
      }
    }
  ?>

  <table border=0>
  <tr><td valign="top" width=180>
    <div id="weatherInfo" align="left" width=180 height=300>
    </div>
  </td>
  <td valign="top">
    <img src="images/fawnmap2.png" USEMAP="#fawnMap" border=0 name="weatherImage">
    <map name="weatherMap">
    <?php
      for ($j=0; $j < count($wth); $j++) {
	echo '<area shape="circle" coords="' . $wthX[$j] . ',' . $wthY[$j] . ',15" ';
	echo 'alt="' . str_replace("_"," ",$wth[$j]) . '" ';
	echo 'title="' . str_replace("_"," ",$wth[$j]) . '" ';
	echo "href=\"javascript:setLocation('" . $wth[$j] . "');\" ";
	echo "onMouseOver=\"updateWthLoc('" . $wth[$j] . "');\">\n";
      }
    ?>
    </map>
    <map name="fawnMap">
    <?php
      $temp = file('images/fawn-points.txt');
      for ($j = 0; $j < count($temp); $j++) {
        $row = trim($temp[$j]);
        $row = preg_split("/\s+/", $row);
        $row[0] = floor($row[0]*0.64+0.5);
        $row[1] = floor($row[1]*0.64+0.5);
        echo '<area shape="circle" coords="' . $row[0] . ',' . $row[1] . ',8" ';
        echo 'alt="' . str_replace("_"," ",$row[2]) . '" ';
        echo 'title="' . str_replace("_"," ",$row[2]) . '" ';
        echo "href=\"javascript:setLocation('" . $row[2] . "');\" ";
        echo "onMouseOver=\"updateFawnLoc('" . $row[2] . "');\">\n";
      }
    ?>
    </map>
  </td>
  <td valign="top">
    <a href="http://fawn.ifas.ufl.edu" target="ccmt"><img src="images/FAWN-branding-2.png" border=0></a>
    <br>
    <a href="http://www.agroclimate.org" target="ccmt"><img src="images/agroclimate.png" border=0></a>
  </td>
  </tr></table>
</td> 
</tr>
<?php } ?>

<?php if (strpos($pagename, "date") === false) { ?>
<tr>
<td name="month">
  <?php
    $daycount = 31;
    if (strpos($pagename, "realtime") !== false) {
      $plttime = time()-60*86400;
      $daycount = (int)(date("t", $plttime))+1; 
      echo '<input type="hidden" name="PLT_DOY" value=' . (int)(date("z", $plttime)+1) . '>';
    } else echo '<input type="hidden" name="PLT_DOY" value=91>'; 
  ?>
  <b>Select plant start date:</b>
  <a href="Week_of_year_table.pdf" target="new">(click here for week of year
  table)</a>
  <select name="month" onChange="setDayList(this.selectedIndex); setDay(true);">
  <?php
    for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value=' . $j;
      if (strpos($pagename, "realtime") !== false) {
	if ($j == (int)(date("m", $plttime))-1) echo ' selected';
      } else if ($j == 3) echo ' selected';
      echo '>';
      echo $months[$j];
    }
  ?>
  </select>

  <select name="day" onChange="setDay(true);">
  <?php
    for ($j = 0; $j < $daycount; $j++) {
      echo '<option name=' . ($j+1) . ' value=' . ($j+1);
      if (strpos($pagename, "realtime") !== false) {
	if ($j == (int)(date("d", $plttime))-1) echo ' selected';
      }
      echo '>';
      echo ($j+1);
    }
  ?>
  </select>

  <input type="text" name="START_YR" size=4 readonly value="">
  <a href="#" onClick="updateCalendar(cal, 'anchor1x'); return false;" name="anchor1x" id="anchor1x">select</a>

  <br>
  <b>Select end year:</b>
  <select name="END_YR">
  <?php
    $currYr = date("Y");
    echo '<option name="' . $currYr . '" value="' . $currYr . '">' . $currYr;
  ?>
  </select>
  <script language="javascript">updateYear();</script>
</td>
</tr>
<?php } ?>

<?php if (strpos($pagename, "realtime") === false) { ?>
<tr>
<td name="FINISH">
  <b>On what basis do you want to finish the crop?</b>
  <ul style="margin-top: 3px;">
    <input type="radio" name="FINISH" value="FIXED" onClick="changeHarv(0);">Finish the crop after a fixed number of weeks regardless of size<br>
    <input type="radio" name="FINISH" value="SIZE" checked onClick="changeHarv(1);">Finish the crop when the following plant height is reached<br>
  </ul>
</td>
</tr>

<tr>
<td id="HARV" name="HARV" valign="top" align="left">
  <ul style="margin-top: 0px; margin-left: 50px">
    <select name="HARV_HT">
    <?php
      for ($j = 6; $j<=36; $j++) {
	if ($j == 16) $defSel = " selected"; else $defSel = "";
	echo '<option name="' . ($j*2.54) . '" value="' . ($j*2.54) . '"' . $defSel . '>' . $j;
      }
    ?>
    </select> inches
  </ul>
</td>
</tr>
<?php } else { #realtime
  echo '<input type="hidden" name="FINISH" value="FIXED">';
  echo '<input type="hidden" name="HARVDAYS" value=366>';  
}?>

<tr>
<td id="ARRANGE" name="ARRANGE" align="left" valign="top"> 
  <table>
  <tr>
  <td valign="top">
    <b>Spacing arrangement:</b>
    <ul style="margin-top: 3px;">
    <input type="radio" name="ARRANGE" value="TRIANG" checked
	onClick="changeImages('arrangeImg','images/triangular_pattern.png');
		changeImages('startImg','images/triangular_start.png');
		updatePTA();
		//moveImgSrc = 'images/triangular_move.png';
		//if (showMoveImg) changeImages('moveImg','images/triangular_move.png');">Triangular Pattern<br>
    <input type="radio" name="ARRANGE" value="SQUARE"
	onClick="changeImages('arrangeImg','images/square_pattern.png');
		changeImages('startImg','images/square_start.png');
		updatePTA();
		//moveImgSrc = 'images/square_move.png';
		//if (showMoveImg) changeImages('moveImg','images/square_move.png');">Square Pattern<br>
  </td>
  <td valign="top">
    <img src="images/triangular_pattern.png" id="arrangeImg" name="arrangeImg" align="right" alt="spacing arrangement">
  </td>
  </tr>
  </table>
    </ul>
</td>
</tr>

<tr>
<td id="spacing" name="spacing">
  <table width=90%><tr>
  <td valign="top">
    <input type="hidden" name="PTA0" value=312.5>
    <b>Container spacing when plants are first placed out onto the production area:</b>
    <ul>
      <span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #ff0000;">B = </span>
      <select name="SPAC_B" onChange="updatePTA();">
      <?php
        for ($j = 0; $j <= 18; $j++) {
	  echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
        }
      ?>
      <option name="contdiam" value="contdiam">Cont. Diam.
      <option name="3/4" value="3/4">3/4 Diameter
      </select> between top edge of containers<br>
      <br>
      <span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #0000ff;">W = </span>
      <select name="SPAC_W" onChange="updatePTA();">
      <?php
        for ($j = 0; $j <= 18; $j++) {
          echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
        }
      ?>
      <option name="contdiam" value="contdiam">Cont. Diam.
      <option name="3/4" value="3/4">3/4 Diameter
      </select> between top edge of containers<br>
    </ul>
  </td>
  <td valign="top">
    <img src="images/triangular_start.png" id="startImg" name="startImg" align="right" alt="spacing arrangement">
  </td>
  </tr></table>
</td>
</tr>

<?php if (strpos($pagename, "realtime") === false) { ?>
<tr>
<td id="move" name="move">
  <table><tr>
  <td valign="top">
    <b>Container move schedule:</b> (<a href="javascript:popupText(laitext);" onMouseOver="if (!IsIE()) ddrivetip(laitext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
    <ul style="margin-top: 3px">
      <input type="hidden" name="PTA1" value=0>
      <input type="hidden" name="MOVE1" value="-99">
      <input type="hidden" name="MOVE_LAI" value="3">
      <input type="radio" name="MOVE" value="FIXED" onClick="changeMove1(0);">Don't Move<br>
      <input type="radio" name="MOVE" value="FIXED" onClick="changeMove1(1);">Move containers after
	<select name="move1w" onChange="document.paramForm.MOVE1.value=(document.paramForm.move1w.selectedIndex+1)*7;" style="z-index: 1;">
	<?php
	  for ($j = 1; $j <= 52; $j++) {
	    if ($j == 12) $defSel = ' selected'; else $defSel = '';
	    echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
	  }
	?>
	</select> weeks<br>
      <input type="radio" name="MOVE" value="LAI" onClick="changeMove1(2);" checked>Move based on model recommendation<br>
      <div name="spac1" id="spac1">
	<ul style="margin-top: 20px; margin-left: -5px">
	<b>Container spacing after move:</b><br>
	<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #ff0000;">B = </span>
	<select name="SPAC1_B" onChange="updatePTA();">
	<?php
	  for ($j = 0; $j <= 18; $j++) { 
	    echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
	  }
	?>
        <option name="contdiam" value="contdiam" selected>Cont. Diam.
        <option name="3/4" value="3/4">3/4 Diameter
	</select> between top edge of containers<br>
	<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #0000ff;">W = </span>
	<select name="SPAC1_W" onChange="updatePTA();">
        <?php
          for ($j = 0; $j <= 18; $j++) {
            echo '<option name=' . $j . ' value=' . $j . '>' . $j . ' inches';
          }
        ?>
        <option name="contdiam" value="contdiam" selected>Cont. Diam.
        <option name="3/4" value="3/4">3/4 Diameter
	</select> between top edge of containers<br>
	</ul>
        <script language="javascript">
	  //set PTA0 and PTA1
	  updatePTA();
	</script>
      </div>
    </ul>
  </td>
  <td valign="top">
    <img src="images/spacer.png" id="moveImg" name="moveImg" align="right">
  </td>
  </tr></table>
</td>
</tr>
<?php } else { #realtime ?>
  <input type="hidden" name="MOVE" value="FIXED">
  <input type="hidden" name="MOVE_LAI" value="3">
  <script language="javascript">
    //set PTA0 and PTA1
    updatePTA();
  </script>
<?php } ?>


<?php if (strpos($pagename, "irr") === false) { ?>
<?php if (strpos($pagename, "realtime") === false) { ?>
<tr><td colspan=4><hr></td></tr>

<tr>
<td id="irrigation" name="irrigation">
  <b>Select an irrigation schedule:</b>
  <ul style="margin-top: 3px;">
    <input type="hidden" name="D_IRR0" value=1.016>
    <input type="hidden" name="D_IRR1" value=0.762>
    <input type="hidden" name="D_IRR2" value=1.016>
    <input type="hidden" name="D_IRR3" value=1.27>
    <input type="radio" name="SCHED" value="FIXED" onClick="changeIrr(0)";>
    Fixed daily rate with options to change the irrigation rate during the season<br>
    <input type="radio" name="SCHED" value="MAD" checked onClick="changeIrr(1);">
    Model-recommended rate that depends upon daily evapotranspiration and rainfall (<a href="javascript:popupText(madtext);" onMouseOver="if (!IsIE()) ddrivetip(madtext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)

    <div name="irrdiv" id="irrdiv">
    </div>
    <?php if (strpos($pagename, "date") === false && strpos($pagename, "location") === false) { ?>
      <script language="javascript">
	document.paramForm.SCHED[0].click();
      </script>
    <?php } ?>
  </ul>
</td>
</tr>
<?php } else { #Realtime ?>
  <input type="hidden" name="D_IRR0" value=1.016>
  <input type="hidden" name="D_IRR1" value=0.762>
  <input type="hidden" name="D_IRR2" value=1.016>
  <input type="hidden" name="D_IRR3" value=1.27>
  <input type="hidden" name="SCHED" value="MAD">
<?php } #end realtime
} #end not irr ?>

<tr><td colspan=4><hr></td></tr>

<tr>
<td id="fertilizer" name="fertilizer">
  <b>Fertilizer detail</b> (<a href="javascript:popupText(ferttext);" onMouseOver="if (!IsIE()) ddrivetip(ferttext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
  <ul>
    <?php if (strpos($pagename, "fert") === false) { ?>
    <b>Fertilizer rate:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT" value=19.775>
      <input type="radio" name="fertb" value="lb" onClick="changeFert(0,0);" checked>Enter lb N per cubic yard: 
      <select name="lbrate" onChange="changeFert(0,0);">
      <?php
	for ($j = 5; $j <=40; $j++) {
	   if ($j == 25) $defSel = " selected"; else $defSel = "";
	   echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
	}
      ?>
      </select><br>
      <input type="radio" name="fertb" value="g" onClick="changeFert(1,0);">Enter g of fertilizer per container:
      <select name="grate" onChange="changeFert(1,0);">
      <?php
        for ($j = 2; $j <= 260; $j++) {
           if ($j == 20) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
    </ul>
    <?php } ?>
    Enter percent total N in fertilizer: (<a href="javascript:popupText(pctntext);" onMouseOver="if (!IsIE()) ddrivetip(pctntext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
    <select name="PCT_N" onChange="changeFert(-1,0);">
    <?php
      for ($j = 8; $j <=30; $j++) {
        if ($j == 18) $defSel = " selected"; else $defSel = "";
	echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
      }
    ?>
    </select>%<br>
    Enter a percent controlled-release N: (<a href="javascript:popupText(pctcrntext);" onMouseOver="if (!IsIE()) ddrivetip(pctcrntext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
    <select name="PCT_CRN">
    <?php
      for ($j = 8; $j <=30; $j++) {
        if ($j == 15) $defSel = " selected"; else $defSel = "";
        echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
      }
    ?>
    </select>%<br>

    Enter percent P<sub>2</sub>O<sub>5</sub> in fertilizer: (<a href="javascript:popupText(pctptext);" onMouseOver="if (!IsIE()) ddrivetip(pctptext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
    <input type="hidden" name="PCT_P" value=2.616>
    <select name="PCT_P2O5" onChange="changeFert(-1,0);">
    <?php
      for ($j = 0; $j <=20; $j++) {
        if ($j == 6) $defSel = " selected"; else $defSel = "";
        echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
      }
    ?>
    </select>%<br>

    Enter longevity rating of controlled-release fertilizer: (<a href="javascript:popupText(fdaystext);" onMouseOver="if (!IsIE()) ddrivetip(fdaystext,'99ffaa',450);" onMouseOut="if (!IsIE()) hideddrivetip();">more info</a>)
    <input type="hidden" name="CRF_DAYS" value="259">
    <select name="fmonth" onChange="changeFDays();">
    <?php
      $nmonths = array(3, 3.5, 4, 5, 5.5, 6, 7, 8, 8.5, 9, 10, 12, 13, 14, 16);
      $smonths = array("3", "3-4", "4", "5", "5-6", "6", "7", "8", "8-9", "9", "10", "12", "12-14", "14", "16");
      for ($j = 0; $j < count($nmonths); $j++) {
	if ($smonths[$j] == "8-9") $defSel = " selected"; else $defSel = "";
	echo '<option name=' . $smonths[$j] . ' value=' . $nmonths[$j] . $defSel . '>' . $smonths[$j];
      }
    ?>
    </select> months.<br>
  </ul>
</td>
</tr>

</table>
<?php if ($isSub == 4) {
    echo '<hr>';
  } else {
?>
    <br>
    <input type="Submit" name="Submit" value="Submit">
    <input type="Reset" name="Reset" value="Reset">
    </form>
<?php } ?>
