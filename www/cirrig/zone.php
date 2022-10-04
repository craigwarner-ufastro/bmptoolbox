<?php
class zone {

  function zone($row) {
    $this->setCommonInfo($row);
  }

  function calcEffectiveRain() {
    #Calculate effective rain
    $water_deficit_cm = $this->contETin*2.54+$this->prevDeficit*2.54;
    $rain_cm = $this->rain_in*2.54;

    #nullify rain for prod area = plastic
    if ($this->productionArea == 'plastic') $rain_cm = 0;
    $eff_rain_cm = ($water_deficit_cm-$this->cum_cm)/((1+$this->cf)/2.);
    if ($eff_rain_cm < 0) $eff_rain_cm = 0;
    $water_deficit_offset_cm = $eff_rain_cm*(1+$this->cf)/2.;

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

  function et0() {
  }

  function getAutoRunTime() {
    return $this->autoRunTime;
  }

  function getId() {
    return $this->zid;
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

    #$vplt = 4/3.*pi()*pow(($plantWd*2.54)/2, 2)*$plantHt*2.54/2;
    $cf1 = 0;
    if ($this->irrig_capture == "high") {
      #$cf1 = 0.02;
      $cf1 = 2.5;
    } else if ($this->irrig_capture == "medium") {
      #$cf1 = 0.006;
      $cf1 = 2.0; 
    } else if ($this->irrig_capture == "low") {
      #$cf1 = 0.0008;
      $cf1 = 1.5; 
    } else if ($this->irrig_capture == "negative") {
      $cf1 = 0.75;
    }
    #$cf2 = -1*$cf1*($apot*7.39-716)+$apot;
    #$cfmax = max(1, ($cf1*$vplt+$cf2)/$apot); 
    #$cf = min($cfmax, 0.9*$atot/$apot+0.1);

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
    #do this conversion in all constructors
    if ($this->zoneType == "ET-micro" || $this->zoneType == "LF-micro") {
      $this->irrig_in_hr = $this->irrig_gal_hr*1490.157/$apot;
    }

    $irrig_in = $contETin/$cf*100.0/$this->irrig_uniformity;
    $irrig_time = $irrig_in/$this->irrig_in_hr*60.0;

    $retVal = array();
    $retVal['irrig_in'] = $irrig_in;
    $retVal['irrig_time'] = $irrig_time;
    return $retVal;
  }

  function getIrrigRate() {
    return $this->irrig_in_hr;
  }

  function getIrrigRateUnits() {
    return "inch/hr";
  }

  function getIrrigSchedule() {
    return $this->irrigSchedule;
  }

  function getName() {
    return $this->zoneName;
  }

  function getPlant() {
    return $this->plant;
  }

  function getRain() {
    return $this->rain_in;
  }

  function getSolar() {
    return $this->solar_wmhr;
  }

  function getTmax() {
    return $this->tmax_f;
  }

  function getTmin() {
    return $this->tmin_f;
  }

  function processIrrigSchedule($irrig_in, $irrig_time) {
    $retVal = array();
    $retVal['irrig_in'] = $irrig_in;
    $retVal['irrig_time'] = $irrig_time;
    $retVal['deficit_in'] = 0;
    return $retVal;
  }

  function setCommonInfo($row) {
    $this->autoRunTime = $row['autoRunTime'];
    $this->zoneName = $row['zoneName'];
    $this->uid = $row['uid'];
    $this->zid = $row['zid'];
    $this->wsid = $row['wsid'];
    $this->zoneNumber = $row['zoneNumber'];
    $this->plant = $row['plant'];

    $this->contDiam = $row['containerDiam_in'];
    $this->irrig_in_hr = $row['irrig_in_per_hr'];
    $this->irrig_capture = $row['irrigCaptureAbility'];
    $this->irrig_uniformity = $row['irrig_uniformity'];
    $this->elev_ft = $row['elevation_ft'];
    $this->lat_deg = $row['lattitude'];
    $this->long_deg = $row['longitude'];

    $this->hourly_rain_thresh_in = $row['hourly_rain_thresh_in'];
    $this->weekly_rain_thresh_in = $row['weekly_rain_thresh_in'];

    $this->plantHt = $row['plantHeight_in'];
    $this->plantWd = $row['plantWidth_in'];
    $this->pctCover = $row['pctCover'];
    $this->contSpacing = $row['containerSpacing_in'];
    $this->spacingString = $row['spacing'];
    $this->spacing = 1; #square
    if ($row['spacing'] == 'offset') {
      $this->spacing = 0.866; #triangular
    }
    $this->shade = $row['shade'];
    $this->lf = $row['leachingFraction']/100.0;
    $this->availableWater = $row['availableWater']/100.0;
    $this->thresholdFactor = $row['thresholdFactor']/100.0;

    $this->irrig_gal_hr = $row['irrig_gal_per_hr'];
    $this->zoneType = $row['zoneType'];
    $this->productionArea = $row['productionArea'];
    $this->irrigSchedule = $row['irrigSchedule'];
    if ($this->irrigSchedule == "fixed days") {
      $this->fixedDays = $row['fixedDays'];
    }
    $this->minIrrig = $row['minIrrig'];
    $this->ncycles = $row['ncycles'];

    $this->doy = intval(date("z"))+1;#doy is numbered from 0 in php
    $temp = explode(':', $this->autoRunTime);
    $this->firstHour = (int)($temp[0])+1;
  }

  function setFirstHour($firstHour) {
    $this->firstHour = $firstHour;
  }

  function setHourlyWeather($hourly) { 
    $this->hourly = $hourly;
    #Accumulate hourly weather
    $this->tmax_f = $hourly[0]['max_temp'];
    $this->tmin_f = $hourly[0]['min_temp'];
    $this->solar_wmhr = $hourly[0]['solar_radiation'];
    $this->rain_in = $hourly[0]['rain_in'];
    for ($j = 1; $j < 24; $j++) {
      if (!isset($hourly[$j])) continue;
      $this->tmax_f = max($this->tmax_f, $hourly[$j]['max_temp']);
      $this->tmin_f = min($this->tmin_f, $hourly[$j]['min_temp']);
      $this->solar_wmhr += $hourly[$j]['solar_radiation'];
      $this->rain_in += $hourly[$j]['rain_in'];
    }
    $this->solar_wmhr /= 24.0;
  }

  function setPrevDeficit($prevDeficit) {
    $this->prevDeficit = $prevDeficit;
  }

  function updateUserWeather($tmax_f, $tmin_f, $solar_wmhr, $rain_in) {
    $this->tmax_f = $tmax_f;
    $this->tmin_f = $tmin_f;
    $this->solar_wmhr = $solar_wmhr;
    $this->rain_in = $rain_in;
  }
}
?>
