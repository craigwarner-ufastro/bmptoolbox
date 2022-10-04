<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Compare Fertilizer Rates</title>
<script language="Javascript">
var nruns_old = 4;
//vars for plots
var xplot = new Array(); 
var yplot = new Array();
var costplot = new Array();

function getGraphURL(name, id, index, ytitle) {
  var xvals = xplot[0];
  var yvals = yplot[0][index];
  var xmin = xplot[0];
  var xmax = xplot[0];
  for (j = 1; j < xplot.length; j++) {
    xvals += ',' + xplot[j];
    yvals += ',' + yplot[j][index];
    if (xplot[j] < xmin) xmin = xplot[j];
    if (xplot[j] > xmax) xmax = xplot[j];
  }
  path = "compgraph.php?xvals=" + xvals + "&yvals=" + yvals;
  path += '&ymin=0&xextrema=' + (xmin-0.2) + ',' + (xmax+0.2);
  path += "&xtitle=Fertilizer rate (lb N/cu yd)&ytitle=" + ytitle;
  return path;
}

function updateGraph(imgName, n, index, ytitle) {
   if (document.images) {
      var xvals = xplot[0];
      var yvals = yplot[0][index];
      if (n == 1) yvals = costplot[0][index];
      var xmin = xplot[0];
      var xmax = xplot[0];
      for (j = 1; j < xplot.length; j++) {
        xvals += ',' + xplot[j];
        if (n == 0) {
          yvals += ',' + yplot[j][index];
        } else if (n == 1) {
          yvals += ',' + costplot[j][index];
        }
	if (xplot[j] < xmin) xmin = xplot[j];
	if (xplot[j] > xmax) xmax = xplot[j];
      }
      path = "compgraph.php?xvals=" + xvals + "&yvals=" + yvals;
      path += '&ymin=0&xextrema=' + (xmin-0.2) + ',' + (xmax+0.2);
      path += "&xtitle=Fertilizer rate (lb N/cu yd)&ytitle=" + ytitle;
      document[imgName].src = path; 
   }
}

