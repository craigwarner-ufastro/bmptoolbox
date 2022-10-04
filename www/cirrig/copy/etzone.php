<?php

class etzone extends zone {

  function etzone($row) {
    $this->setCommonInfo($row);
  }

  function et0() {
  }

  function getIrrigation() {
    $solar = $this->solar_wmhr*0.0864;
    $tmax = ($this->tmax_f-32)/1.8;
    $tmin = ($this->tmin_f-32)/1.8;

    #Adjust for shade
    $solar *= (1.0 - $this->shade/100.0);

    $cdr = 22.2+11.1*sin(0.0172*($this->doy-80));
    $apot = pow(($this->contDiam*2.54)/2, 2)*pi(); 
    $atot = pow(($this->contDiam+$this->contSpacing)*2.54, 2)*$this->spacing;
    $aratio = $atot/$apot;
    $gimpFGC = $this->pctCover*0.01;

    $lat = $this->lat_deg*0.01745;
    $sinlat = sin($lat);
    $coslat = cos($lat);
    $elev = $this->elev_ft*0.3049;

    $press = (101.0-0.0107*$elev)/101.0;
    $escor = 1.0-0.016733*cos(0.0172*($this->doy-1.0));
    $om = 0.017202*($this->doy-3.244);

    $theta = $om+0.03344*sin($om)*(1.0-0.15*sin($om))-1.3526;
    $sindec = 0.3978*sin($theta);
    $cosdec = sqrt(1.0-pow($sindec, 2));
    $sinf = $sindec*$sinlat;
    $cosf = $cosdec*$coslat;
    $hrang = acos(-1*$sinf/$cosf);
    $etr = 37.21/pow($escor, 2)*($hrang*$sinf+$cosf*sin($hrang));
    $h2 = $sinf+$cosf*cos($hrang/2);
    $airmass = $press*(-16.886*pow($h2, 3)+36.137*pow($h2, 2)-27.462*$h2+8.7412);

    $tarcd = 0.87-0.0025*$tmin;
    $cdr2 = $etr*pow($tarcd, $airmass);
    $fcdr = min($solar/$cdr2, 1);
    $drf = max(1.33*($fcdr-0.25), 0);
    $parb = min($solar*0.5, $cdr2*0.5*0.7);
    $brad = $solar*$drf*exp(-3.0*pow($gimpFGC, 0.9))*(1.0-$apot/$atot);
    $tmaxb = $tmax+0.6*$brad;
    $btmean = $tmaxb*0.75+$tmin*0.25;
    $lh = 25.01-$btmean/42.3;

    $gamma = 0.0674*$press;
    $delta = 2503.0*exp(17.27*$btmean/($btmean+237.3))/pow($btmean+237.3, 2);
    $radcom = $delta/($delta+$gamma)*$solar*0.6/$lh;
    $radcom_cdr = $delta/($delta+$gamma)*$cdr*0.6/$lh;
    $vpd = 0.6108*exp(17.27*$btmean/($btmean+237.3))-0.6108*exp(17.27*$tmin/($tmin+237.3));

    $cropec = 1.0;
    $aerocomP = ($cropec*pow($vpd, 1.5))/$lh;
    $et0 = $radcom+$aerocomP;
    $et_cdr0 = $radcom_cdr+$aerocomP;
    $et = $et0*0.9*pow($gimpFGC, 0.73);
    $etcdr = $et_cdr0*0.9*pow($gimpFGC, 0.73);

    $contETcm = $et*$aratio+0.2;
    $contETcdrcm = $etcdr*$aratio+0.2;
    $contETin = $contETcm/2.54;
    $contETcdrin = $contETcdrcm/2.54;

    $cf1 = 0;
    if ($this->irrig_capture == "high") {
      $cf1 = 2.5;
    } else if ($this->irrig_capture == "medium") {
      $cf1 = 2.0; 
    } else if ($this->irrig_capture == "low") {
      $cf1 = 1.5; 
    } else if ($this->irrig_capture == "negative") {
      $cf1 = 0.75;
    }

    $plantSize=($this->plantHt+$this->plantWd)/2;
    if ($cf1 < 1 && $cf1 != 0) {
      $cf = max($cf1, min(1, 1-($plantSize-$this->contDiam)/$this->contDiam*(1-$cf1)));
    } else {
      $cf=min(max(1,min(1+($plantSize-$this->contDiam)/$this->contDiam*($cf1-1),1.1*$cf1)),0.9*$aratio+0.1);
    }

    #cf = 1 for non et-sprinkler 
    if ($this->zoneType != "ET-sprinkler") {
      $cf = 1.0;
    }
    if ($this->zoneType == "ET-micro") {
      $this->irrig_in_hr = $this->irrig_gal_hr*1490.157/$apot;
    }

    $irrig_in = $contETin/$cf*100.0/$this->irrig_uniformity;
    $irrig_time = $irrig_in/$this->irrig_in_hr*60.0;

    #Add in rain
    $cum = $this->prevDeficit*2.54; #cum is in cm!!

    #Loop over hours to find cumulative deficit
    for ($h = $this->firstHour; $h < $this->firstHour+24; $h++) {
      $scaledET = $this->hourly[$h%24]['solar_radiation']/(24*$this->solar_wmhr)*$contETin*2.54;
      #Changed from (1+cf)/2 to cf 8/19/13
      $rainPerPot = $this->hourly[$h%24]['rain_in']*2.54*$cf;
      #nullify rain for prod area = plastic
      if ($this->productionArea == 'plastic') $rainPerPot = 0;
      $cum += $scaledET-$rainPerPot;
      if ($cum < 0) $cum = 0;
    }
    $this->cum_cm = $cum;

    #Re-calculate irrig_in and irrig_time
    #Apply LF here too
    $cum /= 2.54; #Convert to inches

    #Update global vars
    $this->apot = $apot;
    $this->cum = $cum;
    $this->contETin = $contETin;
    $this->cf = $cf;
    $this->contETcdrin = $contETcdrin;

    $irrig_in = ($cum+$this->lf*$cum/(1.0-$this->lf))/$cf*100.0/$this->irrig_uniformity;
    $irrig_time = $irrig_in/$this->irrig_in_hr*60.0;

    $retVal = array();
    $retVal['irrig_in'] = $irrig_in;
    $retVal['irrig_time'] = $irrig_time;
    return $retVal;
  }

