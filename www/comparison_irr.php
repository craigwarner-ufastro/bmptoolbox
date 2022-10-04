<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Compare Irrigation Schedules</title>
<script language="Javascript">
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
  path += "&style=3&xtitle=Irrigation schedule&ytitle=" + ytitle;
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
      path += "&xticks=" + xticks + "&xtickv=" + xtickv + "&xextrema=" + xextrema + "&ymin=0";
      path += "&style=3&xtitle=Irrigation schedule&ytitle=" + ytitle;
      document[imgName].src = path; 
   }
}

function updateIrr_0(x) {
   s1 = document.paramForm.irr1w_0.options[document.paramForm.irr1w_0.selectedIndex].value;
   s2 = document.paramForm.irr2w_0.options[document.paramForm.irr2w_0.selectedIndex].value;
   s3 = document.paramForm.irr3w_0.options[document.paramForm.irr3w_0.selectedIndex].value;
   if (s1 == "None") n1 = -99; else n1 = Math.floor(s1);
   if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   if (s3 == "None") n3 = -99; else n3 = Math.floor(s3);

   if (x == 1) {
      document.paramForm.irr2w_0.options.length=0;
      document.paramForm.irr2w_0.options[0] = new Option("None", "None", false, false);
      if (n1 != -99) for (j = n1+1; j <= n1+30; j++) {
        document.paramForm.irr2w_0.options[j-n1] = new Option(j, j, false, false);
        if (j == n2) document.paramForm.irr2w_0.options[j-n1].selected = true;
        if (j-n1 == 1 && n2 < j && s2 != 'None') document.paramForm.irr2w_0.options[j-n1].selected = true;
      }
      s2 = document.paramForm.irr2w_0.options[document.paramForm.irr2w_0.selectedIndex].value;
      if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   }

   if (x <= 2) {
      document.paramForm.irr3w_0.options.length=0;
      document.paramForm.irr3w_0.options[0] = new Option("None", "None", false, false);
      if (n2 != -99) for (j = n2+1; j <= n2+30; j++) {
        document.paramForm.irr3w_0.options[j-n2] = new Option(j, j, false, false);
        if (j == n3) document.paramForm.irr3w_0.options[j-n2].selected = true;
        if (j-n2 == 1 && n3 < j && s3 != 'None') document.paramForm.irr3w_0.options[j-n2].selected = true;
      }
      s3 = document.paramForm.irr3w_0.options[document.paramForm.irr3w_0.selectedIndex].value;
      if (s3 == "None") n3 = -99; else n3 = Math.floor(s3)
   }

   if (n1 == -99) {
      document.paramForm.IRR1_0.value = -99;
   } else document.paramForm.IRR1_0.value = n1*7;
   if (n2 == -99) {
      document.paramForm.IRR2_0.value = -99;
   } else document.paramForm.IRR2_0.value = n2*7;
   if (n3 == -99) {
      document.paramForm.IRR3_0.value = -99;
   } else document.paramForm.IRR3_0.value = n3*7;
}

