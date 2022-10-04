<?php session_start(); ?>
<html>
<head>
<title>Container Crop Management Tool</title>
<?php require('header.php'); ?> 
<h1 class="h1-container">CCROP Code and Input Files<font color="#ffffff" class="h1-shadow">CCROP Code and Input Files</font></h1>
<?php
  function isValidFile($file) {
    $rp = realpath($file);
    if (!file_exists($file)) return false; 
    if (substr($rp, 0, 6) != '/home/') return false;
    return true;
  }

  if (isset($_GET['file'])) $file = $_GET['file']; else $file = '';
  if (isset($_GET['type'])) $type = $_GET['type']; else $type = '';
  $cat = 1; 
  if (isset($_GET['cat'])) $cat = $_GET['cat'];

  echo '<center>';
  if ($cat != 1) echo '<a href="sourcecode.php?cat=1" class="myaccount">Fortran Code</a> | '; else echo 'Fortran Code | ';
  if ($cat != 2) echo '<a href="sourcecode.php?cat=2" class="myaccount">AgroClimate Weather</a> | '; else echo 'AgroClimate Weather | ';
  if ($cat != 3) echo '<a href="sourcecode.php?cat=3" class="myaccount">FAWN Weather</a> | '; else echo 'FAWN Weather | ';
  if ($cat != 4) echo '<a href="sourcecode.php?cat=4" class="myaccount">Input Spec Files</a> | '; else echo 'Input Spec Files | ';
  echo '<a href="../CCROP_Parameters.pdf" target="ccmt" class="myaccount">Parameter List</a> | ';
  echo '<a href="UserManual.pdf" target="ccmt" class="myaccount">User Manual</a>';
  echo '</center><br>';

  if ($cat == 1) {
?>
    <ul>
      <li><a href="sourcecode.php?file=Driver&type=fortran&cat=1">Driver.for</a>
      -- The main program for the Container Crop Resource Optimization Program (CCROP).
      <li><a href="sourcecode.php?file=Nutrient&type=fortran&cat=1">Nutrient.for</a>
      -- The nutrient subroutine for CCROP.
      <li><a href="sourcecode.php?file=Output&type=fortran&cat=1">Output.for</a>
      -- The output subroutine for CCROP.
      <li><a href="sourcecode.php?file=Plant&type=fortran&cat=1">Plant.for</a>
      -- The plant subroutine for CCROP.
      <li><a href="sourcecode.php?file=Water&type=fortran&cat=1">Water.for</a>
      -- The water subroutine for CCROP.
    </ul>
<?php
  } else if ($cat == 2) {
    echo '<form name="sourceForm" method="GET" action="sourcecode.php">';
    echo '<input type="hidden" name="cat" value=2>';
    echo '<input type="hidden" name="type" value="agro">';
    $wth = array("Belle_Glade", "Clermont", "Fort_Myers", "Gainesville", "Jacksonville", "Milton", "Plant_City", "Quincy", "Tamiami_Trail_Dade");
    echo '<center>Select a location: ';
    echo '<select name="file">';
    for ($j = 0; $j < count($wth); $j++) {
      echo '<option name="' . $wth[$j] . '" value="' . $wth[$j] . '">' . str_replace("_"," ",$wth[$j]);
    }
    echo '</select>';
    echo '<input type="submit" name="Go" value="Go">';
    echo '</center>';
  } else if ($cat == 3) {
    echo '<form name="sourceForm" method="GET" action="sourcecode.php">';
    echo '<input type="hidden" name="cat" value=3>';
    echo '<input type="hidden" name="type" value="fawn">';
    $fawn = array();
    $f = opendir("../fawn");
    while ($temp = readdir($f)) {
      if (preg_match("/.wth/i", $temp)) {
      #if (eregi('.wth', $temp)) {
        $fawn[] = substr($temp, 0, strpos($temp, '.wth'));
      }
    }
    sort($fawn);
    echo '<center>Select a location: ';
    echo '<select name="file">';
    for ($j = 0; $j < count($fawn); $j++) {
      echo '<option name="' . $fawn[$j] . '" value="' . $fawn[$j] . '">' . str_replace("_"," ",$fawn[$j]); 
    }
    echo '</select>';
    echo '<input type="submit" name="Go" value="Go">';
    echo '</center>';
  } else if ($cat == 4) {
?>
    <b>Main input P_spec files:</b>
    <ul>
      <li><a href="sourcecode.php?file=1-gal_default&type=spec&cat=4">1-gal_default.txt</a>
      -- The default input spec file for Driver.for when using 1 gallon containers for <i>Viburnum odoratissimum</i>.
      <li><a href="sourcecode.php?file=3-gal_default&type=spec&cat=4">3-gal_default.txt</a>
      -- The default input spec file for Driver.for when using 3 gallon containers for <i>Viburnum odoratissimum</i>.
      <li><a href="sourcecode.php?file=1-gal_default_ilex&type=spec&cat=4">1-gal_default_ilex.txt</a>
      -- The default input spec file for Driver.for when using 1 gallon containers for <i>Ilex vomitoria</i>.
      <li><a href="sourcecode.php?file=3-gal_default_ilex&type=spec&cat=4">3-gal_default_ilex.txt</a>
      -- The default input spec file for Driver.for when using 3 gallon containers for <i>Ilex vomitoria</i>.
    </ul>
    <b>Plant spec files:</b>
    <ul>
      <li><a href="sourcecode.php?file=VIBUR_OD&type=spec&cat=4">VIBUR_OD.plt</a>
      -- <i>Viburnum odoratissimum</i> plant spec file. 
      <li><a href="sourcecode.php?file=ILEX_VOM&type=spec&cat=4">ILEX_VOM.plt</a>
      -- <i>Ilex vomitoria</i> plant spec file.
    </ul>
    <b>Notes on inputs and outputs:</b>
    <ul>
      <li><a href="sourcecode.php?file=Notes_input&type=spec&cat=4">Notes_input.txt</a>
      -- Notes on the input spec file.
      <li><a href="sourcecode.php?file=Notes_output&type=spec&cat=4">Notes_output.txt</a>
      -- Notes on the output files.
    </ul>
<?php
  }

  if ($type == "fortran") {
    $file = "../driver/" . $file . ".for";
  } else if ($type == "agro") {
    $file .= ".wth";
  } else if ($type == "fawn") {
    $file = "fawn/" . $file . ".wth";
  } else if ($type == "spec") {
    if (isValidFile($file . ".txt")) $file .= ".txt"; else if (isValidFile("../" . $file . ".txt")) $file = "../" . $file . ".txt";
    if (isValidFile($file . ".plt")) $file .= ".plt"; else if (isValidFile("../" . $file . ".plt")) $file = "../" . $file . ".plt";
    if (isValidFile($file . ".sfn")) $file .= ".sfn"; else if (isValidFile("../" . $file . ".sfn")) $file = "../" . $file . ".sfn";
    if (isValidFile($file . ".irr")) $file .= ".irr"; else if (isValidFile("../" . $file . ".irr")) $file = "../" . $file . ".irr";
  } else if ($isLoggedIn && $type == "user") {
    $file = "data/" . $bmpusername . "/" . $file;
  } else $file = false;
  if (!file_exists($file)) $file = "../" . $file;
  $valid = isValidFile($file);

  if ($valid) {
    echo '<div style="margin-left: 50px;">';
    if (strpos($file, '/') !== false) $shortfile = substr($file, strrpos($file,'/')+1); else $shortfile = $file;
    echo '<h3>' . $shortfile . '</h3>';
    $content = file($file);
    echo '<pre>';
    for ($j = 0; $j < count($content); $j++) {
      $content[$j] = str_replace("<", "&lt;", $content[$j]);
      $content[$j] = str_replace(">", "&gt;", $content[$j]);
      echo $content[$j];
    }
    echo '</pre>';
    echo '</div>';
  } 
?>
<?php require('footer.php'); ?>