function changeFert2(x, n) {
  document.paramForm.fertb1[x].click();
  if (n == 1) {
    if (x == 0) {
      var a = document.paramForm.lbrate1.options[document.paramForm.lbrate1.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT1.value = a/document.paramForm.PCT_N.value;
      var grate = document.paramForm.grate1;
      for (j = 0; j < grate.options.length; j++) {
        if (grate.options[j].value == Math.round(document.paramForm.FERT1.value)) grate.selectedIndex = j;
      }
      if (document.paramForm.FERT1.value < parseInt(grate.options[0].value)) grate.selectedIndex = 0;
      document.getElementById("fertb2lb").style.display = '';
      document.getElementById("fertb2g").style.display = 'none';
      var a = document.paramForm.lbrate2.options[document.paramForm.lbrate2.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT2.value = a/document.paramForm.PCT_N.value;
      if (document.getElementById("fertb3lb") != null) {
	document.getElementById("fertb3lb").style.display = '';
	document.getElementById("fertb3g").style.display = 'none';
	var a = document.paramForm.lbrate3.options[document.paramForm.lbrate3.selectedIndex].value * 0.059325 * subVol;
	document.paramForm.FERT3.value = a/document.paramForm.PCT_N.value;
      }
      if (document.getElementById("fertb4lb") != null) {
	document.getElementById("fertb4lb").style.display = '';
	document.getElementById("fertb4g").style.display = 'none';
	var a = document.paramForm.lbrate4.options[document.paramForm.lbrate4.selectedIndex].value * 0.059325 * subVol;
	document.paramForm.FERT4.value = a/document.paramForm.PCT_N.value;
      }
    } else if (x == 1) {
      document.paramForm.FERT1.value = document.paramForm.grate1.options[document.paramForm.grate1.selectedIndex].value;
      var a = document.paramForm.FERT1.value*document.paramForm.PCT_N.value/(0.059325 * subVol);
      var lbrate = document.paramForm.lbrate1;
      for (j = 0; j < lbrate.options.length; j++) {
        if (lbrate.options[j].value*10 == Math.round(a*10)) lbrate.selectedIndex = j;
      }
      if (a > lbrate.options[j-1].value) lbrate.selectedIndex = j-1;
      document.getElementById("fertb2lb").style.display = 'none';
      document.getElementById("fertb2g").style.display = '';
      document.paramForm.FERT2.value = document.paramForm.grate2.options[document.paramForm.grate2.selectedIndex].value;
      if (document.getElementById("fertb3lb") != null) {
	document.getElementById("fertb3lb").style.display = 'none';
	document.getElementById("fertb3g").style.display = '';
	document.paramForm.FERT3.value = document.paramForm.grate3.options[document.paramForm.grate3.selectedIndex].value;
      }
      if (document.getElementById("fertb4lb") != null) {
	document.getElementById("fertb4lb").style.display = 'none';
	document.getElementById("fertb4g").style.display = '';
	document.paramForm.FERT4.value = document.paramForm.grate4.options[document.paramForm.grate4.selectedIndex].value;
      }
    }
  } else if (n == 2) {
    if (x == 0) {
      var a = document.paramForm.lbrate2.options[document.paramForm.lbrate2.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT2.value = a/document.paramForm.PCT_N.value;
      var grate = document.paramForm.grate2;
      for (j = 0; j < grate.options.length; j++) {
        if (grate.options[j].value == Math.round(document.paramForm.FERT2.value)) grate.selectedIndex = j;
      }
      if (document.paramForm.FERT2.value < parseInt(grate.options[0].value)) grate.selectedIndex = 0;
    } else if (x == 1) {
      document.paramForm.FERT2.value = document.paramForm.grate2.options[document.paramForm.grate2.selectedIndex].value;
      var a = document.paramForm.FERT2.value*document.paramForm.PCT_N.value/(0.059325 * subVol);
      var lbrate = document.paramForm.lbrate2;
      for (j = 0; j < lbrate.options.length; j++) {
        if (lbrate.options[j].value*10 == Math.round(a*10)) lbrate.selectedIndex = j;
      }
      if (a > lbrate.options[j-1].value) lbrate.selectedIndex = j-1;
    }
  } else if (n == 3) {
    if (x == 0) {
      var a = document.paramForm.lbrate3.options[document.paramForm.lbrate3.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT3.value = a/document.paramForm.PCT_N.value;
      var grate = document.paramForm.grate3;
      for (j = 0; j < grate.options.length; j++) {
        if (grate.options[j].value == Math.round(document.paramForm.FERT3.value)) grate.selectedIndex = j;
      }
      if (document.paramForm.FERT3.value < parseInt(grate.options[0].value)) grate.selectedIndex = 0;
    } else if (x == 1) {
      document.paramForm.FERT3.value = document.paramForm.grate3.options[document.paramForm.grate3.selectedIndex].value;
      var a = document.paramForm.FERT3.value*document.paramForm.PCT_N.value/(0.059325 * subVol);
      var lbrate = document.paramForm.lbrate3;
      for (j = 0; j < lbrate.options.length; j++) {
        if (lbrate.options[j].value*10 == Math.round(a*10)) lbrate.selectedIndex = j;
      }
      if (a > lbrate.options[j-1].value) lbrate.selectedIndex = j-1;
    }
  } else if (n == 4) {
    if (x == 0) {
      var a = document.paramForm.lbrate4.options[document.paramForm.lbrate4.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT4.value = a/document.paramForm.PCT_N.value;
      var grate = document.paramForm.grate4;
      for (j = 0; j < grate.options.length; j++) {
        if (grate.options[j].value == Math.round(document.paramForm.FERT4.value)) grate.selectedIndex = j;
      }
      if (document.paramForm.FERT4.value < parseInt(grate.options[0].value)) grate.selectedIndex = 0;
    } else if (x == 1) {
      document.paramForm.FERT4.value = document.paramForm.grate4.options[document.paramForm.grate4.selectedIndex].value;
      var a = document.paramForm.FERT4.value*document.paramForm.PCT_N.value/(0.059325 * subVol);
      var lbrate = document.paramForm.lbrate4;
      for (j = 0; j < lbrate.options.length; j++) {
        if (lbrate.options[j].value*10 == Math.round(a*10)) lbrate.selectedIndex = j;
      }
      if (a > lbrate.options[j-1].value) lbrate.selectedIndex = j-1;
    }
  }
}

function setNRuns(n) {
  if (n < 4) {
    df = document.getElementById('run4');
    desc = '<hr><br>';
    df.innerHTML = desc;
  } else {
    df = document.getElementById('run4');
    desc = '<b>Fertilizer rate 4:</b>';
    desc += '<ul style="margin-top: 3px;">';
    desc += '<input type="hidden" name="FERT4" value=19.775>';
    desc += '<div id="fertb4lb">Enter lb N per cubic yard:';
    desc += '<select name="lbrate4" onChange="changeFert(0, 4);">';
    for (j = 5; j <=40; j++) {
      if (j == 25) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + (j/10.) + ' value=' + (j/10.) + defSel + '>' + (j/10.);
    }
    desc += '</select><br>';
    desc += '</div>';
    desc += '<div id="fertb4g" style="display:none">Enter g of fertilizer per container:';
    desc += '<select name="grate4" onChange="changeFert(1, 4);">';
    for (j = 2; j <= 260; j++) {
      if (j == 20) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + j + ' value=' + j + defSel + '>' + j;
    }
    desc += '</select><br>';
    desc += '</div>';
    desc += '</ul><hr><br>';
    df.innerHTML = desc;
  }
  if (n < 3) {
    df = document.getElementById('run3');
    desc = '';
    df.innerHTML = desc;
  } else if (nruns_old < 3) {
    df = document.getElementById('run3');
    desc = '<b>Fertilizer rate 3:</b>';
    desc += '<ul style="margin-top: 3px;">';
    desc += '<input type="hidden" name="FERT3" value=15.82>';
    desc += '<div id="fertb3lb">Enter lb N per cubic yard:';
    desc += '<select name="lbrate3" onChange="changeFert(0, 3);">';
    for (j = 5; j <=40; j++) {
      if (j == 20) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + (j/10.) + ' value=' + (j/10.) + defSel + '>' + (j/10.);
    }
    desc += '</select><br>';
    desc += '</div>';
    desc += '<div id="fertb3g" style="display:none">Enter g of fertilizer per container:';
    desc += '<select name="grate3" onChange="changeFert(1, 3);">';
    for (j = 2; j <=260; j++) {
      if (j == 16) defSel = ' selected'; else defSel = '';
      desc += '<option name=' + j + ' value=' + j + defSel + '>' + j;
    }
    desc += '</select><br>';
    desc += '</div>';
    desc += '</ul>';
    df.innerHTML = desc;
  }
  nruns_old = n;
}

</script>

<?php require('header.php'); ?>

<h1 class="h1-container">Compare Fertilizer Rates<font color="#ffffff" class="h1-shadow">Compare Fertilizer Rates</font></h1>
<center><b>Compare Fertilizer Rates</b> |
<a href="comparison_irr.php">Compare Irrigation Schedules</a> |
<a href="comparison_fert_irr.php">Compare Fertilizer and Irrigation</a><br>
<a href="comparison_date.php">Compare Plant Dates</a> |
<a href="comparison_location.php">Compare Locations</a>
</center>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = "fert";

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $xtitle = "Fertilizer rate (lb N/cu yd)";
      $tablelabel = "Fertilizer rate";
      $compnames = array("FERT");

      require("compare_setup_save.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['FERT' . ($j+1)];
        $xlabels[$j] = outFormat("" . $compvals[0][$j]*$input['PCT_N']*16.86/$subVol, 2);
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      $xtitle = "Fertilizer rate (lb N/cu yd)";
      $tablelabel = "Fertilizer rate";
      $compnames = array("FERT");

      require("compare_save_submitted.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['FERT' . ($j+1)];
        $xlabels[$j] = outFormat("" . $compvals[0][$j]*$input['PCT_N']*16.86/$subVol, 2);
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
<li>Use this tool to compare different fertilizer rates using the same fertilizer.
</ul>
<ol>
<li>Select the number of rates to compare
<li>Enter the different fertilizer rates to compare
<li>Select the remaining management practices that will be the same for all comparisons
<li>Click Submit to run simulations
</ol>

<form action="<?php echo $_SERVER['PHP_SELF'];?>" name="paramForm" method="POST" onSubmit="return checkRuns(true);"  target="output">
<input type="hidden" name="isSubmitted" value=1>
<input type="hidden" name="timestamp" value="<?php echo $timestamp; ?>">
<input type="hidden" name="id" value="<?php echo $id; ?>">
<table border=0 name="inputTable">
<?php
  require('user-save.php');
?>
<tr>
<td>
<b>Enter the number of rates to compare:</b>
<select name="nruns" onChange="setNRuns(this.selectedIndex+2);">
<option name="2" value="2">2
<option name="3" value="3">3
<option name="4" value="4" selected>4</select>
<br>
<hr>
</td>
</tr>

<tr>
<td id="run1" name="run1">
    <b>Fertilizer rate 1:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT1" value=7.91>
      <input type="radio" name="fertb1" onClick="changeFert(0, 1);" checked>Enter lb N per cubic yard:
      <select name="lbrate1" onChange="changeFert(0, 1);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 10) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      <input type="radio" name="fertb1" onClick="changeFert(1, 1);">Enter g of fertilizer per container:
      <select name="grate1" onChange="changeFert(1, 1);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 8) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
      </ul>
</td>
</tr>

<tr>
<td id="run2" name="run2">
    <b>Fertilizer rate 2:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT2" value=11.865>
      <div id="fertb2lb">Enter lb N per cubic yard:
      <select name="lbrate2" onChange="changeFert(0, 2);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 15) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      </div>
      <div id="fertb2g" style="display:none">Enter g of fertilizer per container:
      <select name="grate2" onChange="changeFert(1, 2);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 12) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
      </div>
      </ul>
</td>
</tr>

<tr>
<td id="run3" name="run3">
    <b>Fertilizer rate 3:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT3" value=15.82>
      <div id="fertb3lb">Enter lb N per cubic yard:
      <select name="lbrate3" onChange="changeFert(0, 3);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 20) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      </div>
      <div id="fertb3g" style="display:none">Enter g of fertilizer per container:
      <select name="grate3" onChange="changeFert(1, 3);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 16) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
      </div>
      </ul>
</td>
</tr>

<tr>
<td id="run4" name="run4">
    <b>Fertilizer rate 4:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT4" value=19.775>
      <div id="fertb4lb">Enter lb N per cubic yard:
      <select name="lbrate4" onChange="changeFert(0, 4);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 25) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      </div>
      <div id="fertb4g" style="display:none">Enter g of fertilizer per container:
      <select name="grate4" onChange="changeFert(1, 4);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 20) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
      </div>
      </ul>
    <hr>
    <br>
</td>
</tr>

<?php
  require('common.php');
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $nruns = $_POST['nruns'];
      $xtitle = "Fertilizer rate (lb N/cu yd)";
      $tablelabel = "Fertilizer rate";
      $compnames = array("FERT");
      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $_POST['FERT' . ($j+1)];
        $xlabels[$j] = outFormat("" . $compvals[0][$j]*$_POST['PCT_N']*16.86/$subVol, 2);
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
