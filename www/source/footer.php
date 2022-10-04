        <!-- End body here -->
      </td>
      </tr>
      </table>
   </td>
   </tr>
   <tr>
   <td valign="top">
      <table class="topborder" cellspacing=0 cellpadding=0>
      <tr class="bottombordertr">
      <td width=140></td>
      <td align="center">
	<a href="../index.php" class="footer">Home</a> |
        <a href="../model.php" class="footer">Model Concepts</a> |
	<a href="../driver_grower.php" class="footer">Grower</a> |
	<a href="../comparison_fert.php" class="footer">Comparisons</a> |
        <a href="../realtime.php" class="footer">Real Time</a> |
	<a href="../driver_all.php" class="footer">Technical</a> |
        <a href="../links.php" class="footer">Links</a> |
        <a href="../contact.php" class="footer">Contact Us</a> |
        <a href="../credits.php" class="footer">Credits</a>
        <br>
        <span class="small">
        &#169; Copyright 2009 <a href="http://www.ufl.edu">University of Florida</a>/<a href="http://www.ifas.ufl.edu">Institute of Food and Agricultural Sciences</a>. All rights reserved.
        </span>
      </td>
      <td width=140>
	<a href="http://www.ufl.edu">
        <img src="images/uf_logo.gif" align="right" border=0 alt="UF logo"></a>
      </td>
      </tr>
      </table>
   </td>
   </tr>
</table>
<?php
  function getBrowser($useragent) {
    if (preg_match("/opera/i", $useragent)) {
      return "Opera";
    } else if (preg_match("/galeon/i", $useragent)) {
      return "Galeon";
    } else if (preg_match("/chrome/i", $useragent)) {
      return "Chrome";
    } else if (preg_match("/safari/i", $useragent)) {
      return "Safari";
    } else if (preg_match("/konqueror/i", $useragent)) {
      return "Konqueror";
    } else if (preg_match("/MSIE 5.0/i", $useragent)) {
      return "IE 5.0";
    } else if (preg_match("/MSIE 5.5/i", $useragent)) {
      return "IE 5.5";
    } else if (preg_match("/MSIE 6.0/i", $useragent)) {
      return "IE 6.0";
    } else if (preg_match("/MSIE 7.0/i", $useragent)) {
      return "IE 7.0";
    } else if (preg_match("/MSIE 8.0/i", $useragent)) {
      return "IE 8.0";
    } else if (preg_match("/MSIE/i", $useragent)) {
      return "IE";
    } else if (preg_match("/firefox/i", $useragent)) {
      return "Firefox";
    } else if (preg_match("/flock/i", $useragent)) {
      return "Flock";
    } else if (preg_match("/seamonkey/i", $useragent)) {
      return "Seamonkey";
    } else if (preg_match("/netscape/i", $useragent)) {
      return "Netscape";
    } else if (preg_match("/mozilla/4/i", $useragent)) {
      return "Netscape";
    } else if (preg_match("/gecko/i", $useragent)) {
      return "Mozilla";
    } else if (preg_match("/mozilla/i", $useragent)) {
      return "Mozilla";
    }
    return "Other";
  }

  function getOS($useragent) {
    $OSList = array(
      'Windows 3.11' => 'Win16',
      'Windows 95' => '(Windows 95)|(Win95)|(Windows_95)',
      'Windows 98' => '(Windows 98)|(Win98)',
      'Windows ME' => '(Windows ME)|(Win 9x 4.9)',
      'Windows 2000' => '(Windows NT 5.0)|(Windows 2000)',
      'Windows XP' => '(Windows NT 5.1)|(Windows XP)',
      'Windows Server 2003' => '(Windows NT 5.2)',
      'Windows Vista' => '(Windows NT 6.0)',
      'Windows 7' => '(Windows NT 6.1)|(Windows NT 7)',
      'Windows NT 4' => '(Windows NT 4.0)|(WinNT4.0)|(WinNT)|(Windows NT)',
      'Open BSD' => 'OpenBSD',
      'Sun OS' => 'SunOS',
      'Linux' => '(Linux)|(X11)',
      'Mac OS' => '(Mac_PowerPC)|(Macintosh)|(Mac)',
      'QNX' => 'QNX',
      'BeOS' => 'BeOS',
      'Amiga' => 'Amiga',
      'OS/2' => 'OS/2',
      'Search Bot'=>'(nuhk)|(Googlebot)|(Yammybot)|(Openbot)|(Slurp)|(MSNBot)|(Ask Jeeves/Teoma)|(ia_archiver)'
    );
       
    // Loop through the array of user agents and matching operating systems
    foreach($OSList as $CurrOS=>$Match) {
      // Find a match
      if (preg_match("/{$Match}/i", $useragent)) {
      #if (eregi($Match, $useragent)) {
	return $CurrOS;
      }
    }
    return "Other";
  }
 
  $screenres = "unknown";
  if (isset($_SESSION['res']) && $_SESSION['res'] != "unknown") {
    $screenres = $_SESSION['res'];
  } else {
    if (!isset($_COOKIE["screenres"])) {
      echo '<script language="javascript">writeCookie();</script>';
    } 
    if (isset($_COOKIE["screenres"])) {
      $screenres = $_COOKIE["screenres"];
    } else if (isset($_GET['res'])) {
      $screenres = $_GET['res'];
    } else {
      echo '<script language="javascript">';
      echo 'window.location.href="' . $_SERVER['PHP_SELF'] . '?res=" + screen.width + "x" + screen.height;';
      echo '</script>'; 
    }
    if (!isset($_SESSION['res'])) session_register('res');
    $_SESSION['res'] = $screenres;
  }

  $ip = $_SERVER['REMOTE_ADDR'];
  if (isset($_SERVER['HTTP_REFERER'])) {
    $ref = str_replace("http://", "", $_SERVER['HTTP_REFERER']);
  } else $ref = "";
  $ref = substr($ref, 0, strpos($ref, "/"));
  $page = $_SERVER['PHP_SELF'];
  $page = substr($page, strrpos($page, '/')+1);
  $useragent = $_SERVER['HTTP_USER_AGENT'];
  $os = getOS($useragent);
  $browser = getBrowser($useragent);
  if (!$isLoggedIn) {
    $bmpusername = "Not Logged In";
  }

  $addHit = true;
  if ($page == "tracker.php" && isset($_GET['cat'])) $addHit = false;

  if ($addHit) {
    $sql = "insert into tracker (time, res, browser, os, ip, referrer, page, username) ";
    $sql .= "values(NOW(), '" . $screenres . "', '" . $browser . "', '";
    $sql .= $os . "', '" . $ip . "', '" . $ref . "', '" . $page . "', '" . $bmpusername . "')";
    $out = $mysqli_db->query($sql);

    $nupdate = 0;
    $sql = "select count(*) from tracker where ip='" . $ip . "'";
    $sql .= " AND res='unknown' AND DATE(time) = DATE(NOW())";
    $out = $mysqli_db->query($sql);
    if ($out != false) {
      if ($row = $out->fetch_array(MYSQLI_BOTH)) {
        $nupdate = $row[0];
      }
    }
    if ($nupdate > 0) {
      $sql = "update tracker set res='" . $screenres . "' where ip='" . $ip;
      $sql .= "' AND res='unknown' AND DATE(time) = DATE(NOW())";
      $out = $mysqli_db->query($sql);
    }
  }
?>
</body>
</html>
