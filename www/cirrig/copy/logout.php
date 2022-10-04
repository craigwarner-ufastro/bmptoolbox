<?php session_start(); ?>
<html>
<head>
<title>BMP Toolbox</title>
<?php
   if (isset($_SESSION['bmpid'])) {
      unset($_SESSION['bmpid']);
   }
   session_destroy();
?>
<?php require('header.php'); ?>
	<!-- Start body here -->

   <center>You have been successfully logged out.</center>


	<!-- End body here -->
<?php require('footer.php'); ?>
