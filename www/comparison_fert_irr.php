<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Compare Fertilizer and Irrigation</title>
<script language="Javascript">
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
  path += "&xticks=1,2,3,4&xtickv=0.6,1.6,2.6,3.6&xextrema=0,4&ymin=0";
  path += "&style=3&xtitle=Run&ytitle=" + ytitle;
  return path;
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
      path += "&xticks=1,2,3,4&xtickv=0.6,1.6,2.6,3.6&xextrema=0,4&ymin=0";
      path += "&style=3&xtitle=Run&ytitle=" + ytitle;
      document[imgName].src = path; 
   }
}

function updateIrr(x) {
   s1 = document.paramForm.irr1w.options[document.paramForm.irr1w.selectedIndex].value;
   s2 = document.paramForm.irr2w.options[document.paramForm.irr2w.selectedIndex].value;
   s3 = document.paramForm.irr3w.options[document.paramForm.irr3w.selectedIndex].value;
   if (s1 == "None") n1 = -99; else n1 = Math.floor(s1);
   if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   if (s3 == "None") n3 = -99; else n3 = Math.floor(s3);

   if (x == 1) {
      document.paramForm.irr2w.options.length=0;
      document.paramForm.irr2w.options[0] = new Option("None", "None", false, false);
      if (n1 != -99) for (j = n1+1; j <= n1+30; j++) {
        document.paramForm.irr2w.options[j-n1] = new Option(j, j, false, false);
        if (j == n2) document.paramForm.irr2w.options[j-n1].selected = true;
        if (j-n1 == 1 && n2 < j && s2 != 'None') document.paramForm.irr2w.options[j-n1].selected = true;
      }
      s2 = document.paramForm.irr2w.options[document.paramForm.irr2w.selectedIndex].value;
      if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   }

   if (x <= 2) {
      document.paramForm.irr3w.options.length=0;
      document.paramForm.irr3w.options[0] = new Option("None", "None", false, false);
      if (n2 != -99) for (j = n2+1; j <= n2+30; j++) {
        document.paramForm.irr3w.options[j-n2] = new Option(j, j, false, false);
        if (j == n3) document.paramForm.irr3w.options[j-n2].selected = true;
        if (j-n2 == 1 && n3 < j && s3 != 'None') document.paramForm.irr3w.options[j-n2].selected = true;
      }
      s3 = document.paramForm.irr3w.options[document.paramForm.irr3w.selectedIndex].value;
      if (s3 == "None") n3 = -99; else n3 = Math.floor(s3)
   }

   if (n1 == -99) {
      document.paramForm.IRR1.value = -99;
   } else document.paramForm.IRR1.value = n1*7;
   if (n2 == -99) {
      document.paramForm.IRR2.value = -99;
   } else document.paramForm.IRR2.value = n2*7;
   if (n3 == -99) {
      document.paramForm.IRR3.value = -99;
   } else document.paramForm.IRR3.value = n3*7;
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
  }
}

</script>

<?php require('header.php'); ?>

