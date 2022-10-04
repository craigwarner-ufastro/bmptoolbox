<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Compare Plant Dates</title>
<script language="Javascript">
var ncal = 1;
var nruns_old = 4;
//vars for plots
var xplot = new Array(); 
var yplot = new Array();
var costplot = new Array();

function getGraphURL(name, id, index, ytitle) {
  var xvals = xplot[0];
  var yvals = yplot[0][index];
  for (j = 1; j < xplot.length; j++) {
    xvals += ',' + xplot[j];
    yvals += ',' + yplot[j][index];
  }
  path = "compgraph.php?xvals=" + xvals + "&yvals=" + yvals;
  path += "&xticks=" + xticks + "&xtickv=" + xtickv + "&xextrema=" + xextrema + "&ymin=0";
  path += "&style=3&xtitle=Plant Date&ytitle=" + ytitle;
  return path
}

function updateGraph(imgName, n, index, ytitle) {
   if (document.images) {
      var xvals = xplot[0];
      var yvals = yplot[0][index];
      if (n == 1) yvals = costplot[0][index];
      for (j = 1; j < xplot.length; j++) {
        xvals += ',' + xplot[j];
        if (n == 0) {
          yvals += ',' + yplot[j][index];
        } else if (n == 1) {
          yvals += ',' + costplot[j][index];
        }
      }
      path = "compgraph.php?xvals=" + xvals + "&yvals=" + yvals;
      path += "&xticks=" + xticks + "&xtickv=" + xtickv + "&xextrema=" + xextrema + "&ymin=0";
      path += "&style=3&xtitle=Plant Date&ytitle=" + ytitle;
      document[imgName].src = path; 
   }
}

