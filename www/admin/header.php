<link rel=stylesheet href="../driver.css" type="text/css">
<script language="javascript">
  function validNum(e) {
    var k;
    document.all ? k = e.keyCode : k = e.which;
    //numbers
    if (k > 47 && k < 58) return true;
    // backspace cursor,delete
    if (k == 8 || k == 0) return true;
    return false;
  }

  function validDec(e) {
    var k;
    document.all ? k = e.keyCode : k = e.which;
    //numbers
    if (k > 47 && k < 58) return true;
    // backspace cursor,delete
    if (k == 46 || k == 8 || k == 0) return true;
    return false;
  }

  function unselect(selForm) {
    if (selForm.options[0].selected) {
      for (j = 1; j < selForm.options.length; j++) {
        selForm.options[j].selected = false;
      }
    }
  }


  function imposeMaxLength(Event, Object, MaxLen) {
    return (Object.value.length <= MaxLen)||(Event.keyCode == 8 ||Event.keyCode==46||(Event.keyCode>=35&&Event.keyCode<=40))
  }
</script>
<?php
  $msie='/msie\s(5\.[5-9]|[6]\.[0-9]*).*(win)/i';

   $affiliations = array("Grower", "University or County Extension", "Allied Industry", "Research", "Education - Teacher", "Education - Student", "Government - Regulatory", "Government - Nonregulatory", "Non-profit Organization", "Other (specify)");

  function stripQuotes($string) {
    return str_replace('"', '\"', str_replace("'","\'",$string));
  }

  require('../dbcnx.php');
  mysql_select_db("mybmp");

?>
<body>
