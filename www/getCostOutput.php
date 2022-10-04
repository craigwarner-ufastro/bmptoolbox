<?php
  function getCostOutput($pfix, $id, $mysqli_db, $thisFert=false) {
    if ($thisFert === false) {
      #Default case.  thisFert is only set on fertilizer and fert/irr comparisons!
      $thisFert = $_POST['FERT'];
    }
    $costs = array();
    $quant = array();
    $rates = array();
    $units = array();
    if (!isset($_POST['costBasis'])) return array($costs, $rates, $quant, $units);
    #$costNames = array('Liner', 'Container', 'Substrate', 'Substrate Labor', 'Fert N incorp', 'Planting labor', 'Tagging labor', 'Pruning labor', 'Spacing labor', 'Plant moving labor', 'Fert N topdress', 'Topdress labor', 'Irrig pumping', 'Allocated space');
    $cid = $_POST['costBasis'];
    $sql = "select * from costs where cid=$cid AND uid = 0";
    if (isset($_SESSION['bmpid'])) {
      $sql = "select * from costs where cid=$cid AND (uid = " . $_SESSION['bmpid'] . " or uid=0)";
    }
    $out = $mysqli_db->query($sql);
    $n = 0;
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $rates = $row;
      }
    }
    if ($rates['uid'] == 0) {
      #Default value
      $laborData =  file('labor/currentLabor.txt');
      $wageRate = (float)$laborData[0]/60.;  
      $rates['fillContainer'] *= $wageRate;
      $rates['planting'] *= $wageRate;
      $rates['tagging'] *= $wageRate;
      $rates['pruning'] *= $wageRate;
      $rates['spacing'] *= $wageRate;
      $rates['plantMovement'] *= $wageRate;
      $rates['fertTopdress'] *= $wageRate;
    }

    #Read summaryoutput.txt
    $yearly = file($pfix . $id . "_summaryoutput.txt");
    $colNums = array();
    $colVals = array("NOP", "NOM", "RD2", "Irrig", "P_Area", "DAY"); 
    $meanVals = array();
    for ($j = 0; $j < count($yearly); $j++) {
      $row = trim($yearly[$j]);
      $row = preg_split("/\s+/", $row);
      if ($row[0] == 'YR') {
	#Find column numbers
	foreach($colVals as $key=>$value) {
	  $idx = array_search($value, $row);
	  if ($idx !== false) $colNums[$value] = $idx;
	}
      } else if ($row[0] == 'Mean') {
	#Get values
	foreach($colNums as $key=>$value) {
	  $meanVals[$key] = (float)($row[$value]);
	}
      }
    }

    $costs['Liner'] = $rates['liner'];
    $costs['Container'] = $rates['container'];
    #Convert from cm^3 to yd^3
    $costs['Substrate'] = $rates['substrate']*$_POST['S_VOL']/pow(91.44,3); 
    $costs['Fill Container'] = $rates['fillContainer'];
    $costs['Fert N incorp'] = $rates['fertCostPerPound']/453.6*$thisFert*$_POST['PCT_N']/100.0;
    $costs['Planting labor'] = $rates['planting'];
    $costs['Tagging labor'] = $rates['tagging'];
    $costs['Pruning labor'] = $rates['pruning']*$meanVals['NOP'];
    $costs['Spacing labor'] = $rates['spacing']*$meanVals['NOM'];
    $costs['Plant moving labor'] = $rates['plantMovement'];
    if ($meanVals['RD2'] > 1) {
      $costs['Fert N topdress'] = $rates['fertCostPerPound']/453.6*$_POST['FERT2']*$_POST['PCT_N2']/100.0;
    } else $costs['Fert N topdress'] = 0.0;
    $costs['Topdress labor'] = $rates['fertTopdress'];
    $costs['Irrig pumping'] = $rates['irrigationPumping']*$meanVals['Irrig']/3.785;#3.785 L/Gallon 
    $costs['Allocated space'] = $rates['allocSpace']*$meanVals['P_Area']/929.03*$meanVals['DAY']/30.5;#929.03 cm^2/ft^2; 30.5 days/month

    $rates['Liner'] = $rates['liner'];
    $rates['Container'] = $rates['container'];
    $rates['Substrate'] = $rates['substrate'];
    $rates['Fill Container'] = $rates['fillContainer'];
    $rates['Fert N incorp'] = $rates['fertCostPerPound']/453.6;
    $rates['Planting labor'] = $rates['planting'];
    $rates['Tagging labor'] = $rates['tagging'];
    $rates['Pruning labor'] = $rates['pruning'];
    $rates['Spacing labor'] = $rates['spacing'];
    $rates['Plant moving labor'] = $rates['plantMovement'];
    $rates['Fert N topdress'] = $rates['fertCostPerPound']/453.6;
    $rates['Topdress labor'] = $rates['fertTopdress'];
    $rates['Irrig pumping'] = $rates['irrigationPumping'];
    $rates['Allocated space'] = $rates['allocSpace'];

    $quant['Liner'] = 1; 
    $quant['Container'] = 1; 
    $quant['Substrate'] = $_POST['S_VOL']/pow(91.44,3);
    $quant['Fill Container'] = 1;
    $quant['Fert N incorp'] = $thisFert*$_POST['PCT_N']/100.0;
    $quant['Planting labor'] = 1;
    $quant['Tagging labor'] = 1;
    $quant['Pruning labor'] = $meanVals['NOP'];
    $quant['Spacing labor'] = $meanVals['NOM'];
    $quant['Plant moving labor'] = 1; 
    if ($meanVals['RD2'] > 1) {
      $quant['Fert N topdress'] = $_POST['FERT2']*$_POST['PCT_N2']/100.0;
    } else $quant['Fert N topdress'] = 0.0;
    $quant['Topdress labor'] = 1;
    $quant['Irrig pumping'] = $meanVals['Irrig']/3.785;#3.785 L/Gallon
    $quant['Allocated space'] = $meanVals['P_Area']/929.03*$meanVals['DAY']/30.5;#929.03 cm^2/ft^2; 30.5 days/month

    $units['Liner'] = 'ea'; 
    $units['Container'] = 'ea';
    $units['Substrate'] = 'yd<sup>3</sup>'; 
    $units['Fill Container'] = 'ea'; 
    $units['Fert N incorp'] = 'g N'; 
    $units['Planting labor'] = 'ea'; 
    $units['Tagging labor'] = 'ea';
    $units['Pruning labor'] = 'ea'; 
    $units['Spacing labor'] = 'ea'; 
    $units['Plant moving labor'] = 'ea';
    $units['Fert N topdress'] = 'g N'; 
    $units['Topdress labor'] = 'ea';
    $units['Irrig pumping'] = 'gal'; 
    $units['Allocated space'] = 'ft<sup>2</sup>/month'; 
 
    #$costNames = array('Liner', 'Container', 'Substrate', 'Substrate Labor', 'Fert N incorp', 'Planting labor', 'Tagging labor', 'Pruning labor', 'Spacing labor', 'Plant moving labor', 'Fert N topdress', 'Topdress labor', 'Irrig pumping', 'Allocated space');
    return array($costs, $rates, $quant, $units);
  }
?>
