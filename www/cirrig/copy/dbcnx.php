<?php
   $mysqli_db = mysqli_connect("localhost", "cwarner", "", "mybmp");
   if (!$mysqli_db) {
     die("Could not connect: " . mysqli_connect_errno()); 
   }
   #$dbcnx = mysql_connect("localhost", "cwarner", "")
   #   or die ("Could not connect: " . mysql_error());
?>
