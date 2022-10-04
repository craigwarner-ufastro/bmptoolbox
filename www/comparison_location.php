<?php #ini_set('session.cache_limiter', 'private');
session_start(); ?>
<html>
<head>
<title>Compare Plant Dates</title>
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
  path += "&xticks=" + xticks + "&xtickv=" + xtickv + "&xextrema=" + xextrema + "&ymin=0";
  path += "&style=3&xtitle=Location&ytitle=" + ytitle;
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
      path += "&style=3&xtitle=Location&ytitle=" + ytitle;
      document[imgName].src = path; 
   }
}

function setNRuns(n) {
  var currWth;
  if (wthType == 0) {
    currWth = fawn;
  } else if (wthType == 1) {
    currWth = wth;
  } else if (wthType == 2) {
    currWth = userWth;
  }
  if (n < 4) {
    df = document.getElementById('run4');
    desc = '<br>';
    df.innerHTML = desc;
  } else {
    df = document.getElementById('run4');
    desc = '<b>Location 4:</b> ';
    desc += '<select name="WFNAME_4" onChange="updateYear(); updateUserWth(4);">';
    for (j = 0; j < currWth.length; j++) {
      if (j == 3) selTxt = " selected"; else selTxt = "";
      desc += '<option name="' + currWth[j] + '" value="' + currWth[j] + '" ' + selTxt + '>' + currWth[j].substring(currWth[j].lastIndexOf("/")+1).replace(/_/g," ");
    }
    desc += '</select>';
    desc += '<br>';
    df.innerHTML = desc;
  }
  if (n < 3) {
    df = document.getElementById('run3');
    desc = '';
    df.innerHTML = desc;
  } else if (nruns_old < 3) {
    df = document.getElementById('run3');
    desc = '<b>Location 3:</b> ';
    desc += '<select name="WFNAME_3" onChange="updateYear(); updateUserWth(3);">';
    for (j = 0; j < currWth.length; j++) {
      if (j == 2) selTxt = " selected"; else selTxt = "";
      desc += '<option name="' + currWth[j] + '" value="' + currWth[j] + '" ' + selTxt + '>' + currWth[j].substring(currWth[j].lastIndexOf("/")+1).replace(/_/g," ");
    }
    desc += '</select>';
    desc += '<br>';
    df.innerHTML = desc;
  }
  nruns_old = n;
  updateYear();
}

</script>

<?php require('header.php'); ?>

