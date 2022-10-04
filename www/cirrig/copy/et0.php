<?php
function et0($solar_wmhr, $tmax_f, $tmin_f, $doy, $zone) { 
        $contDiam = $zone['containerDiam_in'];
        $irrig_in_hr = $zone['irrig_in_per_hr'];
        $irrig_capture = $zone['irrigCaptureAbility'];
        $irrig_uniformity = $zone['irrig_uniformity'];
        $elev_ft = $zone['elevation_ft'];
        $lat_deg = $zone['lattitude'];
        $long_deg = $zone['longitude'];

        $plantHt = $zone['plantHeight_in'];
        $plantWd = $zone['plantWidth_in'];
        $pctCover = $zone['pctCover'];
        $contSpacing = $zone['containerSpacing_in'];
        $spacing = 1; #square
        if ($zone['spacing'] == 'offset') {
          $spacing = 0.866; #triangular
        }
        $shade = $zone['shade'];
        $lf = $zone['leachingFraction']/100.0;
        $availableWater = $zone['availableWater']/100.0;
        $thresholdFactor = $zone['thresholdFactor']/100.0;

        $irrig_gal_hr = $zone['irrig_gal_per_hr'];
        $zoneType = $zone['zoneType'];
        if ($zoneType == "LF-sprinkler" || $zoneType == "LF-micro") {
          $pctCover = 100.0;
          $contSpacing = 0.0;
          $spacing = 1;
        }

/*
$contDiam = 11.0;
$irrig_in_hr = 0.7;
$irrig_capture = "high";
$irrig_uniformity = 80;
$elev_ft = 100;
$lat_deg = 29.803;
$long_deg = -82.41;

$plantHt = 12.6;
$plantWd = 17.0;
$pctCover = 95.0;
$contSpacing = 0.0;
$spacing = 0.866;
*/

#$doy = 75;
#$solar_wmhr = 204;
#$tmax_f = 85.82;
#$tmin_f = 52.52;

$solar = $solar_wmhr*0.0864;
$tmax = ($tmax_f-32)/1.8;
$tmin = ($tmin_f-32)/1.8;

#Adjust for shade
$solar *= (1.0 - $shade/100.0);

$cdr = 22.2+11.1*sin(0.0172*($doy-80));
$apot = pow(($contDiam*2.54)/2, 2)*pi(); 
$atot = pow(($contDiam+$contSpacing)*2.54, 2)*$spacing;
$aratio = $atot/$apot;
$gimpFGC = $pctCover*0.01;

$lat = $lat_deg*0.01745;
$sinlat = sin($lat);
$coslat = cos($lat);
$elev = $elev_ft*0.3049;

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
$et_cdr0 = $radcom_cdr+$aerocomP;
$et = $et0*0.9*pow($gimpFGC, 0.73);
$etcdr = $et_cdr0*0.9*pow($gimpFGC, 0.73);

$contETcm = $et*$aratio+0.2;
$contETcdrcm = $etcdr*$aratio+0.2;
$contETin = $contETcm/2.54;
$contETcdrin = $contETcdrcm/2.54;

#$vplt = 4/3.*pi()*pow(($plantWd*2.54)/2, 2)*$plantHt*2.54/2;
$cf1 = 0;
if ($irrig_capture == "high") {
  #$cf1 = 0.02;
  $cf1 = 2.5;
} else if ($irrig_capture == "medium") {
  #$cf1 = 0.006;
  $cf1 = 2.0; 
} else if ($irrig_capture == "low") {
  #$cf1 = 0.0008;
  $cf1 = 1.5; 
} else if ($irrig_capture == "negative") {
  $cf1 = 0.75;
}
#$cf2 = -1*$cf1*($apot*7.39-716)+$apot;
#$cfmax = max(1, ($cf1*$vplt+$cf2)/$apot); 
#$cf = min($cfmax, 0.9*$atot/$apot+0.1);

$plantSize=($plantHt+$plantWd)/2;
if ($cf1 < 1 && $cf1 != 0) {
  $cf = max($cf1, min(1, 1-($plantSize-$contDiam)/$contDiam*(1-$cf1)));
} else {
  $cf=min(max(1,min(1+($plantSize-$contDiam)/$contDiam*($cf1-1),1.1*$cf1)),0.9*$aratio+0.1);
}

#cf = 1 for non et-sprinkler 
if ($zoneType != "ET-sprinkler") {
  $cf = 1.0;
}
if ($zoneType == "ET-micro" || $zoneType == "LF-micro") {
  $irrig_in_hr = $irrig_gal_hr*1490.157/$apot;
}

$irrig_in = $contETin/$cf*100.0/$irrig_uniformity;
$irrig_time = $irrig_in/$irrig_in_hr*60.0;

/*
echo "CDR: $cdr<br>"; 
echo "APOT: $apot<br>";
echo "ATOT: $atot<br>"; 
echo "Aratio: $aratio<br>";
echo "GimpFGC: $gimpFGC<br>";
echo "LAT: $lat<br>";
echo "SINLAT: $sinlat<br>";
echo "COSLAT: $coslat<br>";
echo "ELEV: $elev<br>";
echo "PRESS: $press<br>";
echo "ESCOR: $escor<br>";
echo "OM: $om<br>";
echo "THETA: $theta<br>";
echo "SINDEC: $sindec<br>";
echo "COSDEC: $cosdec<br>";
echo "SINF: $sinf<br>";
echo "COSF: $cosf<br>";
echo "HRANG: $hrang<br>";
echo "ETR: $etr<br>";
echo "H2: $h2<br>";
echo "AIRMASS: $airmass<br>";
echo "TARCD: $tarcd<br>";
echo "CDR2: $cdr2<br>";
echo "FCDR: $fcdr<br>";
echo "DRF: $drf<br>";
echo "PARB: $parb<br>";
echo "BRAD: $brad<br>";
echo "TMAXB: $tmaxb<br>";
echo "BTmean: $btmean<br>";
echo "LH: $lh<br>";
echo "gamma: $gamma<br>";
echo "delta: $delta<br>";
echo "radcom: $radcom<br>";
echo "radcom_cdr: $radcom_cdr<br>";
echo "vpd: $vpd<br>";
echo "cropec: $cropec<br>";
echo "aerocomP: $aerocomP<br>";
echo "ET0: $et0<br>";
echo "ET-cdr0: $et_cdr0<br>";
echo "ET: $et<br>";
echo "ETcdr: $etcdr<br>";
echo "Container ET cm: $contETcm<br>";
echo "Container ETcdr cm: $contETcdrcm<br>";
echo "Container ET in: $contETin<br>";
echo "Container ET cdr in: $contETcdrin<br>";
echo "Vplt: $vplt<br>";
echo "CF1: $cf1<br>";
echo "CF2: $cf2<br>";
echo "CFmax: $cfmax<br>";
echo "CFin: $cf_in<br>";
echo "Irrigation (in): $irrig_in<br>";
echo "Irrigation run time (min): $irrig_time<br>";
*/
  return $et0;
}
?>
