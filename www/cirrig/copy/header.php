<link rel=stylesheet href="driver.css" type="text/css">
<link rel=stylesheet href="calendar.css" type="text/css">
<style type="text/css">
  #dhtmltooltip{
    position: absolute;
    width: 150px;
    border: 2px solid black;
    padding: 2px;
    background-color: lightyellow;
    visibility: hidden;
    z-index: 100;
    /*Remove below line to remove shadow. Below line should always appear last within this CSS*/
    /*filter: progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135);*/
  }
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="CalendarPopup.js"></SCRIPT>
<script language="javascript">
  var outputTab = null;
  if (self.name != "output" && self.name != "ccmt") self.name = "input";

  document.write(getCalendarStyles());
  var cal = new CalendarPopup("calDiv");
  cal.setCssPrefix("TEST");
  cal.setReturnFunction("setCalendarVals");
  cal.showYearNavigation();
  function setCalendarVals(y,m,d) {
     document.paramForm.START_YR.value=y;
     document.paramForm.month.selectedIndex=m-1;
     setDayList(m-1);
     document.paramForm.day.selectedIndex=d-1;
     setDay(false);
     updateEndYear(false);
  }

  function updateYear() {
    if (pagename.indexOf("date") != -1) {
      return updateYear2();
    } else if (pagename.indexOf("realtime") != -1) {
      var endDate;
      var selIdx = document.paramForm.WFNAME.selectedIndex;
      if (wthType == 0) {
	endDate = latest;
      } else if (wthType == 1) {
	endDate = wthEnd[selIdx];
      } else if (wthType == 2) {
	endDate = userWthEnd[selIdx];
      }
      var d = new Date(endDate);
      var mon = d.getMonth()+1;
      var day = d.getDate();
      var yr = d.getFullYear();
      var currMon = document.paramForm.month.selectedIndex+1;
      var currDay = document.paramForm.day.selectedIndex+1;
      if (currMon < mon || (currMon == mon && currDay <= day)) {
	document.paramForm.START_YR.value = yr;
      } else document.paramForm.START_YR.value = (yr-1);
      updateEndYear(true);
      return;
    } else if (pagename.indexOf("location") != -1) {
      var startDate = wthStart;
      var d = new Date(startDate);
      for (i = 1; i <= nruns_old; i++) {
	var sel = eval('document.paramForm.WFNAME_'+i);
	if (typeof sel == "undefined") continue;
	if (wthType == 0) {
	  startDate = fawnStart[sel.selectedIndex];
	} else if (wthType == 1) {
	  startDate = wthStart;
	} else if (wthType == 2) {
	  startDate = userWthStart[sel.selectedIndex];
	}
	var newd = new Date(startDate);
	if (newd > d) d = newd;
      }
      var mon = d.getMonth()+1;
      var day = d.getDate();
      var yr = d.getFullYear();
      var currMon = document.paramForm.month.selectedIndex+1;
      var currDay = document.paramForm.day.selectedIndex+1;
      if (currMon < mon || (currMon == mon && currDay <= day)) {
	document.paramForm.START_YR.value = (yr+1);
      } else document.paramForm.START_YR.value = yr;
      updateEndYear(true);
      return;
    }
    var startDate;
    if (wthType == 0) {
      startDate = fawnStart[document.paramForm.WFNAME.selectedIndex];
    } else if (wthType == 1) {
      startDate = wthStart;
    } else if (wthType == 2) {
      startDate = userWthStart[document.paramForm.WFNAME.selectedIndex];
    }
    var d = new Date(startDate);
    var mon = d.getMonth()+1;
    var day = d.getDate();
    var yr = d.getFullYear();
    var currMon = document.paramForm.month.selectedIndex+1;
    var currDay = document.paramForm.day.selectedIndex+1;
    if (currMon < mon || (currMon == mon && currDay <= day)) {
      document.paramForm.START_YR.value = (yr+1);
    } else document.paramForm.START_YR.value = yr;
    updateEndYear(true);
  }

  function updateEndYear(setLast) {
    var startYr = 0;
    if (pagename.indexOf("date") != -1) {
      for (j = 1; j < nruns_old+1; j++) {
	var sel = eval('document.paramForm.START_YR_'+j);
        var tempYr = parseInt(sel.value);
        if (tempYr > startYr) startYr = tempYr;
      }
    } else {
      startYr = parseInt(document.paramForm.START_YR.value);
    }
    var selYr = document.paramForm.END_YR.options[document.paramForm.END_YR.selectedIndex].value;
    document.paramForm.END_YR.options.length=0;

    var endYr;
    if (wthType == 0) {
      endYr = (new Date(latest)).getFullYear(); 
    } else if (wthType == 1) {
      if (pagename.indexOf("location") != -1) {
        for (i = 1; i < 5; i++) {
          var sel = eval('document.paramForm.WFNAME_'+i);
          var tempYr = new Date(wthEnd[sel.selectedIndex]).getFullYear();
          if (tempYr < endYr || i == 1) endYr = tempYr
        }
      } else {
        endYr = new Date(wthEnd[document.paramForm.WFNAME.selectedIndex]).getFullYear(); 
      }
    } else if (wthType == 2) {
      if (pagename.indexOf("location") != -1) {
        for (i = 1; i < 5; i++) {
          var sel = eval('document.paramForm.WFNAME_'+i);
          var tempYr = new Date(userWthEnd[sel.selectedIndex]).getFullYear();
          if (tempYr < endYr) endYr = tempYr
        }
      } else {
        endYr = new Date(userWthEnd[document.paramForm.WFNAME.selectedIndex]).getFullYear(); 
      }
    }
    var n = 0;
    for (j = startYr+1; j <= endYr; j++) {
      document.paramForm.END_YR.options[n] = new Option(j, j, false, false);
      if (j == selYr) document.paramForm.END_YR.options[n].selected = true;
      n++;
    }
    if (n == 0) {
      document.paramForm.END_YR.options[n] = new Option(startYr, startYr, false, false);
      n++;
    }
    if (j <= selYr) document.paramForm.END_YR.options[n-1].selected = true;
    if (setLast) document.paramForm.END_YR.options[n-1].selected = true;
  }

  function updateCalendar(cal, anchor) {
    var selDate = '' + (document.paramForm.month.selectedIndex+1);
    selDate += '/' + (document.paramForm.day.selectedIndex+1);
    selDate += '/' + document.paramForm.START_YR.value;
    var startDate;
    var endDate;
    var sd;
    var ed;
    if (pagename.indexOf("location") != -1) {
      startDate = wthStart;
      endDate = latest;
      sd = new Date(startDate);
      ed = new Date(latest);
      for (i = 1; i < 5; i++) {
	var sel = eval('document.paramForm.WFNAME_'+i);
        if (typeof sel == "undefined") continue;
        if (wthType == 0) {
          startDate = fawnStart[sel.selectedIndex];
	  endDate = latest;
        } else if (wthType == 1) {
          startDate = wthStart;
	  endDate = wthEnd[sel.selectedIndex];
        } else if (wthType == 2) {
          startDate = userWthStart[sel.selectedIndex];
	  endDate = userWthEnd[sel.selectedIndex];
        }
        var newsd = new Date(startDate);
        if (newsd > sd) sd = newsd; 
	var newed = new Date(endDate);
	if (newed < ed) ed = newed
      }
    } else {
      if (wthType == 0) {
	startDate = fawnStart[document.paramForm.WFNAME.selectedIndex];
	endDate = latest;
      } else if (wthType == 1) {
	startDate = wthStart;
        endDate = wthEnd[document.paramForm.WFNAME.selectedIndex];
      } else if (wthType == 2) {
	startDate = userWthStart[document.paramForm.WFNAME.selectedIndex];
	endDate = userWthEnd[document.paramForm.WFNAME.selectedIndex];
      }
      sd = new Date(startDate);
      ed = new Date(endDate);
    }
    sd.setDate(sd.getDate()-1);
    ed = new Date(endDate);
    cal.disabledDatesExpression = "";
    cal.addDisabledDates(formatDate(ed,"yyyy-MM-dd"), null);
    cal.addDisabledDates(null, formatDate(sd,"yyyy-MM-dd"));
    cal.showCalendar(anchor, selDate);
    return false;
  }

  var moveImgSrc = 'images/triangular_move.png';
  var showMoveImg = false;
  var laitext = 'Leaf area index (LAI) is the ratio of leaf surface area to ground area. For sweet viburnum, interception of incoming solar radiation increases until a LAI of approximately 4 is reached at which point essentially all useful light is intercepted.  A LAI of 4 means that for every square inch of ground there are 4 square inches of leaf surfaces above it.  As LAI increases above 4, light will become increasingly limited causing upright habit, reduced potential growth and potential dieback of lower leaves.  If you select &quot;move based on model recommendation&quot;, plants will be moved when LAI is 4.';
  var pctntext = 'The total N content of the fertilizer is expressed in the first number of the fertilizer grade or analysis.  A fertilizer with a 16-8-7 analysis contains 16% N.';
  var pctcrntext = 'Due to imperfections in fertilizer coatings, it is common for a portion of the nitrogen in controlled-release fertilizers to <b>not</b> exhibit controlled-release properties. On the fertilizer label, the actual percentage of coated, controlled-release N is provided.  This value should be less than or equal to the percent total N in the fertilizer.';
  var pctptext = 'The total %P<sub>2</sub>O<sub>5</sub> content of the fertilizer as reported on the label.'; 
  var fdaystext = 'The longevity rating is the length of time required for 80-90% of total nutrients in the fertilizer to be released under controlled testing and with a uniform temperature, usually 70-75 degrees F. The longevity rating will be given on the fertilizer label. The actual release rate of nutrients from controlled release fertilizer is governed primarily by temperature.  The model factors in temperature and longevity rating when estimating nutrient release during the simulation.';
  var madtext = 'The amount of irrigation water to apply is based upon the expected water deficit in the container and the plant canopy\'s irrigation interception factor.  The water deficit in the container is a result of yesterday\'s evapotranspiration loss adjusted for any rainfall gain.  Depending on the size and architecture of the canopy as well as container spacing, interception of irrigation water can be greater than or less than the amount of irrigation water that would fall into the container without a plant growing in it.  The model estimates the interception factor and adjusts irrigation rates accordingly.'; 
  var rtprunetext = 'The effect of pruning depends upon the degree of pruning.  With CCROP, the degree of pruning is directly related to plant height reduction.  To make an accurate assessment of plant height reduction, the REAL-TIME TOOL asks the user to input plant height and width before and after pruning. Values for plant height and width before pruning will automatically be input as a plant size adjustment for the day prior to pruning and will overwrite any values previously input in the plant size adjustment section.';
  var rtadjtext = 'Plant size adjustments allow the user to adjust CCROP simulated values with field-observed values.  Size adjustments may be overwritten if a pruning event(s) is entered.';
  var rtctext1 = 'If actual irrigation is different than recommended, enter the actual rate in the "B" section.';
  var rtctext2 = 'If plants are pruned, enter height and width before and after pruning in the "B" section.  CCROP-estimated height and width can be "calibrated" to actual field measurements several times during the season.';
  var rtctext3 = 'It is recommended that containers be spaced when LAI reaches 3-4.  If plants are spaced, enter move date and new spacing in the "B" section.';
  var ferttext = 'CCROP assumes that polymer-coated, controlled-release fertilizers are used whose release properties are dependent soley on temperature.  N release from other fertilizers such as sulfur-coated urea whose release is also dependent on microbial activity or sparingly soluble products whose release is also dependent upon particle size is not simulated by CCROP.';

  var dailytext = 'Irrigates daily as long as irrigation run time exceeds the minimum. If the minimum run time is not exceeded then the water deficit is carried over to the subsequent day.';
  var odd_days_text = 'Irrigates on odd dates e.g. 1st, 3rd, 5th, etc. of the month.  If the minimum run time is not exceeded on the day to irrigate, then the water deficit is carried over to the subsequent day.';
  var fixed_days_text = 'Irrigates only on the days of the week selected. Water deficits for days not irrigated are cumulated until the next irrigation day.';
  var threshold_text = 'Instead of a time schedule, the threshold irrigation schedule is based on not letting the substrate water content fall below a critical threshold level.   C-Irrig essentially asks “If we don’t irrigate today, can we get through tomorrow without stressing the plants”.   The threshold value is the product of a threshold factor and the container substrate’s available water-holding capacity (inch).  The default threshold factor is 40% and the default substrate available water-holding capacity (inch) is based on 20% available water capacity on a substrate volume basis.  Using these defaults, a 10-inch diameter container would have a substrate fill volume of 7.4 liters, an available water-holding capacity of 1.44 inch and a threshold value of 0.58 inch.  In this case, irrigation would be withheld as long as the maximum potential water deficit during the following day did not exceed 0.58 inch. The following day’s maximum expected water deficit is calculated as the present day’s water deficit plus the next day’s maximum potential ET, which is calculated using clear day radiation, the maximum radiation level for that location and time of year.';
  var nonetext = 'The irrigation run time is set to 0 minutes.';

  var raintabletext = 'This table provides detailed information on the effectiveness of rain in reducing irrigation demand. Both the time of day that rain occurred and CF are accounted for in rain effectiveness calculations.';  
  raintabletext += '<br>1) <b>Water deficit (inch)</b> is the past 24 hour’s ETc plus any carryover deficit from previous day(s).';
  raintabletext += '<br>2) <b>Rain total (inch)</b> is the total amount of rain during past 24-hours.';
  raintabletext += '<br>3) <b>Effective rain total (inch)</b> is the amount of rain that offset the water deficit.';
  raintabletext += '<br>4) <b>Water deficit offset (inch)</b> is the amount of the water deficit (inch) that was reduced by effective rain. This value is different than effective rain when CF ≠ 1.';
  raintabletext += '<br>5) <b>% water deficit reduction</b> is the percent of the water deficit reduced by rain (water deficit offset/water deficit).';

  var pagename = "index";
  var pfix = "data/i";
  var subVol = 2400.0;
  var savedRuns = new Array();
  var uploadedFiles = new Array();

  var wth = new Array();
  var wthLat = new Array();
  var wthLong = new Array();
  var wthElev = new Array();
  var wthEnd = new Array();

  var fawn = new Array();
  var fawnId = new Array();
  var fawnLat = new Array();
  var fawnLong = new Array();
  var fawnStart = new Array();
  var fawnElev = new Array();

  var userWth = new Array();
  var userWthStart = new Array();
  var userWthLat = new Array();
  var userWthLong = new Array();
  var userWthElev = new Array();
  var userWthEnd = new Array();

  var wthType = 0;

  var hires = false;
  if (screen.width >= 1280) hires = true;
  
  function writeCookie() {
    var the_date = new Date("December 31, 2019");
    var the_cookie_date = the_date.toGMTString();
    var the_cookie = "screenres="+ screen.width +"x"+ screen.height;
    var the_cookie = the_cookie + ";expires=" + the_cookie_date;
    document.cookie=the_cookie
  } 

  function IsIE() {
    return ( navigator.appName=="Microsoft Internet Explorer" );
  }

  function validChar(e) {
    var k;
    document.all ? k = e.keyCode : k = e.which;
    //letters
    if ((k > 64 && k < 91) || (k > 96 && k < 123)) return true;
    //numbers
    if (k > 47 && k < 58) return true;
    // - _ . backspace cursor,delete
    if (k == 45 || k == 46 || k == 95 || k == 8 || k == 0) return true;
    return false;
  }

  function validDec(e) {
    var k;
    document.all ? k = e.keyCode : k = e.which;
    //numbers
    if (k > 47 && k < 58) return true;
    // backspace cursor,delete
    if (k == 45 || k == 46 || k == 8 || k == 0) return true;
    //e and +
    if (k == 101 || k == 43) return true;
    return false;
  }

  function validNum(e) {
    var k;
    document.all ? k = e.keyCode : k = e.which;
    //numbers
    if (k > 47 && k < 58) return true;
    // backspace cursor,delete
    if (k == 8 || k == 0) return true;
    return false;
  }

   if (document.images) {
      var fawnmap = new Image();
      fawnmap.src = "images/fawnmap2.png";
      var wthmap = new Image();
      wthmap.src = "images/Weather_locations.png";
      var userwth = new Image();
      userwth.src = "images/userwth.png";

      var homeButton = new Image();
      homeButton.src = "images/button-off-home.png";
      var homeButtonOn = new Image();
      homeButtonOn.src = "images/button-on-home.png";
      var moreinfoButton = new Image();
      moreinfoButton.src = "images/button-off-moreinfo.png";
      var moreinfoButtonOn = new Image();
      moreinfoButtonOn.src = "images/button-on-moreinfo.png";
      var calculatorButton = new Image();
      calculatorButton.src = "images/button-off-calculator.png";
      var calculatorButtonOn = new Image();
      calculatorButtonOn.src = "images/button-on-calculator.png";

      var homeButtonSel = new Image();
      homeButtonSel.src = "images/button-sel-home.png";
      var moreinfoButtonSel = new Image();
      moreinfoButtonSel.src = "images/button-sel-moreinfo.png";
      var calculatorButtonSel = new Image();
      calculatorButtonSel.src = "images/button-sel-calculator.png";

      var growerButton = new Image();
      growerButton.src = "images/button-off-grower.png";
      var growerButtonOn = new Image();
      growerButtonOn.src = "images/button-on-grower.png";
      var compButton = new Image();
      compButton.src = "images/button-off-comp.png";
      var compButtonOn = new Image();
      compButtonOn.src = "images/button-on-comp.png";
      var realtimeButton = new Image();
      realtimeButton.src = "images/button-off-realtime.png";
      var realtimeButtonOn = new Image();
      realtimeButtonOn.src = "images/button-on-realtime.png";
      var technicalButton = new Image();
      technicalButton.src = "images/button-off-technical.png";
      var technicalButtonOn = new Image();
      technicalButtonOn.src = "images/button-on-technical.png";
      var usermanualButton = new Image();
      usermanualButton.src = "images/button-off-usermanual.png";
      var usermanualButtonOn = new Image();
      usermanualButtonOn.src = "images/button-on-usermanual.png";
      var sourcecodeButton = new Image();
      sourcecodeButton.src = "images/button-off-sourcecode.png";
      var sourcecodeButtonOn = new Image();
      sourcecodeButtonOn.src = "images/button-on-sourcecode.png";
      var cfButton = new Image();
      cfButton.src = "images/button-off-cf.png";
      var cfButtonOn = new Image();
      cfButtonOn.src = "images/button-on-cf.png";
      var linksButton = new Image();
      linksButton.src = "images/button-off-links.png";
      var linksButtonOn = new Image();
      linksButtonOn.src = "images/button-on-links.png";
      var contactButton = new Image();
      contactButton.src = "images/button-off-contact.png";
      var contactButtonOn = new Image();
      contactButtonOn.src = "images/button-on-contact.png";
      var creditsButton = new Image();
      creditsButton.src = "images/button-off-credits.png";
      var creditsButtonOn = new Image();
      creditsButtonOn.src = "images/button-on-credits.png";

      var graphImage = new Image();
      graphImage.src = "graph.php";
   }

   function on(imgName) {
      if (document.images)
        document[imgName].src = eval(imgName + 'ButtonOn.src');
   }

   function off(imgName) {
      document[imgName].src = eval(imgName + 'Button.src');
   }

  function checkRuns(update) {
    var retVal = true;
    for (j = 0; j < savedRuns.length; j++) {
      if (savedRuns[j] == document.paramForm.savename.value) {
	retVal = confirm("A run already exists with the name "+savedRuns[j]+".  Are you sure you want to overwrite it?");
      }
    }
    if (update) updateTabs();
    return retVal;
  }

  function checkRunsOutput() {
    var retVal = true;
    for (j = 0; j < savedRuns.length; j++) {
      if (savedRuns[j] == document.outputSave.savename.value) {
        retVal = confirm("A run already exists with the name "+savedRuns[j]+".  Are you sure you want to overwrite it?");
      }
    }
    return retVal;
  }

  function updateTabs() {
    if (self.name == "output") {
      self.name = "input";
      outputTab = null;
    }
    if (outputTab != null) outputTab.close();
    outputTab = window.open('', 'output');
    outputTab.close();
  }

  function dispHandle(name, show) {
    var obj = document.getElementById(name);
    if (show == true) obj.style.display = ""; else obj.style.display = "none";
  }

  function selectOutputDiv(name) {
    if (name == "dailygraphs") {
      dispHandle("dailygraphs", true);
      document.getElementById("dailygraphsspan").style.color = "red";
    } else {
      dispHandle("dailygraphs", false);
      document.getElementById("dailygraphsspan").style.color = "black";
    }

    if (name == "summarycharts") {
      dispHandle("summarycharts", true);
      document.getElementById("summarychartsspan").style.color = "red";
    } else {
      dispHandle("summarycharts", false);
      document.getElementById("summarychartsspan").style.color = "black";
    }

    if (name == "summarytables") {
      dispHandle("summarytables", true);
      document.getElementById("summarytablesspan").style.color = "red";
    } else {
      dispHandle("summarytables", false);
      document.getElementById("summarytablesspan").style.color = "black";
    }

    if (name == "viewfiles") {
      dispHandle("viewfiles", true);
      document.getElementById("viewfilesspan").style.color = "red";
    } else {
      dispHandle("viewfiles", false);
      document.getElementById("viewfilesspan").style.color = "black";
    }
  }

  function selectCompOutputDiv(name) {
    if (name == "summarygraphs") {
      dispHandle("summarygraphs", true);
      document.getElementById("summarygraphsspan").style.color = "red";
    } else {
      dispHandle("summarygraphs", false);
      document.getElementById("summarygraphsspan").style.color = "black";
    }

    if (name == "summarytables") {
      dispHandle("summarytables", true);
      document.getElementById("summarytablesspan").style.color = "red";
    } else {
      dispHandle("summarytables", false);
      document.getElementById("summarytablesspan").style.color = "black";
    }

    if (name == "viewfiles") {
      dispHandle("viewfiles", true);
      document.getElementById("viewfilesspan").style.color = "red";
    } else {
      dispHandle("viewfiles", false);
      document.getElementById("viewfilesspan").style.color = "black";
    }
  }

  function selectExampleDiv(name) {
    if (name == "wthexample") {
      dispHandle("wthexample", true);
    } else dispHandle("wthexample", false);

    if (name == "pltexample") {
      dispHandle("pltexample", true);
    } else dispHandle("pltexample", false);

    if (name == "sfnexample") {
      dispHandle("sfnexample", true);
    } else dispHandle("sfnexample", false);

    if (name == "irrexample") {
      dispHandle("irrexample", true);
    } else dispHandle("irrexample", false);
  }

  function showReport() {
    dispHandle("reportdiv", true);
  }

  function hideReport() {
    dispHandle("reportdiv", false);
  }

  function setReportChecks(name) {
    var elem = eval('document.reportForm.'+name);
    var list = document.reportForm.getElementsByTagName('INPUT');
    for (j = 0; j < list.length; j++) {
      if (list[j].name.indexOf(name+"_") == 0) {
        list[j].disabled = !elem.checked;
      }
    }
  }

  function checkReport(name, n, elem, id, ytitle) {
    var list = document.reportForm.getElementsByTagName('INPUT');
    var x = 0;
    for (j = 0; j < list.length; j++) {
      if (list[j].name.indexOf(name+"_") == 0) {
	if (list[j].checked) x++;
      }
    }
    if (x > n) {
      elem.checked=false;
      alert("There are already "+n+" items selected.  You must uncheck one of them before selecting a new plot.");
    } else {
      var index = parseInt(elem.name.substring(elem.name.lastIndexOf("_")+1));
      elem.value = getGraphURL(name, id, index, ytitle);
    }
  }

  function showToolTipGraph(name, n, elem, id, ytitle) {
    var index = parseInt(elem.name.substring(elem.name.lastIndexOf("_")+1));
    var url = getGraphURL(name, id, index, ytitle);
    if (isIE) {
      var r = Math.random();
      url += "&" + r;
    }
    ddrivetip('<img src="' + url + '">','99ffaa',graphImage.width);
  }

  function toggleReportState(pfix, i1, i2, state) {
    for (var j = i1; j < i2; j++) {
      var cbox = document.getElementById(pfix+"_"+j);
      if (cbox.checked != state) cbox.click();
    }
  }

  function popupText(text) { 
    var generator=window.open('','name','height=250,width=450');
    generator.document.write('<html><head><title>More info</title>');
    generator.document.write('</head><body>');
    generator.document.write(text);
    generator.document.write('</body></html>');
    generator.document.close();
  }

  function setLocation(loc) {
    for (j = 0; j < document.paramForm.WFNAME.length; j++) {
      if (document.paramForm.WFNAME.options[j].value.indexOf(loc) != -1) {
        document.paramForm.WFNAME.options[j].selected=true;
      } 
    }
    updateYear();
  }

  function updateFawnLoc(loc) {
    var obj = document.getElementById("weatherInfo");
    for (j = 0; j < fawn.length; j++) {
      if (loc == fawn[j].substring(5)) {
	desc = '<a href="http://fawn.ifas.ufl.edu/station.php?id=' + fawnId[j] + '" target="ccmt">';
	desc += '<b>' + loc.replace(/_/g," ") + '</b></a><br>';
	desc += 'Start Date: '+fawnStart[j]+'<br>';
        desc += 'End Date: '+latest+'<br>';
	desc += 'Lattitude: '+fawnLat[j]+'<br>';
	desc += 'Longitude: '+fawnLong[j]+'<br>';
        desc += 'Elevation (m): '+fawnElev[j]+'<br>';
	obj.innerHTML = desc;
      }
    }
  }

  function updateWthLoc(loc) {
    var obj = document.getElementById("weatherInfo");
    for (j = 0; j < wth.length; j++) {
      if (loc == wth[j]) {
        desc = '<b>' + loc.replace(/_/g," ") + '</b><br>';
        desc += 'Start Date: '+wthStart+'<br>';
        desc += 'End Date: '+wthEnd[j]+'<br>';
        desc += 'Lattitude: '+wthLat[j]+'<br>';
        desc += 'Longitude: '+wthLong[j]+'<br>';
        desc += 'Elevation (m): '+wthElev[j]+'<br>';
        obj.innerHTML = desc;
      }
    }
  }

  function updateUserWth() {
    if (wthType == 2) {
      var obj = document.getElementById("weatherInfo");
      var j = document.paramForm.WFNAME.selectedIndex;
      desc = '<b>' + document.paramForm.WFNAME.options[j].text.replace(/_/g," ") + '</b><br>';
      desc += 'Start Date: '+userWthStart+'<br>';
      desc += 'End Date: '+userWthEnd[j]+'<br>';
      desc += 'Lattitude: '+userWthLat[j]+'<br>';
      desc += 'Longitude: '+userWthLong[j]+'<br>';
      desc += 'Elevation (m): '+userWthElev[j]+'<br>';
      obj.innerHTML = desc;
    }
  }

  function updateUserWthLoc(i) {
    if (wthType == 2) {
      var obj = document.getElementById("weatherInfo");
      sel = eval('document.paramForm.WFNAME_'+i);
      if (typeof sel == "undefined") return;
      var j = sel.selectedIndex;
      desc = '<b>' + sel.options[j].text.replace(/_/g," ") + '</b><br>';
      desc += 'Start Date: '+userWthStart+'<br>';
      desc += 'End Date: '+userWthEnd[j]+'<br>';
      desc += 'Lattitude: '+userWthLat[j]+'<br>';
      desc += 'Longitude: '+userWthLong[j]+'<br>';
      desc += 'Elevation (m): '+userWthElev[j]+'<br>';
      obj.innerHTML = desc;
    }
  }

  function setDayList(n) {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    nd = daysInMonth[n];
    nsel = document.paramForm.day.selectedIndex;
    document.paramForm.day.options.length=0;
    for (j = 0; j < nd; j++) {
      document.paramForm.day.options[j] = new Option(j+1, j+1, false, false);
    }
    document.paramForm.day.options[nsel].selected = true;
  }

  function setDay(x) {
    var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    var month = document.paramForm.month.selectedIndex;
    var doy = 0;
    for (j = 0; j < month; j++) {
      doy+=daysInMonth[j];
    }
    doy += document.paramForm.day.selectedIndex+1;
    document.paramForm.PLT_DOY.value = doy;
    if (x) updateYear();
  }

  function changeHarv(x) {
    df = document.getElementById('HARV');
    desc = '<ul style="margin-top: 0px; margin-left: 50px">';
    if (x == 0) {
      desc+= '<select name="HARVDAYS">';
      for (j = 10; j <= 78; j++) {
        if (j == 20) defSel = " selected"; else defSel = "";
        desc+='<option name="'+(j*7)+'" value="'+(j*7)+'"'+defSel+'>'+j;
      }
      desc+='</select> weeks';
    } else if (x == 1) {
      desc+= '<select name="HARV_HT">';
      for (j = 12; j<=36; j++) {
	if (j == 18) defSel = " selected"; else defSel = "";
	desc+='<option name="'+(j*2.54)+'" value="'+(j*2.54)+'"'+defSel+'>'+j;
      }
      desc+='</select> inches';
    }
    desc+='</ul>';
    df.innerHTML = desc;
  }

  function changeSpecies(species) {
    var contSize = 1;
    for (j = 0; j < document.paramForm.container_size.length; j++) {
      if (document.paramForm.container_size[j].checked) contSize = document.paramForm.container_size[j].value;
    }
    var tempFile = ""+contSize+"-gal_default";
    if (species != "vibur") tempFile += "_"+species;
    tempFile += ".txt";
    document.paramForm.template.value = tempFile; 
    var newHarvHt = parseFloat(defaultValues[tempFile]["HARV_HT"]);
    if (pagename != "realtime") {
      //update HARV_HT
      var harvHtOpt = document.paramForm.HARV_HT.options;
      for (j = 0; j < harvHtOpt.length; j++) {
        if (harvHtOpt[j].value == newHarvHt) {
          document.paramForm.HARV_HT.selectedIndex = j;
        }
      }
    }
  }

  function changeContSize(contSize) {
    var species = "vibur";
    for (j = 0; j < document.paramForm.species.length; j++) {
      if (document.paramForm.species[j].checked) species = document.paramForm.species[j].value;
    }
    var tempFile = ""+contSize+"-gal_default";
    if (species != "vibur") tempFile += "_"+species;
    tempFile += ".txt";
    document.paramForm.template.value = tempFile;
    var newHarvHt = parseFloat(defaultValues[tempFile]["HARV_HT"]);
    var newLbRate = 2.5;
    subVol = parseFloat(defaultValues[tempFile]["S_VOL"]);
    document.paramForm.S_VOL.value = parseFloat(defaultValues[tempFile]["S_VOL"]);
    document.paramForm.POTDIAM.value = parseFloat(defaultValues[tempFile]["POTDIAM"]);
    document.paramForm.NOPMAX.value = parseFloat(defaultValues[tempFile]["NOPMAX"]);
    
    if (contSize == 3) {
      newLbRate = 3.0;
    }
    if (pagename != "realtime") {
      //update HARV_HT
      var harvHtOpt = document.paramForm.HARV_HT.options;
      for (j = 0; j < harvHtOpt.length; j++) {
        if (harvHtOpt[j].value == newHarvHt) {
          document.paramForm.HARV_HT.selectedIndex = j;
        }
      }
    }
    if (pagename.indexOf("fert") == -1) {
      //update FERT and set to 2.5 or 3 lb
      //toggle radio button if g per container selected.
      var lbrate = document.paramForm.lbrate;
      for (j = 0; j < lbrate.options.length; j++) {
	if (lbrate.options[j].value == newLbRate) lbrate.selectedIndex = j;
      }
      document.paramForm.fertb[0].click();
    } else {
      changeFert2(0,1);
      changeFert2(0,2);
      if (document.getElementById("fertb3lb") != null) changeFert2(0,3);
      if (document.getElementById("fertb4lb") != null) changeFert2(0,4);
    } 
    //update PTA0 and PTA1
    updatePTA();
    if (pagename == "realtime" && document.paramForm.PTA2 != null) {
      adjMove();
    }
  }

  function changeFert(x,n) {
    if (pagename.indexOf("technical") != -1) return;
    if (x == -1) {
      if (pagename.indexOf("fert") != -1) {
	if (document.paramForm.fertb1[0].checked) x = 0; else x = 1;
      }
      else if (document.paramForm.fertb[0].checked) x = 0; else x = 1; 
      document.paramForm.PCT_P.value = 0.436*document.paramForm.PCT_P2O5.value;
    }
    if (pagename.indexOf("fert") != -1) return changeFert2(x,n);
    document.paramForm.fertb[x].click();
    if (x == 0) {
      var a = document.paramForm.lbrate.options[document.paramForm.lbrate.selectedIndex].value * 0.059325 * subVol;
      document.paramForm.FERT.value = a/document.paramForm.PCT_N.value;
      var grate = document.paramForm.grate;
      for (j = 0; j < grate.options.length; j++) {
	if (grate.options[j].value == Math.round(document.paramForm.FERT.value)) grate.selectedIndex = j;
      }
      if (document.paramForm.FERT.value < parseInt(grate.options[0].value)) grate.selectedIndex = 0;
    } else if (x == 1) {
      document.paramForm.FERT.value  = document.paramForm.grate.options[document.paramForm.grate.selectedIndex].value;
      var a = document.paramForm.FERT.value*document.paramForm.PCT_N.value/(0.059325 * subVol);
      var lbrate = document.paramForm.lbrate;
      for (j = 0; j < lbrate.options.length; j++) {
        if (lbrate.options[j].value*10 == Math.round(a*10)) lbrate.selectedIndex = j;
      }
      if (a > lbrate.options[j-1].value) lbrate.selectedIndex = j-1;
    }
  }

  function changeImages(imgName, path) {
    if (document.images) document[imgName].src = path;
  }

  function changeMove1(x) {
    df = document.getElementById('spac1');
    desc = ''
    if (x == 0) {
      showMoveImg = false;
      if (document.images) document['moveImg'].src = 'images/spacer.png';
      document.paramForm.MOVE1.value=-99;
    }
    else if (x >= 1) {
      if (x == 1) {
        document.paramForm.MOVE1.value = (document.paramForm.move1w.selectedIndex+1)*7;
      }
      else if (x == 2) {
        document.paramForm.MOVE1.value=-99;
        document.paramForm.MOVE_LAI.value=3;
      }
      showMoveImg = true;
      //if (document.images) document['moveImg'].src = moveImgSrc;
      desc+='<ul style="margin-top: 20px; margin-left: -5px">';
      desc+='<b>Container spacing after move:</b><br>';
      desc+='<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #ff0000;">B = </span>';
      desc+='<select name="SPAC1_B" onChange="updatePTA();">';
      for (j = 0; j <= 18; j++) {
	desc+='<option name=' + j + ' value=' + j + '>' + j + ' inches';
      }
      desc+='<option name="contdiam" value="contdiam" selected>Cont. Diam.';
      desc+='<option name="3/4" value="3/4">3/4 Diameter';
      desc+='</select> between top edge of containers<br>';
      desc+='<span style="font: 16pt arial,helvetica,sans-serif; font-weight: bold; color: #0000ff;">W = </span>';
      desc+='<select name="SPAC1_W" onChange="updatePTA();">';
      for (j = 0; j <= 18; j++) {
        desc+='<option name=' + j + ' value=' + j + '>' + j + ' inches';
      }
      desc+='<option name="contdiam" value="contdiam" selected>Cont. Diam.';
      desc+='<option name="3/4" value="3/4">3/4 Diameter';
      desc+='</select> between top edge of containers<br>';
      desc+='</ul>';
    }
    df.innerHTML = desc;
    updatePTA();
  }

  function changeIrr(x) {
    df = document.getElementById('irrdiv');
    desc = '';
    if (x == 0) {
      desc+='<ul style="margin-left: -15px;">';
      desc+='<table border=1 style="margin-left: 5px; margin-top: 5px;" cellpadding=5>';
      desc+='<tr><td>';
      desc+='<b>Automatically shutoff irrigation if rain exceeds daily rate: </b>';
      desc+='<input type="radio" name="RAINCUT" value="YES">Yes';
      desc+='<input type="radio" name="RAINCUT" value="NO" checked>No<br>';
      desc+='<b>Irrigation rate at start of season: </b>';
      desc+='<select name="D_IRR0in" onChange="document.paramForm.D_IRR0.value=this.value*2.54;">';
      for (j = 0; j <= 15; j++) {
	x = j/10.
        if (x == 0.4) defSel = ' selected'; else defSel = '';
        desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
      } 
      desc+='</select> inches per day<br>';

      desc+='<b>1st change in irrigation rate (optional): </b>';
      desc+='<select name="irr1w" onChange="updateIrr(1);">';
      desc+='<option name="None" value="None" selected>None';
      for (j = 1; j <= 30; j++) {
        desc+='<option name=' + j + ' value=' + j + '>' + j;
      }
      desc+='</select> weeks after planting ';
      desc+='<select name="D_IRR1in" onChange="document.paramForm.D_IRR1.value=this.value*2.54;">';
      for (j = 0; j <= 15; j++) {
        x = j/10.
        if (x == 0.3) defSel = ' selected'; else defSel = '';
        desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
      } 
      desc+='</select> inches per day<br>';

      desc+='<b>2nd change in irrigation rate (optional): </b>';
      desc+='<select name="irr2w" onChange="updateIrr(2);">';
      desc+='<option name="None" value="None" selected>None';
      desc+='</select> weeks after planting ';
      desc+='<select name="D_IRR2in" onChange="document.paramForm.D_IRR2.value=this.value*2.54;">';
      for (j = 0; j <= 15; j++) {
        x = j/10.
        if (x == 0.4) defSel = ' selected'; else defSel = '';
        desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
      } 
      desc+='</select> inches per day<br>';

      desc+='<b>3rd change in irrigation rate (optional): </b>';
      desc+='<select name="irr3w" onChange="updateIrr(3);">';
      desc+='<option name="None" value="None" selected>None';
      desc+='</select> weeks after planting ';
      desc+='<select name="D_IRR3in" onChange="document.paramForm.D_IRR3.value=this.value*2.54;">';
      for (j = 0; j <= 15; j++) {
        x = j/10.
        if (x == 0.5) defSel = ' selected'; else defSel = '';
        desc+='<option name=' + x + ' value=' + x + defSel + '>' + x;
      } 
      desc+='</select> inches per day<br>';

      desc+='<input type="hidden" name="IRR1" value=-99>';
      desc+='<input type="hidden" name="IRR2" value=-99>';
      desc+='<input type="hidden" name="IRR3" value=-99>';

      desc+='</td></tr>';
      desc+='</table></ul>';
    }
    df.innerHTML = desc;
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

  function updatePTA() {
    var b,w;
    var spacb = document.paramForm.SPAC_B.options[document.paramForm.SPAC_B.selectedIndex].value;
    var spacw = document.paramForm.SPAC_W.options[document.paramForm.SPAC_W.selectedIndex].value;
    var potDiam = eval(document.paramForm.POTDIAM.value);

    if (spacb == "contdiam") {
      b = 2*potDiam;
    } else if (spacb == "3/4") {
      b = 1.75*potDiam;
    } else b = potDiam + 2.54*spacb;
    if (spacw == "contdiam") {
      w = 2*potDiam;
    } else if (spacw == "3/4") {
      w = 1.75*potDiam;
    } else w = potDiam + 2.54*spacw;

    var tri = document.paramForm.ARRANGE[0].checked;
    if (tri) {
      document.paramForm.PTA0.value = Math.pow(Math.pow(b,2) - Math.pow(w/2, 2), 0.5)*w;
    } else document.paramForm.PTA0.value = b*w;

    if (!document.paramForm.MOVE || document.paramForm.MOVE == null) return;
    if (!document.paramForm.MOVE[0]) return;

    if (document.paramForm.MOVE[0].checked) {
      document.paramForm.PTA1.value = 0;
    } else {
      var b1, w1;
      var spacb1 = document.paramForm.SPAC1_B.options[document.paramForm.SPAC1_B.selectedIndex].value;
      var spacw1 = document.paramForm.SPAC1_W.options[document.paramForm.SPAC1_W.selectedIndex].value;

      if (spacb1 == "contdiam") {
	b1 = 2*potDiam;
      } else if (spacb1 == "3/4") {
	b1 = 1.75*potDiam;
      } else b1 = potDiam + 2.54*spacb1;
      if (spacw1 == "contdiam") {
	w1 = 2*potDiam;
      } else if (spacw1 == "3/4") {
        w1 = 1.75*potDiam;
      } else w1 = potDiam + 2.54*spacw1;

      if (tri) {
        document.paramForm.PTA1.value = Math.pow(Math.pow(b1,2) - Math.pow(w1/2, 2), 0.5)*w1;
      } else document.paramForm.PTA1.value = b1*w1;
    }
  }

  function changeFDays() {
    document.paramForm.CRF_DAYS.value = Math.floor(document.paramForm.fmonth.options[document.paramForm.fmonth.selectedIndex].value*365.25/12.+0.5);
  }

  function changeWeather(x) {
    wthType = x;
    var obj = document.getElementById("weatherInfo");
    obj.innerHTML = '';
    document.paramForm.WFNAME.options.length=0;
    if (x == 0) {
      document['weatherImage'].src = fawnmap.src;
      document['weatherImage'].useMap = "#fawnMap"; 
      for (j = 0; j < fawn.length; j++) {
	document.paramForm.WFNAME.options[j] = new Option(fawn[j].replace("fawn/","").replace(/_/g," "), fawn[j], false, false);
      }
    } else if (x == 1) {
      document['weatherImage'].src = wthmap.src;
      document['weatherImage'].useMap = "#weatherMap"; 
      for (j = 0; j < wth.length; j++) {
        document.paramForm.WFNAME.options[j] = new Option(wth[j].replace("fawn/","").replace(/_/g," "), wth[j], false, false);
      }
    } else if (x == 2) {
      document['weatherImage'].src = userwth.src; 
      document['weatherImage'].useMap = "#"; 
      for (j = 0; j < userWth.length; j++) {
        document.paramForm.WFNAME.options[j] = new Option(userWth[j].substring(userWth[j].lastIndexOf("/")+1), userWth[j], false, false);
      }
      updateUserWth();
    }
    updateYear();
  }

  function changeAllWeather(x) {
    wthType = x;
    var obj = document.getElementById("weatherInfo");
    obj.innerHTML = '';
    if (x == 0) {
      document['weatherImage'].src = fawnmap.src;
      document['weatherImage'].useMap = "#fawnMap"; 
    } else if (x == 1) {
      document['weatherImage'].src = wthmap.src;
      document['weatherImage'].useMap = "#weatherMap"; 
    } else if (x == 2) {
      document['weatherImage'].src = userwth.src; 
      document['weatherImage'].useMap = "#";
    }
    for (i = 1; i < 5; i++) {
      sel = eval('document.paramForm.WFNAME_'+i);
      if (typeof sel == "undefined") continue;
      sel.options.length = 0;
      if (x == 0) {
	for (j = 0; j < fawn.length; j++) {
          sel.options[j] = new Option(fawn[j].replace("fawn/","").replace(/_/g," "), fawn[j], false, false);
	}
	sel.options[i-1].selected = true;
      } else if (x == 1) {
	for (j = 0; j < wth.length; j++) {
	  sel.options[j] = new Option(wth[j].replace("fawn/","").replace(/_/g," "), wth[j], false, false);
	}
        sel.options[i-1].selected = true;
      } else if (x == 2) {
        for (j = 0; j < userWth.length; j++) {
	  sel.options[j] = new Option(userWth[j].substring(userWth[j].lastIndexOf("/")+1), userWth[j], false, false);
        }
	if (sel.options.length >= i) sel.options[i-1].selected = true;
      }
    }
    updateUserWthLoc(1);
    updateYear();
  }

  function setFormState(form, state) {
    var formElem = form.elements;
    var x = 0;
    if (state == false) return;
    for (var elem, i = 0; ( elem = formElem[i] ); i++) {
      var type = elem.type;
      if (type == "hidden" || type == "submit" || type == "reset") continue; 
      if (document.getElementById("hidden_"+elem.name+"_"+elem.value) != null) continue; //already have element 
      var input = document.createElement("input");
      input.setAttribute("type", "hidden");
      input.setAttribute("name", elem.name); 
      input.setAttribute("id", "hidden_"+elem.name+"_"+elem.value);
      input.setAttribute("value", elem.value); 
      form.appendChild(input);
      elem.disabled = state;
    }
  }

  //tooltip that (hopefully) works with IE
  /***********************************************
  * Cool DHTML tooltip script- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
  * This notice MUST stay intact for legal use
  * Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
  ***********************************************/

  var offsetxpoint=-60 //Customize x offset of tooltip
  var offsetypoint=20 //Customize y offset of tooltip
  var ie=document.all
  var ns6=document.getElementById && !document.all
  var enabletip=false
  var tipobj = null;
  //if (ie||ns6)
  //var tipobj=document.all? document.all["dhtmltooltip"] : document.getElementById? document.getElementById("dhtmltooltip") : ""

  function ietruebody(){
    return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
  }

  function ddrivetip(thetext, thecolor, thewidth){
    tipobj = document.getElementById("dhtmltooltip");
    if (ns6||ie){
      if (typeof thewidth!="undefined") tipobj.style.width=thewidth+"px"
      if (typeof thecolor!="undefined" && thecolor!="") tipobj.style.backgroundColor=thecolor
      tipobj.innerHTML=thetext
      enabletip=true
      return false
    }
  }

  function positiontip(e){
    tipobj = document.getElementById("dhtmltooltip");
    if (enabletip){
      var curX=(ns6)?e.pageX : event.clientX+ietruebody().scrollLeft;
      var curY=(ns6)?e.pageY : event.clientY+ietruebody().scrollTop;
      //Find out how close the mouse is to the corner of the window
      var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20
      var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20
      var leftedge=(offsetxpoint<0)? offsetxpoint*(-1) : -1000

      //if the horizontal distance isn't enough to accomodate the width of the context menu
      if (rightedge<tipobj.offsetWidth)
        //move the horizontal position of the menu to the left by it's width
        tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+"px" : window.pageXOffset+e.clientX-tipobj.offsetWidth+"px"
      else if (curX<leftedge)
        tipobj.style.left="5px"
      else
	//position the horizontal position of the menu where the mouse is positioned
	tipobj.style.left=curX+offsetxpoint+"px"

      //same concept with the vertical position
      if (bottomedge<tipobj.offsetHeight)
	tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+"px" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+"px"
      else
	tipobj.style.top=curY+offsetypoint+"px"
      tipobj.style.visibility="visible"
    }
  }

  function hideddrivetip(){
    tipobj = document.getElementById("dhtmltooltip");
    if (ns6||ie){
      enabletip=false
      tipobj.style.visibility="hidden"
      tipobj.style.left="-1000px"
      tipobj.style.backgroundColor=''
      tipobj.style.width=''
    }
  }

  function isValidKey(key) {
    if (key == "isSubmitted" || key == "timestamp" || key == "id") return false;
    if (key == "Submit") return false;
    return true;
  }

  function setFormElement(form, name, value) {
    elem = eval("document." + form + "." + name);
    if (elem == null) return;
    type = eval("document." + form + "." + name + ".type");
    if (type == "hidden" || type == "text") {
      elem.value = value;
    } else if (type == "select-one") {
      for (j = 0; j < elem.length; j++) {
        if (elem.options[j].value == value) {
          elem.options[j].selected=true;
        }
      }
    } else if (type == "checkbox") {
      if (!elem.checked) elem.click();
    } else if (elem.length > 0) {
      if (elem[0].type == "radio") {
	for (j = 0; j < elem.length; j++) {
          if (elem[j].value == value) {
            elem[j].click();
          } 
        }
      }
    }
    if (elem.onchange) elem.onchange();
    if (elem.onclick) elem.onclick();
  }

document.onmousemove=positiontip

</script>

</head>
<body>
<div id="dhtmltooltip"></div>
<?php
   date_default_timezone_set('America/New_York');
   require('stats.php');
   $pagename = "index";
   $pfix = "data/i";
   if (isset($_POST['S_VOL'])) {
     $subVol = $_POST['S_VOL'];
   } else $subVol = 2400.0;
   $nruns = 1;
   $npspec = count(file("/home/mybmp/driver/1_P_spec.txt"))+1;
   $npspec /= 4;

   ### Glob on all default P_spec files.    ####
   ### Read them in and store in hash.  Copy ###
   ### these values over to javascript.      ###
   $defFileList = glob("*gal*default*.txt");
   $defaultValues = Array();
   for ($j = 0; $j < count($defFileList); $j++) {
      $defaultValues[$defFileList[$j]] = Array();
      $spec = file($defFileList[$j]);
      for ($l = 0; $l < $npspec; $l++) {
	$temp = trim($spec[$l*4+1]);
	$tempnames = preg_split("/\s+/", $temp);
	$temp = trim($spec[$l*4+2]);
	$tempvals = preg_split("/\s+/", $temp);
	for ($i = 0; $i < count($tempnames); $i++) {
	  $defaultValues[$defFileList[$j]][$tempnames[$i]] = $tempvals[$i];
        }
      }
   }
   echo "<script language='javascript'>\n";
   echo "\tvar defaultValues = new Object();\n";
   for ($j = 0; $j < count($defFileList); $j++) {
      echo "\tdefaultValues['" . $defFileList[$j] . "'] = new Object();\n";
      foreach ($defaultValues[$defFileList[$j]] as $key=>$value) {
	echo "\t\tdefaultValues['" . $defFileList[$j] . "']['" . $key . "'] = '" . $value . "';";
      }
   }
   echo "</script>\n";

   $isIE = false;
   $useragent = $_SERVER['HTTP_USER_AGENT'];
   if (preg_match("/MSIE/i", $useragent)) $isIE = true;
   #if (eregi("MSIE", $useragent)) $isIE = true; 
   echo '<script language="javascript">';
   if ($isIE) echo 'var isIE = true;'; else echo 'var isIE=false;';
   echo '</script>';

   $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");
   $affiliations = array("Grower", "University or County Extension", "Allied Industry", "Research", "Education - Teacher", "Education - Student", "Government - Regulatory", "Government - Nonregulatory", "Non-profit Organization", "Other (specify)");


   //New daily files
   $dailyfiles = array("daily", "Nplant", "NRel", "Nleach", "Pplant", "PRel", "Pleach");
   $skipl = array(3, 3, 6, 3, 3, 3, 3);

   //7/28/14 -- new zone type enum definitions
   $zoneTypes = array("ET-sprinkler", "ET-micro", "LF-sprinkler", "LF-micro");


   function stripQuotes($string) {
      return str_replace('"', '\"', str_replace("'","\'",$string));
   }

   function removeQuotes($string) {
      return str_replace('"', '', str_replace("'", "", $string));
   }

   function findValidYears($wthfile, $doy) {
      $wth = file($wthfile . '.wth');
      $isValid = false;
      $n = 0;
      while (!$isValid) {
	if (strpos($wth[$n], '@DATE') !== false) $isValid = true; 
	$n++;
      }
      $startYr = floor(substr($wth[$n],0,4));
      $startDay = floor(substr($wth[$n],4,3));
      if ($startDay > $doy) $startYr++;
      $endYr = floor(substr($wth[count($wth)-1], 0, 4));
      $endDay = floor(substr($wth[count($wth)-1], 4, 3));
      if ($endDay <= $doy) $endYr--;
      return array($startYr, $endYr);
   }

   function showOutputButtons() {
      echo '<center><table border=0 align="center"><tr>';
      //echo '<td><a href="javascript:history.go(-1);"><img src="images/back-off.png" border=0 onMouseOver=\'this.src="images/back-on.png";\' onMouseOut=\'this.src="images/back-off.png";\'></a></td>';
      //echo '<td width=20></td>';
      echo '<td><a href="javascript:print();"><img src="images/print-off.png" border=0 onMouseOver=\'this.src="images/print-on.png";\' onMouseOut=\'this.src="images/print-off.png";\'></a></td>';
      echo '<td width=20></td>';
      echo '<td><a href="javascript:showReport();"><img src="images/report-off.png" border=0 onMouseOver=\'this.src="images/report-on.png";\' onMouseOut=\'this.src="images/report-off.png";\'></a></td>';
      echo '</tr></table><br></center>';
      echo '<script language="javascript">if (document.title.indexOf("Output") == -1) document.title += ": Output";</script>';
   }

   function isWthFile($wfile) {
      $isWth = true;
      $wth = file($wfile);
      $head = array('@', 'INSI', 'LAT', 'LONG', 'ELEV', 'TAV', 'AMP', 'REFHT', 'WNDHT');
      if (substr($wth[0], 0, 8) != '*WEATHER') return false;
      if (trim($wth[1]) != '') return false;
      if (preg_split('/\s+/', trim($wth[2])) != $head) return false;
      if (count(preg_split('/\s+/', trim($wth[3]))) != 8) return false;
      for ($j = 4; $j < count($wth); $j++) {
	if (trim($wth[$j]) == '') continue;
	if (count(preg_split('/\s+/', trim($wth[$j]))) != 5) return false;
	if (stripos($wth[$j], '<?php') !== false) return false;
      }
      return true;
   }

   function isPltFile($pfile) {
      $isPlt = true;
      $plt = file($pfile);
      $vibur = file('VIBUR_OD.plt');
      for ($j = 0; $j < count($plt); $j++) {
        if (trim($plt[$j]) == '') continue;
        if (stripos($plt[$j], '<?php') !== false) return false;
        if ($j % 4 != 2 && preg_split('/\s+/', trim($plt[$j])) != preg_split('/\s+/', trim($vibur[$j]))) return false;
        if ($j % 4 == 2 && count(preg_split('/\s+/', trim($plt[$j]))) != count(preg_split('/\s+/', trim($vibur[$j])))) return false;
      }
      return true;
   }

   function isSfnFile($sfile) {
      $isSfn = true;
      $sfn = file($sfile);
      $head = array('@DATE','SFN');
      if (preg_split('/\s+/', trim($sfn[0])) != $head) return false;
      for ($j = 1; $j < count($sfn); $j++) {
        if (trim($sfn[$j]) == '') continue;
        if (count(preg_split('/\s+/', trim($sfn[$j]))) != 2) return false;
        if (stripos($sfn[$j], '<?php') !== false) return false;
      }
      return true;
   }

   function isIrrFile($ifile) {
      $isIrr = true;
      $irr = file($ifile);
      $head = array('@DATE','IRR');
      if (preg_split('/\s+/', trim($irr[0])) != $head) return false;
      for ($j = 1; $j < count($irr); $j++) {
        if (trim($irr[$j]) == '') continue;
        if (count(preg_split('/\s+/', trim($irr[$j]))) != 2) return false;
        if (stripos($irr[$j], '<?php') !== false) return false;
      }
      return true;
   }

   function getLastLine($wfile) {
      $fp = fopen($wfile, 'r');
      $pos = -2; $line = ''; $c = '';
      do {
	$line = $c . $line;
	fseek($fp, $pos--, SEEK_END);
	$c = fgetc($fp);
      } while ($c != PHP_EOL);
      fclose($fp);
      return $line;
   }

   function isComp($pagename) {
     if ($pagename == "fert") return true;
     if ($pagename == "fert_irr") return true;
     if ($pagename == "irr") return true;
     if ($pagename == "date") return true;
     if ($pagename == "location") return true;
     return false;
   }

   require('dbcnx.php');
   #mysql_select_db("mybmp");

   //check if user is logged in
   $isLoggedIn = false;
   $doPreview = false;

   if (isset($_SESSION['bmpid'])) {
      $sql = 'select firstName, username, birthday from users where uid = ' . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
           $birthday = $row['birthday'];
           $firstName = $row['firstName'];
           $bmpusername = $row['username'];
           $isLoggedIn = true;
        }
      }
   } else if (isset($_SESSION['preview']) and $_SESSION['preview'] === true) {
      $doPreview = true;
   } else if (isset($_POST['showPreview']) and $_POST['showPreview'] == "true") {
      $_SESSION['preview'] = true;
      $doPreview = true;
   }

   $plotWidth = 320;
   $plotHeight = 240;
   $hires = true;
   $plotbg = "white";
   $plotfg = "black";
   if ($isLoggedIn) {
      $sql = 'select * from prefs where uid = ' . $_SESSION['bmpid'];
      $out = $mysqli_db->query($sql);
      if ($out != false) {
        if ($row = $out->fetch_array(MYSQLI_BOTH)) {
          $plotWidth = $row['plotWidth'];
          $plotHeight = $row['plotHeight'];
          $plotbg = $row['plotbg'];
          $plotfg = $row['plotfg'];
          $hires = $row['hires'];
	  echo '<script language="javascript">';
	  if ($hires == 1) echo 'hires=true'; else echo 'hires=false';
	  echo '</script>';
	}
      }
   }
   $_SESSION['pw'] = $plotWidth;
   $_SESSION['ph'] = $plotHeight;
   $_SESSION['bg'] = str_replace("%23","#",$plotbg);
   $_SESSION['fg'] = str_replace("%23","#",$plotfg);

   #register array to handle restoring page state on back button click 

   //delete old files
   $t = time();
   $f = opendir("data/");
   while ($temp = readdir($f)) {
      if (substr($temp, 0, 1) == 'i' and substr($temp,-4) == '.txt') {
        $fstat = stat("data/" . $temp);
        if ($fstat['mtime'] < $t-3600) unlink("data/" . $temp);
      }
      if (substr($temp, 0, 1) == 'i' and substr($temp,-4) == '.irr') {
        $fstat = stat("data/" . $temp);
        if ($fstat['mtime'] < $t-3600) unlink("data/" . $temp);
      }
      if (substr($temp, 0, 1) == 'i' and substr($temp,-4) == '.wth') {
        $fstat = stat("data/" . $temp);
        if ($fstat['mtime'] < $t-3600) unlink("data/" . $temp);
      }
      if (substr($temp, 0, 1) == 'i' and substr($temp,-4) == '.csv') {
        $fstat = stat("data/" . $temp);
        if ($fstat['mtime'] < $t-3600) unlink("data/" . $temp);
      }
      if (substr($temp, 0, 3) == 'run' and substr($temp,-4) == '.txt') {
        $fstat = stat("data/" . $temp);
        if ($fstat['mtime'] < $t-3600) unlink("data/" . $temp);
      }
   }

   function dateFromDoy($x, $year=2009) {
      $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");
      $days = array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
      if ($year % 4 == 0) {
	$days[1] = 29;
	if ($x > 366) {
	  $x-=366;
	}
      } else {
        while ($x > 365) {
          $x-=365;
        }
      }
      $n = 0;
      while ($x > $days[$n]) {
        $x-=$days[$n];
        $n++;
      }
      $s = $months[$n] . ' ' . $x;
      return $s;
   }

   function getWthInfo($wfile) {
      $fp = fopen($wfile, 'r');
      for ($j = 0; $j < 4; $j++) {
	$x = fgets($fp,1024);
      }
      $temp = preg_split('/\s+/', trim($x));
      $info = array();
      $info["lat"] = $temp[1];
      $info["long"] = $temp[2];
      $info["elev"] = $temp[3];
      $x = fgets($fp,1024);
      $x = fgets($fp,1024);
      $year = (int)substr($x, 0, 4);
      $doy = (int)substr($x, 4, 3);
      $info["start"] = dateFromDoy($doy, $year) . ", " . $year;
      fclose($fp);
      return $info;
   }

   function getWthEndDate($wfile) {
      $x = getLastLine($wfile);
      $year = (int)substr($x, 0, 4);
      $doy = (int)substr($x, 4, 3);
      return dateFromDoy($doy, $year) . ", " . $year;
   }