function updateIrr_1(x) {
   s1 = document.paramForm.irr1w_1.options[document.paramForm.irr1w_1.selectedIndex].value;
   s2 = document.paramForm.irr2w_1.options[document.paramForm.irr2w_1.selectedIndex].value;
   s3 = document.paramForm.irr3w_1.options[document.paramForm.irr3w_1.selectedIndex].value;
   if (s1 == "None") n1 = -99; else n1 = Math.floor(s1);
   if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   if (s3 == "None") n3 = -99; else n3 = Math.floor(s3);

   if (x == 1) {
      document.paramForm.irr2w_1.options.length=0;
      document.paramForm.irr2w_1.options[0] = new Option("None", "None", false, false);
      if (n1 != -99) for (j = n1+1; j <= n1+30; j++) {
        document.paramForm.irr2w_1.options[j-n1] = new Option(j, j, false, false);
        if (j == n2) document.paramForm.irr2w_1.options[j-n1].selected = true;
        if (j-n1 == 1 && n2 < j && s2 != 'None') document.paramForm.irr2w_1.options[j-n1].selected = true;
      }
      s2 = document.paramForm.irr2w_1.options[document.paramForm.irr2w_1.selectedIndex].value;
      if (s2 == "None") n2 = -99; else n2 = Math.floor(s2);
   }

   if (x <= 2) {
      document.paramForm.irr3w_1.options.length=0;
      document.paramForm.irr3w_1.options[0] = new Option("None", "None", false, false);
      if (n2 != -99) for (j = n2+1; j <= n2+30; j++) {
        document.paramForm.irr3w_1.options[j-n2] = new Option(j, j, false, false);
        if (j == n3) document.paramForm.irr3w_1.options[j-n2].selected = true;
        if (j-n2 == 1 && n3 < j && s3 != 'None') document.paramForm.irr3w_1.options[j-n2].selected = true;
      }
      s3 = document.paramForm.irr3w_1.options[document.paramForm.irr3w_1.selectedIndex].value;
      if (s3 == "None") n3 = -99; else n3 = Math.floor(s3)
   }

   if (n1 == -99) {
      document.paramForm.IRR1_1.value = -99;
   } else document.paramForm.IRR1_1.value = n1*7;
   if (n2 == -99) {
      document.paramForm.IRR2_1.value = -99;
   } else document.paramForm.IRR2_1.value = n2*7;
   if (n3 == -99) {
      document.paramForm.IRR3_1.value = -99;
   } else document.paramForm.IRR3_1.value = n3*7;
}

function setNRuns(n) {
  if (n < 3) {
    df = document.getElementById('run2');
    desc = '';
    df.innerHTML = desc;

    df = document.getElementById('run3');
    desc = '<b>An ET-based schedule will be run for comparison</b>'
    desc += '<hr><br>';
    df.innerHTML = desc;
    
  } else {
    df = document.getElementById('run2');
    desc = '<b>Fixed irrigation schedule 2:</b>';
    desc+='<ul>';
    desc+='<table border=1 style="margin-left: 5px; margin-top: 5px;" cellpadding=5>';
    desc+='<tr><td>';
    desc+='<b>Automatically shutoff irrigation if rain exceeds daily rate: </b>';
    desc+='<input type="radio" name="RAINCUT_1" value="YES" checked">Yes';
    desc+='<input type="radio" name="RAINCUT_1" value="NO">No<br>';
    desc+='<b>Irrigation rate at start of season: </b>';
    desc+='<select name="D_IRR0_1in" onChange="document.paramForm.D_IRR0_1.value=this.value*2.54;">';
    for (j = 0; j <= 15; j++) {
      x = j/10.
      if (x == 0.8) defSel = ' selected'; else defSel = '';
      desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
    } 
    desc+='</select> inches per day<br>';

    desc+='<b>1st change in irrigation rate (optional): </b>';
    desc+='<select name="irr1w_1" onChange="updateIrr_1(1);">';
    desc+='<option name="None" value="None" selected>None';
    for (j = 1; j<=52; j++) {
      desc+='<option name=' + j + ' value=' + j + '>' + j;
    }
    desc+='</select> weeks after planting ';
    desc+='<select name="D_IRR1_1in" onChange="document.paramForm.D_IRR1_1.value=this.value*2.54;">';
    for (j = 0; j <= 15; j++) {
      x = j/10.
      if (x == 0.3) defSel = ' selected'; else defSel = '';
      desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
    } 
    desc+='</select> inches per day<br>';

    desc+='<b>2nd change in irrigation rate (optional): </b>';
    desc+='<select name="irr2w_1" onChange="updateIrr_1(2);">';
    desc+='<option name="None" value="None" selected>None';

    desc+='</select> weeks after planting ';
    desc+='<select name="D_IRR2_1in" onChange="document.paramForm.D_IRR2_1.value=this.value*2.54;">';
    for (j = 0; j <= 15; j++) {
      x = j/10.
      if (x == 0.4) defSel = ' selected'; else defSel = '';
      desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
    } 
    desc+='</select> inches per day<br>';

    desc+='<b>3rd change in irrigation rate (optional): </b>';
    desc+='<select name="irr3w_1" onChange="updateIrr_1(3);">';
    desc+='<option name="None" value="None" selected>None';

    desc+='</select> weeks after planting ';
    desc+='<select name="D_IRR3_1in" onChange="document.paramForm.D_IRR3_1.value=this.value*2.54;">';
    for (j = 0; j <= 15; j++) {
      x = j/10.
      if (x == 0.5) defSel = ' selected'; else defSel = '';
      desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
    } 
    desc+='</select> inches per day<br>';

    desc+='<input type="hidden" name="IRR1_1" value=-99>';
    desc+='<input type="hidden" name="IRR2_1" value=-99>';
    desc+='<input type="hidden" name="IRR3_1" value=-99>';

    desc+='</td></tr>';
    desc+='</table></ul>';
    df.innerHTML = desc;

    df = document.getElementById('run3');
    desc = '<b>An ET-based schedule will be run for comparison</b>'
    desc += '<hr><br>';
    df.innerHTML = desc;
  }
}