function setNRuns(n) {
  var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"]; 
  if (n < 4) {
    df = document.getElementById('run4');
    desc = '<br>';
    df.innerHTML = desc;
  } else {
    df = document.getElementById('run4');
    desc = '<input type="hidden" name="PLT_DOY_4" value=274>';
    desc += '<b>Plant date 4:</b>\n';
    desc += '<select name="month4" onChange="setDayList2(this.selectedIndex, document.paramForm.day4); setDay2(this, document.paramForm.day4, document.paramForm.PLT_DOY_4, true);">';
    for (j = 0; j < 12; j++) {
      if (j == 9) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + j + ' value=' + j + defSel + '>' + months[j]; 
    }
    desc += '</select>\n';
    desc += '<select name="day4" onChange="setDay2(document.paramForm.month4, this, document.paramForm.PLT_DOY_4, true);">';
    for (j = 0; j < 30; j++) {
      desc += '<option name=' + (j+1) + ' value=' + (j+1) + '>' + (j+1); 
    }
    desc += '</select>';
    desc += ' <input type="text" name="START_YR_4" size=4 readonly value="' + document.paramForm.START_YR_2.value + '">';
    desc += ' <a href="#" onClick="updateCalendar2(cal, \'anchor4x\', 4); return false;" name="anchor4x" id="anchor4x">select</a>';
    desc += '<br>';
    df.innerHTML = desc;
  }
  if (n < 3) {
    df = document.getElementById('run3');
    desc = '';
    df.innerHTML = desc;
  } else if (nruns_old < 3) {
    df = document.getElementById('run3');
    desc = '<input type="hidden" name="PLT_DOY_3" value=182>';
    desc += '<b>Plant date 3:</b>\n';
    desc += '<select name="month3" onChange="setDayList2(this.selectedIndex, document.paramForm.day3); setDay2(this, document.paramForm.day3, document.paramForm.PLT_DOY_3, true);">';
    for (j = 0; j < 12; j++) {
      if (j == 6) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + j + ' value=' + j + defSel + '>' + months[j]; 
    }
    desc += '</select>\n';
    desc += '<select name="day3" onChange="setDay2(document.paramForm.month3, this, document.paramForm.PLT_DOY_3, true);">';
    for (j = 0; j < 30; j++) {
      if (j == 0) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + (j+1) + ' value=' + (j+1) + defSel + '>' + (j+1); 
    }
    desc += '</select>';
    desc += ' <input type="text" name="START_YR_3" size=4 readonly value="' + document.paramForm.START_YR_2.value + '">';
    desc += ' <a href="#" onClick="updateCalendar2(cal, \'anchor3x\', 3); return false;" name="anchor3x" id="anchor3x">select</a>';
    df.innerHTML = desc;
  }
  nruns_old = n;
}

  function setDayList2(n, dayForm) {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    var nd = daysInMonth[n];
    var nsel = dayForm.selectedIndex;
    dayForm.options.length=0;
    for (j = 0; j < nd; j++) {
      dayForm.options[j] = new Option(j+1, j+1, false, false);
    }
    dayForm.options[nsel].selected = true;
  }

  function setDay2(monthForm, dayForm, doyForm, x) {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    var month = monthForm.selectedIndex;
    var doy = 0;
    for (j = 0; j < month; j++) {
      doy+=daysInMonth[j];
    }
    doy += dayForm.selectedIndex+1;
    doyForm.value = doy;
    for (j = 0; j < 4; j++) {
      doyForm = eval('document.paramForm.PLT_DOY_'+(j+1));
    }
    if (x) updateYear2();
  }

  function setCalendarVals2(y,m,d) {
    var startYr = eval('document.paramForm.START_YR_'+ncal);
    var monthForm = eval('document.paramForm.month'+ncal);
    var dayForm = eval('document.paramForm.day'+ncal);
    var doyForm = eval('document.paramForm.PLT_DOY_'+ncal);
    startYr.value = y;
    monthForm.selectedIndex = m-1;
    setDayList2(m-1, dayForm);
    dayForm.selectedIndex = d-1;
    setDay2(monthForm, dayForm, doyForm, false);
    updateEndYear(false);
  }

  function updateCalendar2(cal, anchor, n) {
    ncal = n;
    var startYr = eval('document.paramForm.START_YR_'+n);
    var monthForm = eval('document.paramForm.month'+n);
    var dayForm = eval('document.paramForm.day'+n);
    var doyForm = eval('document.paramForm.PLT_DOY_'+n);
    selDate = '' + (monthForm.selectedIndex+1);
    selDate += '/' + (dayForm.selectedIndex+1);
    selDate += '/' + startYr.value;
    var startDate;
    var endDate;
    if (wthType == 0) {
      startDate = fawnStart[document.paramForm.WFNAME.selectedIndex];
      endDate = latest;
    } else if (wthType == 1) {
      startDate = wthStart;
      endDate = wthEnd[document.paramForm.WFNAME.selectedIndex];
    } else if (wthType == 2) {
      startDate = userWthStart[document.paramForm.WFNAME.selectedIndex];
      endDate = userWthEnd[document.paramForm.WFNAME.selectedIndex];
    }
    var sd = new Date(startDate);
    sd.setDate(sd.getDate()-1);
    var ed = new Date(endDate);
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(ed,"yyyy-MM-dd"), null);
    cal.addDisabledDates(null, formatDate(sd,"yyyy-MM-dd"));
    cal.showCalendar(anchor, selDate);
    return false;
  }

  function updateYear2() {
    var startDate;
    if (wthType == 0) {
      startDate = fawnStart[document.paramForm.WFNAME.selectedIndex];
    } else if (wthType == 1) {
      startDate = wthStart;
    } else if (wthType == 2) {
      startDate = userWthStart[document.paramForm.WFNAME.selectedIndex];
    }
    var d = new Date(startDate);
    var mon = d.getMonth()+1;
    var day = d.getDate();
    var yr = d.getFullYear();
    var currYr = 0;
    for (j = 1; j < nruns_old+1; j++) {
      var currMon = eval('document.paramForm.month'+j).selectedIndex+1;
      var currDay = eval('document.paramForm.day'+j).selectedIndex+1;
      if (currMon < mon || (currMon == mon && currDay < day)) {
	currYr = Math.max(yr+1, currYr);
      } else currYr = Math.max(yr, currYr); 
    }
    for (j = 1; j < nruns_old+1; j++) {
      startYr = eval('document.paramForm.START_YR_'+j);
      startYr.value = currYr;
    }
    updateEndYear(true);
  }

</script>

<?php require('header.php'); ?>
<script language+"javascript">
  cal.setReturnFunction("setCalendarVals2");
</script>