<h1 class="h1-container">Compare Fertilizer and Irrigation<font color="#ffffff" class="h1-shadow">Compare Fertilizer and Irrigation</font></h1>
<center><a href="comparison_fert.php">Compare Fertilizer Rates</a> |
<a href="comparison_irr.php">Compare Irrigation Schedules</a> |
<b>Compare Fertilizer and Irrigation</b><br>
<a href="comparison_date.php">Compare Plant Dates</a> |
<a href="comparison_location.php">Compare Locations</a>
</center>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = "fert_irr";

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $xtitle = "Run";
      $tablelabel = "fertilizer and irrigation";
      $compnames = array("FERT", "SCHED");

      require("compare_setup_save.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['FERT' . ($j+1)];
        $compvals[1][$j] = ($j > 1) ? "MAD" : "FIXED";
        $xlabels[$j] = "" . ($j+1);
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      $xtitle = "Run";
      $tablelabel = "fertilizer and irrigation";
      $compnames = array("FERT", "SCHED");

      require("compare_save_submitted.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['FERT' . ($j+1)];
        $compvals[1][$j] = ($j > 1) ? "MAD" : "FIXED";
        $xlabels[$j] = "" . ($j+1);
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
<li>Use this tool to compare two different fertilizer rates using two different irrigation schedules.
</ul>
<ol>
<li>Enter two different fertilizer rates
<li>Select a fixed irrigation schedule
<li>An ET-based schedule will automatically be run for comparison
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
<td id="run1" name="run1">
    <b>Fertilizer rate #1:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT1" value=11.865>
      <input type="radio" name="fertb1" onClick="changeFert(0, 1);" checked>Enter lb N per cubic yard:
      <select name="lbrate1" onChange="changeFert(0, 1);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 15) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      <input type="radio" name="fertb1" onClick="changeFert(1, 1);">Enter g of fertilizer per container:
      <select name="grate1" onChange="changeFert(1, 1);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 12) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . $j . ' value=' . $j . $defSel . '>' . $j;
        }
      ?>
      </select><br>
      </ul>
</td>
</tr>

<tr>
<td id="run2" name="run2">
    <b>Fertilizer rate #2:</b>
    <ul style="margin-top: 3px;">
      <input type="hidden" name="FERT2" value=19.775>
      <div id="fertb2lb">Enter lb N per cubic yard:
      <select name="lbrate2" onChange="changeFert(0, 2);">
      <?php
        for ($j = 5; $j <=40; $j++) {
           if ($j == 25) $defSel = " selected"; else $defSel = "";
           echo '<option name=' . ($j/10.) . ' value=' . ($j/10.) . $defSel . '>' . ($j/10.);
        }
      ?>
      </select><br>
      </div>
      <div id="fertb2g" style="display:none">Enter g of fertilizer per container:
      <select name="grate2" onChange="changeFert(1, 2);">
      <?php
        for ($j = 2; $j <=260; $j++) {
           if ($j == 20) $defSel = " selected"; else $defSel = "";
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
  <input type="hidden" name="D_IRR0" value=1.016>
  <input type="hidden" name="D_IRR1" value=0.762>
  <input type="hidden" name="D_IRR2" value=1.016>
  <input type="hidden" name="D_IRR3" value=1.27>
  <b>Fixed irrigation schedule:</b>
  <?php
    $inches = array(0.1, 0.2, 0.25, 0.3, 0.4, 0.5, 0.75, 1, 1.5);
  ?>
  <ul>
  <table border=1 style="margin-left: 5px; margin-top: 5px;" cellpadding=5>
  <tr><td>
  <b>Automatically shutoff irrigation if rain exceeds daily rate: </b>
  <input type="radio" name="RAINCUT" value="YES" checked>Yes
  <input type="radio" name="RAINCUT" value="NO">No<br>
  <b>Irrigation rate at start of season: </b>
  <select name="D_IRR0in" onChange="document.paramForm.D_IRR0.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.4) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>1st change in irrigation rate (optional): </b>
  <select name="irr1w" onChange="updateIrr(1);">
  <option name="None" value="None" selected>None
  <?php
    for ($j = 1; $j < 30; $j++) {
      echo '<option name=' . $j . ' value=' . $j . '>' . $j;
    }
  ?>
  </select> weeks after planting
  <select name="D_IRR1in" onChange="document.paramForm.D_IRR1.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.3) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>2nd change in irrigation rate (optional): </b>
  <select name="irr2w" onChange="updateIrr(2);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR2in" onChange="document.paramForm.D_IRR2.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.4) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>3rd change in irrigation rate (optional): </b>
  <select name="irr3w" onChange="updateIrr(3);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR3in" onChange="document.paramForm.D_IRR3.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.5) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <input type="hidden" name="IRR1" value=-99>
  <input type="hidden" name="IRR2" value=-99>
  <input type="hidden" name="IRR3" value=-99>

  </td></tr>
  </table></ul>
  <hr><br>
</td>
</tr>

<?php
  require('common.php');
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $nruns = 4; 
      $xtitle = "Run";
      $tablelabel = "fertilizer and irrigation";
      $compnames = array("FERT", "SCHED");
      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $_POST['FERT' . ($j%2+1)];
        $compvals[1][$j] = ($j > 1) ? "MAD" : "FIXED";
        $xlabels[$j] = "" . ($j+1);
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
