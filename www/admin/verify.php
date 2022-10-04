    <tr><td align="right" class="regreq" valign="top">Verification:<br></td>
    <td>
      <?php
        $words = array("first", "second", "third", "fourth", "fifth");
        $nums = array();
        for ($j = 0; $j < 5; $j++) {
          $nums[$j] = mt_rand(0, 99);
        }
        $i = mt_rand(0, 4);
        echo '<input type="hidden" name="vnum" value="' . $nums[$i] . '">';
      ?>
      <input type="text" size=3 name="verify" maxlength=2 onkeypress="return validNum(event);">
      <br>
      <span class="small">
For security purposes, five numbers between 0 and 100 will be listed below,
separated by spaces.<br>
Enter the <?php echo $words[$i]; ?> number in the sequence into the
Verification box above.<br>
      <?php for ($j = 0; $j < 5; $j++) echo $nums[$j] . ' &nbsp;&nbsp;'; ?>
      </span>
    </td>
    </tr>