<h1 class="h1-container">Compare Plant Dates<font color="#ffffff" class="h1-shadow">Compare Plant Dates</font></h1>
<center><a href="comparison_fert.php">Compare Fertilizer Rates</a> |
<a href="comparison_irr.php">Compare Irrigation Schedules</a> |
<a href="comparison_fert_irr.php">Compare Fertilizer and Irrigation</a><br> 
<b>Compare Plant Dates</b> |
<a href="comparison_location.php">Compare Locations</a>
</center>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = 'date';

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $xtitle = "Plant Date";
      $tablelabel = "plant date";
      $compnames = array("PLT_DOY");

      require("compare_setup_save.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['PLT_DOY' . ($j+1)];
        $xlabels[$j] = dateFromDoy($compvals[0][$j]);
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      $xtitle = "Plant Date";
      $tablelabel = "plant date";
      $compnames = array("PLT_DOY");

      require("compare_save_submitted.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['PLT_DOY' . ($j+1)];
        $xlabels[$j] = dateFromDoy($compvals[0][$j]);
      }
   } else $isSub = 3;

   $tempFile = $_POST['template'];
   $template = file($tempFile);
   for ($j = 0; $j < $npspec; $j++) {
      $temp = trim($template[$j*4]);
      $labels[$j] = $temp;
      $temp = trim($template[$j*4+1]);
      $infields[$j] = preg_split("/\s+/", $temp);
      $temp = trim($template[$j*4+2]);
      $def[$j] = preg_split("/\s+/", $temp);
   }

   //$labels = array("Planting Detail","Finish Date","Container Details","Substrate Specs","CROP Parameters","Irrigation Schedule (enter FIXED, MAD or FILE under Sched)", "Fixed irrigation rates","Weather","LEAF Growth Parameters","Fertilizer","Pruning");

   if ($isSub == 3) {
      $temp = explode(" ", microtime(true));
      $timestamp = $temp[0];//+$temp[1];
      $id = substr($timestamp, 6, 7);
?>

<ul>
<li>Use this tool to compare different plant dates 
</ul>
<ol>
<li>Select the number of dates to compare
<li>Enter the different plant dates to compare
<li>Select the remaining management practices that will be the same for all comparisons 
<li>Click Submit to run simulations
</ol>

<form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="return checkRuns(true);" target="output">
<input type="hidden" name="isSubmitted" value=1>
<input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
<input type="hidden" name="id" value="<?php echo $id; ?>">
<table border=0 name="inputTable">
<?php
  require('user-save.php');
?>
<tr>
<td>
<b>Enter the number of dates to compare:</b>
<select name="nruns" onChange="setNRuns(this.selectedIndex+2);">
<option name="2" value="2">2
<option name="3" value="3">3
<option name="4" value="4" selected>4</select>
<br>
<hr>
</td>
</tr>
<tr>
<td><a href="Week_of_year_table.pdf" target="new">Click here for week of year table</a>
<br>
</td>
</tr>

<tr>
<td id="run1" name="run1">
  <input type="hidden" name="PLT_DOY_1" value=1>
  <b>Plant date 1:</b>
  <select name="month1" onChange="setDayList2(this.selectedIndex, document.paramForm.day1); setDay2(this, document.paramForm.day1, document.paramForm.PLT_DOY_1, true);">
  <?php
    for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value=' . $j;
      if ($j == 0) echo ' selected';
      echo '>';
      echo $months[$j];
    }
  ?>
  </select>

  <select name="day1" onChange="setDay2(document.paramForm.month1, this, document.paramForm.PLT_DOY_1, true);">
  <?php
    for ($j = 0; $j < 31; $j++) {
      echo '<option name=' . ($j+1) . ' value=' . ($j+1) . '>';
      echo ($j+1);
    }
  ?>
  </select>

  <input type="text" name="START_YR_1" size=4 readonly value="">
  <a href="#" onClick="updateCalendar2(cal, 'anchor1x', 1); return false;" name="anchor1x" id="anchor1x">select</a>

</td>
</tr>