</script>

<?php require('header.php'); ?>

<h1 class="h1-container">Compare Irrigation Schedules<font color="#ffffff" class="h1-shadow">Compare Irrigation Schedules</font></h1>
<center><a href="comparison_fert.php">Compare Fertilizer Rates</a> |
<b>Compare Irrigation Schedules</b> |
<a href="comparison_fert_irr.php">Compare Fertilizer and Irrigation</a><br>
<a href="comparison_date.php">Compare Plant Dates</a> |
<a href="comparison_location.php">Compare Locations</a>
</center>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = 'irr';

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $xtitle = "Irrigation schedule";
      $tablelabel = "irrigation schedule";
      $compnames = array("RAINCUT", "SCHED");

      require("compare_setup_save.php");

      for ($j = 0; $j < $nruns; $j++) {
        if ($j < $nruns-1) $compvals[0][$j] = $input['RAINCUT' . ($j+1)];
        $compvals[1][$j] = ($j == $nruns-1) ? "MAD" : "FIXED";
        $xlabels[$j] = ($j == $nruns-1) ? "ET" : "Fixed " . ($j+1);
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      $xtitle = "Irrigation schedule";
      $tablelabel = "irrigation schedule";
      $compnames = array("RAINCUT", "SCHED");

      require("compare_save_submitted.php");

      for ($j = 0; $j < $nruns; $j++) {
        if ($j < $nruns-1) $compvals[0][$j] = $input['RAINCUT' . ($j+1)];
        $compvals[1][$j] = ($j == $nruns-1) ? "MAD" : "FIXED";
        $xlabels[$j] = ($j == $nruns-1) ? "ET" : "Fixed " . ($j+1);
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
<li>Use this tool to compare different irrigation schedules
</ul>
<ol>
<li>Select the number of irrigation schedules to compare
<li>Enter a fixed irrigation schedule
<li>If comparing three irrigation schedules, enter a second fixed irrigation schedule
<li><b>An ET-based schedule will automatically be run for comparison</b>
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
<b>Enter the number of irrigation schedules to compare:</b>
<select name="nruns" onChange="setNRuns(this.selectedIndex+2);">
<option name="2" value="2">2
<option name="3" value="3" selected>3
</select>
<br>
<hr>
</td>
</tr>
<tr>
<td id="run1" name="run1">
  <input type="hidden" name="D_IRR0_0" value=1.016>
  <input type="hidden" name="D_IRR1_0" value=0.762>
  <input type="hidden" name="D_IRR2_0" value=1.016>
  <input type="hidden" name="D_IRR3_0" value=1.27>
  <input type="hidden" name="D_IRR0_1" value=2.032>
  <input type="hidden" name="D_IRR1_1" value=0.762>
  <input type="hidden" name="D_IRR2_1" value=1.016>
  <input type="hidden" name="D_IRR3_1" value=1.27>
  <b>Fixed irrigation schedule 1:</b>
  <ul>
  <table border=1 style="margin-left: 5px; margin-top: 5px;" cellpadding=5>
  <tr><td>
  <b>Automatically shutoff irrigation if rain exceeds daily rate: </b>
  <input type="radio" name="RAINCUT_0" value="YES" checked>Yes
  <input type="radio" name="RAINCUT_0" value="NO">No<br>
  <b>Irrigation rate at start of season: </b>
  <select name="D_IRR0_0in" onChange="document.paramForm.D_IRR0_0.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.4) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>1st change in irrigation rate (optional): </b>
  <select name="irr1w_0" onChange="updateIrr_0(1);">
  <option name="None" value="None" selected>None
  <?php
    for ($j = 1; $j < 30; $j++) {
      echo '<option name=' . $j . ' value=' . $j . '>' . $j;
    }
  ?>
  </select> weeks after planting
  <select name="D_IRR1_0in" onChange="document.paramForm.D_IRR1_0.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.3) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>2nd change in irrigation rate (optional): </b>
  <select name="irr2w_0" onChange="updateIrr_0(2);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR2_0in" onChange="document.paramForm.D_IRR2_0.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.4) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>3rd change in irrigation rate (optional): </b>
  <select name="irr3w_0" onChange="updateIrr_0(3);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR3_0in" onChange="document.paramForm.D_IRR3_0.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.5) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <input type="hidden" name="IRR1_0" value=-99>
  <input type="hidden" name="IRR2_0" value=-99>
  <input type="hidden" name="IRR3_0" value=-99>

  </td></tr>
  </table></ul>
</td>
</tr>

<tr>
<td id="run2" name="run2">

  <b>Fixed irrigation schedule 2:</b>
  <?php
    $inches = array(0.1, 0.2, 0.25, 0.3, 0.4, 0.5, 0.75, 1, 1.5);
  ?>
  <ul>
  <table border=1 style="margin-left: 5px; margin-top: 5px;" cellpadding=5>
  <tr><td>
  <b>Automatically shutoff irrigation if rain exceeds daily rate: </b>
  <input type="radio" name="RAINCUT_1" value="YES" checked>Yes
  <input type="radio" name="RAINCUT_1" value="NO">No<br>
  <b>Irrigation rate at start of season: </b>
  <select name="D_IRR0_1in" onChange="document.paramForm.D_IRR0_1.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.8) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>1st change in irrigation rate (optional): </b>
  <select name="irr1w_1" onChange="updateIrr_1(1);">
  <option name="None" value="None" selected>None
  <?php
    for ($j = 1; $j < 30; $j++) {
      echo '<option name=' . $j . ' value=' . $j . '>' . $j;
    }
  ?>
  </select> weeks after planting
  <select name="D_IRR1_1in" onChange="document.paramForm.D_IRR1_1.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.3) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>2nd change in irrigation rate (optional): </b>
  <select name="irr2w_1" onChange="updateIrr_1(2);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR2_1in" onChange="document.paramForm.D_IRR2_1.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.5) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <b>3rd change in irrigation rate (optional): </b>
  <select name="irr3w_1" onChange="updateIrr_1(3);">
  <option name="None" value="None" selected>None

  </select> weeks after planting
  <select name="D_IRR3_1in" onChange="document.paramForm.D_IRR3_1.value=this.value*2.54;">
  <?php
    for ($j = 0; $j <= 15; $j++) {
      $inches = $j/10.;
      if ($inches == 0.5) $defSel = ' selected'; else $defSel = '';
      echo '<option name=' . $inches . ' value=' . $inches . $defSel . '>' . $inches;
    }
  ?>
  </select> inches per day<br>

  <input type="hidden" name="IRR1_1" value=-99>
  <input type="hidden" name="IRR2_1" value=-99>
  <input type="hidden" name="IRR3_1" value=-99>

  </td></tr>
  </table></ul>

</td>
</tr>

<tr>
<td id="run3" name="run3">
  <b>Run 3 will be an ET-based schedule for comparison</b>
  <hr><br>
</td>
</tr>

<?php
  require('common.php');
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $nruns = $_POST['nruns'];
      $xtitle = "Irrigation schedule";
      $tablelabel = "irrigation schedule";
      $compnames = array("RAINCUT", "SCHED");
      for ($j = 0; $j < $nruns; $j++) {
        if ($j < $nruns-1) $compvals[0][$j] = $_POST['RAINCUT_' . ($j)];
	$compvals[1][$j] = ($j == $nruns-1) ? "MAD" : "FIXED"; 
        $xlabels[$j] = ($j == $nruns-1) ? "ET" : "Fixed " . ($j+1); 
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
