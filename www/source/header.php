<link rel=stylesheet href="../driver.css" type="text/css">
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
<script language="javascript">
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

   if (document.images) {
      var fawnmap = new Image();
      fawnmap.src = "../images/fawnmap2.png";
      var wthmap = new Image();
      wthmap.src = "../images/Weather_locations.png";
      var userwth = new Image();
      userwth.src = "../images/userwth.png";

      var homeButton = new Image();
      homeButton.src = "../images/button-off-home.png";
      var homeButtonOn = new Image();
      homeButtonOn.src = "../images/button-on-home.png";
      var modelButton = new Image();
      modelButton.src = "../images/button-off-model.png";
      var modelButtonOn = new Image();
      modelButtonOn.src = "../images/button-on-model.png";
      var growerButton = new Image();
      growerButton.src = "../images/button-off-grower.png";
      var growerButtonOn = new Image();
      growerButtonOn.src = "../images/button-on-grower.png";
      var compButton = new Image();
      compButton.src = "../images/button-off-comp.png";
      var compButtonOn = new Image();
      compButtonOn.src = "../images/button-on-comp.png";
      var realtimeButton = new Image();
      realtimeButton.src = "../images/button-off-realtime.png";
      var realtimeButtonOn = new Image();
      realtimeButtonOn.src = "../images/button-on-realtime.png";
      var technicalButton = new Image();
      technicalButton.src = "../images/button-off-technical.png";
      var technicalButtonOn = new Image();
      technicalButtonOn.src = "../images/button-on-technical.png";
      var usermanualButton = new Image();
      usermanualButton.src = "../images/button-off-usermanual.png";
      var usermanualButtonOn = new Image();
      usermanualButtonOn.src = "../images/button-on-usermanual.png";
      var sourcecodeButton = new Image();
      sourcecodeButton.src = "../images/button-off-sourcecode.png";
      var sourcecodeButtonOn = new Image();
      sourcecodeButtonOn.src = "../images/button-on-sourcecode.png";
      var linksButton = new Image();
      linksButton.src = "../images/button-off-links.png";
      var linksButtonOn = new Image();
      linksButtonOn.src = "../images/button-on-links.png";
      var contactButton = new Image();
      contactButton.src = "../images/button-off-contact.png";
      var contactButtonOn = new Image();
      contactButtonOn.src = "../images/button-on-contact.png";
      var creditsButton = new Image();
      creditsButton.src = "../images/button-off-credits.png";
      var creditsButtonOn = new Image();
      creditsButtonOn.src = "../images/button-on-credits.png";
   }

   function on(imgName) {
      if (document.images)
        document[imgName].src = eval(imgName + 'ButtonOn.src');
   }

   function off(imgName) {
      document[imgName].src = eval(imgName + 'Button.src');
   }

  function dispHandle(name, show) {
    var obj = document.getElementById(name);
    if (show == true) obj.style.display = ""; else obj.style.display = "none";
  }

  function popupText(text) { 
    var generator=window.open('','name','height=250,width=450');
    generator.document.write('<html><head><title>More info</title>');
    generator.document.write('</head><body>');
    generator.document.write(text);
    generator.document.write('</body></html>');
    generator.document.close();
  }

  function changeImages(imgName, path) {
    if (document.images) document[imgName].src = path;
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
   require('../stats.php');
   $pagename = "index";
   $pfix = "data/i";

   $isIE = false;
   $useragent = $_SERVER['HTTP_USER_AGENT'];
   if (preg_match("/MSIE/i", $useragent)) $isIE = true;
   #if (eregi("MSIE", $useragent)) $isIE = true; 
   echo '<script language="javascript">';
   if ($isIE) echo 'var isIE = true;'; else echo 'var isIE=false;';
   echo '</script>';

   $months = array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec");

   function stripQuotes($string) {
      return str_replace('"', '\"', str_replace("'","\'",$string));
   }

   function showOutputButtons() {
      echo '<center><table border=0 align="center"><tr>';
      echo '<td><a href="javascript:history.go(-1);"><img src="../images/back-off.png" border=0 onMouseOver=\'this.src="../images/back-on.png";\' onMouseOut=\'this.src="../images/back-off.png";\'></a></td>';
      echo '<td width=20></td>';
      echo '<td><a href="javascript:print();"><img src="../images/print-off.png" border=0 onMouseOver=\'this.src="../images/print-on.png";\' onMouseOut=\'this.src="../images/print-off.png";\'></a></td>';
      echo '<td width=20></td>';
      echo '<td><a href="javascript:showReport();"><img src="../images/report-off.png" border=0 onMouseOver=\'this.src="../images/report-on.png";\' onMouseOut=\'this.src="../images/report-off.png";\'></a></td>';
      echo '</tr></table><br></center>';
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

   require('../dbcnx.php');
   #mysql_select_db("mybmp");

   //check if user is logged in
   $isLoggedIn = false;

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
   }

   $plotWidth = 320;
   $plotHeight = 240;
   $hires = false;
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
   if (!isset($_SESSION['pw'])) {
     session_register('pw');
     session_register('ph');
     session_register('bg');
     session_register('fg');
   }
   $_SESSION['pw'] = $plotWidth;
   $_SESSION['ph'] = $plotHeight;
   $_SESSION['bg'] = str_replace("%23","#",$plotbg);
   $_SESSION['fg'] = str_replace("%23","#",$plotfg);

   #register array to handle restoring page state on back button click 
   if (!isset($_SESSION['postBack'])) session_register('postBack');

   //delete old files
   $t = time();

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

?>
<table align="center" cellspacing=0 cellpadding=0 class="maintable">
   <tr class="topbordertr"> 
   <td colspan=2>
      <table class="topborder" cellspacing=0 cellpadding=0>
      <tr>
      <td width=120 valign="top"><a href="http://www.ifas.ufl.edu"><img src="../images/UF_IFAS_120.png" width=120 border=0 alt="IFAS logo"></a></td>
      <script language="javascript">
	if (hires) {
	  document.writeln('<td><img id="header" src="../images/top_banner_bmp_hires.png" alt="bmp header image" border=0></td>');
	} else {
          document.writeln('<td><img id="header" src="../images/top_banner_bmp_lores.png" alt="bmp header image" border=0></td>');
	}
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
	<td valign="top" align="left"><a href="../index.php" onMouseOver="on('home');" onMouseOut="off('home');"><img src="../images/button-off-home.png" border=0 name="home" alt="Home"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="../model.php" onMouseOver="on('model');" onMouseOut="off('model');" title="Model Concepts"><img src="../images/button-off-model.png" border=0 name="model" alt="Model Concepts"></a></td>
        </tr>
	<tr>
	<td valign="top" align="left"><a href="../driver_grower.php" onMouseOver="on('grower');" onMouseOut="off('grower');" title="Run one set of inputs"><img src="../images/button-off-grower.png" border=0 name="grower" alt="Grower"></a></td>
	</tr>
	<tr>
	<td valign="top" align="left"><a href="../comparisons.php" onMouseOver="on('comp');" onMouseOut="off('comp');" title="Run your own experiments"><img src="../images/button-off-comp.png" border=0 name="comp" alt="Comparisons"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="../realtime.php" onMouseOver="on('realtime');" onMouseOut="off('realtime');" title="Get realtime recommendations"><img src="../images/button-off-realtime.png" border=0 name="realtime" alt="Real Time"></a></td>
        </tr>
	<tr>
	<td valign="top" align="left"><a href="../driver_all.php" onMouseOver="on('technical');" onMouseOut="off('technical');"><img src="../images/button-off-technical.png" border=0 name="technical" alt="Technical"></a></td>
	</tr>
<!--
        <tr>
        <td valign="top" align="left"><a href="../UserManual.pdf" target="ccmt" onMouseOver="on('usermanual');" onMouseOut="off('usermanual');" onClick="off('usermanual');"><img src="../images/button-off-usermanual.png" border=0 name="usermanual" alt="User Manual"></a></td>
        </tr>
-->
        <tr>
        <td valign="top" align="left"><a href="sourcecode.php" onMouseOver="on('sourcecode');" onMouseOut="off('sourcecode');"><img src="../images/button-off-sourcecode.png" border=0 name="sourcecode" alt="Source Code"></a></td>
        </tr>
	<tr>
	<td valign="top" align="left"><a href="../links.php" onMouseOver="on('links');" onMouseOut="off('links');"><img src="../images/button-off-links.png" border=0 name="links" alt="Links"></a></td>
	</tr>
	<tr>
        <td valign="top" align="left"><a href="../contact.php" onMouseOver="on('contact');" onMouseOut="off('contact');"><img src="../images/button-off-contact.png" border=0 name="contact" alt="Contact Us"></a></td>
	</tr>
        <tr>
        <td valign="top" align="left"><a href="../credits.php" onMouseOver="on('credits');" onMouseOut="off('credits');"><img src="../images/button-off-credits.png" border=0 name="credits" alt="Credits"></a></td>
        </tr>
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
           <a href="../myaccount.php" class="menubarlink">My Account</a>
           |
           <a href="../logout.php" class="menubarlink">Logout</a>
<?php } else { ?>
           <a href="../login.php" class="menubarlink">Login</a>
           |
           <a href="../register.php" class="menubarlink">Register</a>
<?php } ?>
        </td>
        </tr>
        </table>
      </td>
      </tr>
      <!-- main page -->
      <tr height=800><td></td>
      <td valign="top" align="left" class="maintd">
	<!-- Start body here -->
