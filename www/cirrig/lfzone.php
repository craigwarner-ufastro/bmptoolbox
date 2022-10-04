<?php
class lfzone extends zone {

  function lfzone($row) {
    $this->setCommonInfo($row);

    $this->lfTestDate = $row['lfTestDate'];
    $this->lfTestRuntime = $row['lfTestRuntime'];
    $this->lfTestPct = $row['lfTestPct'];
    $this->lfTargetPct = $row['lfTargetPct'];
    $this->et_fac = $row['et_fac'];

    ##LF calcs
    $this->pctCover = 100.0;
    $this->contSpacing = 0.0;
    $this->spacing = 1;
    $this->cf = 1;
  }

  function calcEffectiveRain() {
    #Calculate effective rain
    $water_deficit_cm = $this->lfIrrig_in*2.54+$this->prevDeficit*2.54;
    $rain_cm = $this->rain_in*2.54;

    #nullify rain for prod area = plastic
    if ($this->productionArea == 'plastic') $rain_cm = 0;
    $eff_rain_cm = ($water_deficit_cm-$this->cum_cm); #cf = 1
    if ($eff_rain_cm < 0) $eff_rain_cm = 0;
    $water_deficit_offset_cm = $eff_rain_cm; #cf = 1

    if ($eff_rain_cm > 0 && $rain_cm > 0) {
      $pct_eff_rain = $eff_rain_cm/$rain_cm*100;
    } else $pct_eff_rain = "N/A";

    if ($water_deficit_cm > 0 && $water_deficit_offset_cm > 0) {
      $water_deficit_red = $water_deficit_offset_cm/$water_deficit_cm*100;
    } else $water_deficit_red = "N/A";

    $this->water_deficit_cm = $water_deficit_cm;
    $this->rain_cm = $rain_cm;
    $this->eff_rain_cm = $eff_rain_cm;
    $this->water_deficit_offset_cm = $water_deficit_offset_cm;
    $this->pct_eff_rain = $pct_eff_rain;
    $this->water_deficit_red = $water_deficit_red;
  }

  function calcET0($solar_wmhr, $tmax_f, $tmin_f, $doy) {
    #Calculate ET0 for a given weather set and doy
    $solar = $solar_wmhr*0.0864;
    $tmax = ($tmax_f-32)/1.8;
    $tmin = ($tmin_f-32)/1.8;

    #Adjust for shade
    $solar *= (1.0 - $this->shade/100.0);

    $cdr = 22.2+11.1*sin(0.0172*($doy-80));
    $apot = pow(($this->contDiam*2.54)/2, 2)*pi();
    $atot = pow(($this->contDiam+$this->contSpacing)*2.54, 2)*$this->spacing;
    $aratio = $atot/$apot;
    $gimpFGC = $this->pctCover*0.01;

    $lat = $this->lat_deg*0.01745;
    $sinlat = sin($lat);
    $coslat = cos($lat);
    $elev = $this->elev_ft*0.3049;

    $press = (101.0-0.0107*$elev)/101.0;
    $escor = 1.0-0.016733*cos(0.0172*($doy-1.0));
    $om = 0.017202*($doy-3.244);

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
    $this->et0 = $et0;
    return $et0;
  }

  function getIrrigation() {
    #Accumulate hourly weather
    $tmax_f_lf = $this->lfhourly[0]['max_temp'];
    $tmin_f_lf = $this->lfhourly[0]['min_temp'];
    $solar_wmhr_lf = $this->lfhourly[0]['solar_radiation'];
    $rain_in_lf = $this->lfhourly[0]['rain_in'];
    for ($h = 1; $h < 24; $h++) {
      if (!isset($this->lfhourly[$h])) continue;
      $tmax_f_lf = max($tmax_f_lf, $this->lfhourly[$h]['max_temp']);
      $tmin_f_lf = min($tmin_f_lf, $this->lfhourly[$h]['min_temp']);
      $solar_wmhr_lf += $this->lfhourly[$h]['solar_radiation'];
      $rain_in_lf += $this->lfhourly[$h]['rain_in'];
    }
    $solar_wmhr_lf /= 24.0;
    $doy_lf = intval(date("z", strtotime($this->lfTestDate)));

    $this->rtlf = (100.0-$this->lfTestPct)/(100.0-$this->lfTargetPct)*$this->lfTestRuntime;
    $this->etlf = $this->calcET0($solar_wmhr_lf, $tmax_f_lf, $tmin_f_lf, $doy_lf);
    $et0 = $this->calcET0($this->solar_wmhr, $this->tmax_f, $this->tmin_f, $this->doy);
    $this->et0 = $et0;
    $this->etlfratio = ($et0/$this->etlf)*100.0;
    $irrig_time = $this->etlfratio/100.0*$this->rtlf;

    #new logic for et_fac 11/19/18
    if ($irrig_time < $this->rtlf && $this->et_fac > 0) {
      $irrig_time = $irrig_time + $this->et_fac*($this->rtlf - $irrig_time);
    } 

    #convert irrigation rate for LF micro
    if ($this->zoneType == "LF-micro") {
      $apot = pow(($this->contDiam*2.54)/2, 2)*pi();
      $this->irrig_in_hr = $this->irrig_gal_hr*1490.157/$apot;
    }
    $irrig_in = $irrig_time * $this->irrig_in_hr / 60.0;
    #set for rain calcs
    $this->lfIrrig_in = $irrig_in;

    #Add in rain
    $cum = $this->prevDeficit; #cum is in inches for LF!!

    #Loop over hours to find cumulative deficit
    for ($h = $this->firstHour; $h < $this->firstHour+24; $h++) {
      $scaledIrrig_in = $this->hourly[$h%24]['solar_radiation']/(24*$this->solar_wmhr)*$irrig_in;
      $rainPerPot_in = $this->hourly[$h%24]['rain_in'];
      #nullify rain for prod area = plastic
      if ($this->productionArea == 'plastic') $rainPerPot_in = 0;
      $cum += $scaledIrrig_in-$rainPerPot_in;
      if ($cum < 0) $cum = 0;
    }
    $this->cum_cm = $cum*2.54;
    $this->cum = $cum;
    $irrig_in = $cum;
    $irrig_in *= 100.0/$this->irrig_uniformity; #Divide by irrig_uniformity AFTER calculating cum deficit 1/11/16 CW
    $irrig_time = $irrig_in*60.0/$this->irrig_in_hr;

    $retVal = array();
    $retVal['irrig_in'] = $irrig_in;
    $retVal['irrig_time'] = $irrig_time;
    return $retVal;
  }


  function getIrrigRate() {
    if ($this->zoneType == "LF-sprinkler") return $this->irrig_in_hr;
    return $this->irrig_gal_hr;
  }

  function getIrrigRateUnits() {
    if ($this->zoneType == "LF-sprinkler") return "inch/hr";
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
      //$retVal['irrig_output'] = (string)(round($irrig_time));
      $retVal['irrig_output'] = outFormat($irrig_time, 1);
    } else {
      //$retVal['irrig_output'] = (string)(round($irrig_time/$this->ncycles)) . " x " . $this->ncycles;
      $retVal['irrig_output'] = (string)(outFormat($irrig_time/$this->ncycles, 1)) . " x " . $this->ncycles;
    }
    return $retVal;
  }

  function setLFHourlyWeather($lfhourly) {
    $this->lfhourly = $lfhourly;
  }
}
?>