?>
<table align="center" cellspacing=0 cellpadding=0 class="maintable">
   <tr class="topbordertr"> 
   <td colspan=2>
      <table class="topborder" cellspacing=0 cellpadding=0>
      <tr>
      <td width=120 valign="top"><a href="http://www.ifas.ufl.edu"><img src="images/UF_IFAS_120.png" width=120 border=0 alt="IFAS logo"></a></td>
      <script language="javascript">
	//if (hires) {
	  document.writeln('<td><img id="header" src="images/top_banner_bmp_hires.png" alt="bmp header image" border=0></td>');
	//} else {
          //document.writeln('<td><img id="header" src="images/top_banner_bmp_lores.png" alt="bmp header image" border=0></td>');
	//}
      </script>
      </tr>
      </table>
   </td>
   </tr>
   <tr>
   <td valign="top" style="position:relative;">
	<!-- bgcolor for IE bug workaround - fuck you Bill Gates! -->
      <table class="mainbody" cellspacing=0 cellpadding=0 bgcolor="#9dadfd"> 
      <tr>
      <td align="left" valign="top" class="leftbordertd" rowspan=3 height=100%>
	<table border=0 cellspacing=0 cellpadding=0> 
	<tr>
	<td valign="top" align="left"><a href="index.php" onMouseOver="on('home');" onMouseOut="off('home');"><img src="images/button-off-home.png" border=0 name="home" alt="Home"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="calculator.php" onMouseOver="on('calculator');" onMouseOut="off('calculator');" title="Calculator"><img src="images/button-off-calculator.png" border=0 name="calculator" alt="Calculator"></a></td>
        </tr>
        <tr>
        <td valign="top" align="left"><a href="moreinfo.php" onMouseOver="on('moreinfo');" onMouseOut="off('moreinfo');" title="More Info"><img src="images/button-off-moreinfo.png" border=0 name="moreinfo" alt="More Info"></a></td>
        </tr>