  function getIrrigRate() {
    if ($this->zoneType == "ET-sprinkler") return $this->irrig_in_hr;
    return $this->irrig_gal_hr;
  }

  function getIrrigRateUnits() {
    if ($this->zoneType == "ET-sprinkler") return "inch/hr";
    return "gal/hr";
  }

  function processIrrigSchedule($irrig_in, $irrig_time) {
    $deficit_in = 0;
    $doThreshTable = False;
    #process different irrig schedules and min irrig here
    if ($this->irrigSchedule == "daily") {
      if ($irrig_time <= $this->minIrrig) {
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      }
    } else if ($this->irrigSchedule == "odd days") {
      $dom = (int)(date("d"));
      if ($dom % 2 == 0) {
        //even day
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      } else if ($irrig_time <= $this->minIrrig) {
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      }
    } else if ($this->irrigSchedule == "fixed days") {
      $dayset = array();
      $fx = $this->fixedDays;
      for ($d = 6; $d >= 0; $d--) {
        if ($fx >= pow(2,$d)) {
          #this day is set, e.g Sat = 64, Fri = 32
          $dayset[$d] = true;
          $fx -= pow(2,$d);
        } else $dayset[$d] = false;
      }
      $dow = (int)(date("N")) % 7;
      if (!$dayset[$dow]) {
        #not set for today
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      } else if ($irrig_time <= $this->minIrrig) {
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      }
    } else if ($this->irrigSchedule == "threshold") {
      #total available water (in)
      $awin = 0.0132*pow($this->contDiam, 2.7506)*1000.0*$this->availableWater/2.54/$this->apot;
      $this->awin = $awin;
      if ($this->cum+$this->contETcdrin < $this->thresholdFactor*$awin) {
        $deficit_in = $irrig_in;
        $irrig_in = 0;
        $irrig_time = 0;
      }
      $doThreshTable = true;
    } else if ($this->irrigSchedule == "none") {
      $irrig_in = 0;
      $irrig_time = 0;
    }

    $retVal = array();
    $retVal['irrig_in'] = $irrig_in;
    $retVal['irrig_time'] = $irrig_time;
    $retVal['deficit_in'] = $deficit_in;
    $retVal['doThreshTable'] = $doThreshTable; 
    if ($this->ncycles == 1) {
      $retVal['irrig_output'] = (string)(round($irrig_time)); 
    } else {
      $retVal['irrig_output'] = (string)(round($irrig_time/$this->ncycles)) . " x " . $this->ncycles;
    }
    return $retVal;
  }
}
?>