<h1 class="h1-container">Compare Locations<font color="#ffffff" class="h1-shadow">Compare Locations</font></h1>
<center><a href="comparison_fert.php">Compare Fertilizer Rates</a> |
<a href="comparison_irr.php">Compare Irrigation Schedules</a> |
<a href="comparison_fert_irr.php">Compare Fertilizer and Irrigation</a><br>
<a href="comparison_date.php">Compare Plant Dates</a> |
<b>Compare Locations</b>
</center>
<div ID="toolTips" style="position:absolute; left:25px; top:-50px; z-index:2"><br></div>
<?php
   $pagename = 'location';

   if (isset($_POST['isSubmitted'])) {
      $isSub = 0;
   } else if (isset($_GET['runid']) && $isLoggedIn) {
      $isSub = 1;
      $xtitle = "Location";
      $tablelabel = "location";
      $compnames = array("WFNAME");

      require("compare_setup_save.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['WFNAME' . ($j+1)];
        if (strrpos($compvals[0][$j], "/") !== false) {
          $xlabels[$j] = substr($compvals[0][$j], strrpos($compvals[0][$j], "/")+1);
        } else $xlabels[$j] = $compvals[0][$j];
        $xlabels[$j] = str_replace("_"," ",substr(str_replace("fawn/","",$xlabels[$j]),0,8));
        $xlabels[$j] = str_replace('"','',$xlabels[$j]);
      }
   } else if (isset($_POST['saveSubmitted'])) {
      $isSub = 2;
      $xtitle = "Location";
      $tablelabel = "location";
      $compnames = array("WFNAME");

      require("compare_save_submitted.php");

      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $input['WFNAME' . ($j+1)];
        if (strrpos($compvals[0][$j], "/") !== false) {
          $xlabels[$j] = substr($compvals[0][$j], strrpos($compvals[0][$j], "/")+1);
        } else $xlabels[$j] = $compvals[0][$j];
        $xlabels[$j] = str_replace("_"," ",substr(str_replace("fawn/","",$xlabels[$j]),0,8));
        $xlabels[$j] = str_replace('"','',$xlabels[$j]);
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
<li>Use this tool to compare results from different locations 
</ul>
<ol>
<li>Select the number of locations to compare
<li>Enter the different locations to compare 
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
<b>Enter the number of locations to compare:</b>
<select name="nruns" onChange="setNRuns(this.selectedIndex+2);">
<option name="2" value="2">2
<option name="3" value="3">3
<option name="4" value="4" selected>4</select>
<br>
<hr>
</td>
</tr>

<tr>
<td>
  <ul style="margin-top: 3px;">
  <input type="radio" name="wtype" value="FAWN" checked onClick="changeAllWeather(0);">Florida Automated Weather Network (FAWN) (35 locations)
  <br>
  <input type="radio" name="wtype" value="HIS" onClick="changeAllWeather(1);">Long-term (&gt;30 yrs) historical weather courtesy of AgroClimate (9 locations)
  <?php if ($isLoggedIn && count($userWth) > 0) {
    echo '<br>';
    echo '<input type="radio" name="wtype" value="USER" onClick="changeAllWeather(2);">My uploaded weather files';
  } ?>
  </ul>
  <table border=0>
  <tr><td valign="top" width=160 rowspan=5>
    <div id="weatherInfo" align="left" width=160>
    </div>
  </td>
  <td valign="top" rowspan=5>
    <img src="images/fawnmap2.png" USEMAP="#fawnMap" border=0 name="weatherImage">
    <map name="weatherMap">
    <?php
      for ($j=0; $j < count($wth); $j++) {
        echo '<area shape="circle" coords="' . $wthX[$j] . ',' . $wthY[$j] . ',15" ';
        echo 'alt="' . str_replace("_"," ",$wth[$j]) . '" ';
        echo 'title="' . str_replace("_"," ",$wth[$j]) . '" ';
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
        echo "onMouseOver=\"updateFawnLoc('" . $row[2] . "');\">\n";
      }
    ?>
    </map>
  </td>
  <td valign="top"></td>
  </tr>
  <tr>
  <td colspan=2></td>
  <td id="run1" name="run1">
    <b>Location 1:</b>
    <select name="WFNAME_1" onChange="updateYear(); updateUserWth(1);">
    <?php
      for ($j = 0; $j < count($fawn); $j++) {
        if ($j == 0) $selTxt = " selected"; else $selTxt = "";
        echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '" ' . $selTxt . '>' . str_replace("fawn/", "", str_replace("_"," ",$fawn[$j]));
      }
    ?>
    </select>
  </td>
  </tr>

  <tr>
  <td colspan=2></td>
  <td id="run2" name="run2">
    <b>Location 2:</b>
    <select name="WFNAME_2" onChange="updateYear(); updateUserWth(2);">
    <?php
      for ($j = 0; $j < count($fawn); $j++) {
        if ($j == 1) $selTxt = " selected"; else $selTxt = "";
        echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '" ' . $selTxt . '>' . str_replace("fawn/", "", str_replace("_"," ",$fawn[$j]));
      }
    ?>
    </select>
  </td>
  </tr>

  <tr>
  <td colspan=2></td>
  <td id="run3" name="run3">
    <b>Location 3:</b>
    <select name="WFNAME_3" onChange="updateYear(); updateUserWth(3);">
    <?php
      for ($j = 0; $j < count($fawn); $j++) {
        if ($j == 2) $selTxt = " selected"; else $selTxt = "";
        echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '" ' . $selTxt . '>' . str_replace("fawn/", "", str_replace("_"," ",$fawn[$j]));
      }
    ?>
    </select>
  </td>
  </tr>

  <tr>
  <td colspan=2></td>
  <td id="run4" name="run4">
    <b>Location 4:</b>
    <select name="WFNAME_4" onChange="updateYear(); updateUserWth(4);">
    <?php
      for ($j = 0; $j < count($fawn); $j++) {
        if ($j == 3) $selTxt = " selected"; else $selTxt = "";
        echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '" ' . $selTxt . '>' . str_replace("fawn/", "", str_replace("_"," ",$fawn[$j]));
      }
    ?>
    </select>
    <br>
  </td>
  </tr>
  </table>
<hr>
</td>
</tr>

<?php
  require('common.php');
  require('setDefault.php');
?>

<?php
   } else if ($isSub == 0) {
      $nruns = $_POST['nruns'];
      $xtitle = "Location";
      $tablelabel = "location";
      $compnames = array("WFNAME");
      for ($j = 0; $j < $nruns; $j++) {
        $compvals[0][$j] = $_POST['WFNAME_' . ($j+1)];
	if (strrpos($compvals[0][$j], "/") !== false) {
	  $xlabels[$j] = substr($compvals[0][$j], strrpos($compvals[0][$j], "/")+1);
	} else $xlabels[$j] = $compvals[0][$j];
        $xlabels[$j] = str_replace("_"," ",substr(str_replace("fawn/","",$xlabels[$j]),0,8)); 
        $xlabels[$j] = str_replace('"','',$xlabels[$j]);
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