<!--
	<tr>
	<td valign="top" align="left"><a href="driver_grower.php" onMouseOver="on('grower');" onMouseOut="off('grower');" title="Run one set of inputs"><img src="images/button-off-grower.png" border=0 name="grower" alt="Grower"></a></td>
	</tr>
	<tr>
	<td valign="top" align="left"><a href="comparisons.php" onMouseOver="on('comp');" onMouseOut="off('comp');" title="Run your own experiments"><img src="images/button-off-comp.png" border=0 name="comp" alt="Comparisons"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="realtime.php" onMouseOver="on('realtime');" onMouseOut="off('realtime');" title="Get realtime recommendations"><img src="images/button-off-realtime.png" border=0 name="realtime" alt="Real Time"></a></td>
        </tr>
	<tr>
	<td valign="top" align="left"><a href="driver_all.php" onMouseOver="on('technical');" onMouseOut="off('technical');"><img src="images/button-off-technical.png" border=0 name="technical" alt="Technical"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="source/UserManual.pdf" target="ccmt" onMouseOver="on('usermanual');" onMouseOut="off('usermanual');" onClick="off('usermanual');"><img src="images/button-off-usermanual.png" border=0 name="usermanual" alt="User Manual"></a></td>
        </tr>
        <tr>
        <td valign="top" align="left"><a href="source/sourcecode.php" onMouseOver="on('sourcecode');" onMouseOut="off('sourcecode');"><img src="images/button-off-sourcecode.png" border=0 name="sourcecode" alt="Source Code"></a></td>
        </tr>
        <tr>
        <td valign="top" align="left"><a href="capturefactor/" target="ccmt" onMouseOver="on('cf');" onMouseOut="off('cf');" onClick="off('cf');"><img src="images/button-off-cf.png" border=0 name="cf" alt="Capture Factor"></a></td>
        </tr>
	<tr>
	<td valign="top" align="left"><a href="links.php" onMouseOver="on('links');" onMouseOut="off('links');"><img src="images/button-off-links.png" border=0 name="links" alt="Links"></a></td>
	</tr>
	<tr>
        <td valign="top" align="left"><a href="contact.php" onMouseOver="on('contact');" onMouseOut="off('contact');"><img src="images/button-off-contact.png" border=0 name="contact" alt="Contact Us"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="credits.php" onMouseOver="on('credits');" onMouseOut="off('credits');"><img src="images/button-off-credits.png" border=0 name="credits" alt="Credits"></a></td>
        </tr>