<tr>
<td id="run2" name="run2">
  <input type="hidden" name="PLT_DOY_2" value=91>
  <b>Plant date 2:</b>
  <select name="month2" onChange="setDayList2(this.selectedIndex, document.paramForm.day2); setDay2(this, document.paramForm.day2, document.paramForm.PLT_DOY_2, true);">
  <?php
    for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value=' . $j;
      if ($j == 3) echo ' selected';
      echo '>';
      echo $months[$j];
    }
  ?>
  </select>

  <select name="day2" onChange="setDay2(document.paramForm.month2, this, document.paramForm.PLT_DOY_2, true);">
  <?php
    for ($j = 0; $j < 31; $j++) {
      echo '<option name=' . ($j+1) . ' value=' . ($j+1);
      if ($j == 0) echo ' selected';
      echo '>';
      echo ($j+1);
    }
  ?>
  </select>

  <input type="text" name="START_YR_2" size=4 readonly value="">
  <a href="#" onClick="updateCalendar2(cal, 'anchor2x', 2); return false;" name="anchor2x" id="anchor2x">select</a>

</td>
</tr>

<tr>
<td id="run3" name="run3">
  <input type="hidden" name="PLT_DOY_3" value=182>
  <b>Plant date 3:</b>
  <select name="month3" onChange="setDayList2(this.selectedIndex, document.paramForm.day3); setDay2(this, document.paramForm.day3, document.paramForm.PLT_DOY_3, true);">
  <?php
    for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value=' . $j;
      if ($j == 6) echo ' selected';
      echo '>';
      echo $months[$j];
    }
  ?>
  </select>

  <select name="day3" onChange="setDay2(document.paramForm.month3, this, document.paramForm.PLT_DOY_3, true);">
  <?php
    for ($j = 0; $j < 31; $j++) {
      echo '<option name=' . ($j+1) . ' value=' . ($j+1); 
      if ($j == 0) echo ' selected';
      echo '>';
      echo ($j+1);
    }
  ?>
  </select>

  <input type="text" name="START_YR_3" size=4 readonly value="">
  <a href="#" onClick="updateCalendar2(cal, 'anchor3x', 3); return false;" name="anchor3x" id="anchor3x">select</a>
</td>
</tr>

<tr>
<td id="run4" name="run4">
  <input type="hidden" name="PLT_DOY_4" value=274>
  <b>Plant date 4:</b>
  <select name="month4" onChange="setDayList2(this.selectedIndex, document.paramForm.day4); setDay2(this, document.paramForm.day4, document.paramForm.PLT_DOY_4, true);">
  <?php
    for ($j = 0; $j < 12; $j++) {
      echo '<option name="' . $months[$j] . '" value=' . $j;
      if ($j == 9) echo ' selected';
      echo '>';
      echo $months[$j];
    }
  ?>
  </select>

  <select name="day4" onChange="setDay2(document.paramForm.month4, this, document.paramForm.PLT_DOY_4, true);">
  <?php
    for ($j = 0; $j < 30; $j++) {
      echo '<option name=' . ($j+1) . ' value=' . ($j+1) . '>';
      echo ($j+1);
    }
  ?>
  </select>

  <input type="text" name="START_YR_4" size=4 readonly value="">
  <a href="#" onClick="updateCalendar2(cal, 'anchor4x', 4); return false;" name="anchor4x" id="anchor4x">select</a>
</td>
</tr>

<tr>
<td>
  <b>Select end year:</b>
  <select name="END_YR">
  <?php
    $currYr = date("Y");
    echo '<option name="' . $currYr . '" value="' . $currYr . '">' . $currYr;
  ?>
  </select>
  <hr>
  <br>
</td>
</tr>


<?php
  require('common.php');
  require('setDefault.php');
?>
<script language="javascript">updateYear2();</script>

<?php
   } else if ($isSub == 0) {
      $nruns = $_POST['nruns'];
      $xtitle = "Plant Date";
      $tablelabel = "plant date";
      $compnames = array("PLT_DOY");
      for ($j = 0; $j < $nruns; $j++) {
	$compvals[0][$j] = $_POST['PLT_DOY_' . ($j+1)];
        $xlabels[$j] = dateFromDoy($compvals[0][$j]);
      }
      require("compare_dorun.php");
   } else if ($isSub == 1 || $isSub == 2) {
      require("compare_saved.php");
   }
   if ($isSub < 3) {
      require("compare_output.php");
   }

   require('footer.php');
?>
