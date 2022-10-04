<?php
phpinfo();
$gd = gd_info();
foreach ($gd as $key=>$value) {
   echo $key . ' ' . $value . '<br>';
}
?>