-->
	</table>
      </td>
      </tr>
      <!-- menubar -->
      <tr height=10 bgcolor="#9dadfd"><td bgcolor="#9dadfd"></td>
      <td valign="top" bgcolor="#9dadfd" align="center" valign="center" class="small">
        <table border=0 cellpadding=0 cellspacing=0 height=10 width=95% style="margin-left: 10px; margin-right: 5px;">
        <tr height=10>
	<td class="menubar">
<?php
	if ($isLoggedIn) {
	  echo 'Logged in as ' . $bmpusername; 
	}
?>
	</td>
        <td class="small" width=25% height=10 align="right">
<?php if ($isLoggedIn) { ?>
           <a href="myaccount.php" class="menubarlink">My Account</a>
           |
           <a href="logout.php" class="menubarlink">Logout</a>
<?php } else { ?>
           <a href="login.php" class="menubarlink">Login</a>
           |
           <a href="register/register.php" class="menubarlink">Register</a>
<?php } ?>
        </td>
        </tr>
        </table>
      </td>
      </tr>
      <!-- main page -->
      <tr height=800><td></td>
      <td valign="top" align="left" class="maintd">
	<!-- calendar -->
        <div id="calDiv" style="position:absolute;visibility:hidden;background-color:white;"></div>
	<!-- Start body here -->
<?php if ($doPreview) echo '<center><span style="color: red;">This is a preview mode of CIRRIG that allows you to view but not edit zones and weather stations.</span></center>';?>
