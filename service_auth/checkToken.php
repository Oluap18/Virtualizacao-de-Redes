<?php
#Get the ipaddress
$iter = 0;
$found0 = false;
$found1 = false;
$last_line = exec('ifconfig', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta
  $comp = substr($row, 0, 4);
  if(strcmp($comp, "eth0") == 0){
    $found0 = true;
  }
  if(strcmp($comp, "eth1") == 0){
    $found1 = true;
  }
  if($found0 == true || $found1 == true){
    $iter++;
  }
  if($iter == 2){
    if($found0 == true){
      $hostDB = substr($row, 20, 9)."2";
      $found0 = false;
    }
    else{
      $hostPHP = substr($row, 20, 9)."3";
      $found1 = false;
    }
    $iter = 0;
  }
}

$sendMail = "http://".$hostPHP."/mail.php";
$conn_string = "host=$hostDB;port=5432;dbname=vr;user=vr;password=vr";
try{
  $conn = new PDO("pgsql:".$conn_string);
}catch (PDOException $e){
  // report error message
  echo $e->getMessage();
}

$checkToken = 'SELECT userid FROM tokens WHERE tokenid = '.$_POST['token'].';';
$r = $conn->query($checkToken);
if($r!== false){
  if($r->rowCount() !== 0){
  ?>
    <form action="<?php echo $sendMail; ?>" method="POST" target="_self">
      <input type="email" required name="emailto" placeholder="Receivers e-mail" value = <?php echo $_POST['emailto']; ?>>
      <br>
      <input type="text" required name="subject" placeholder="Subject" value = <?php echo $_POST['subject']; ?>>
      <br>
      <textarea class="msgtext" required name="message" placeholder="Type your message here"><?php echo $_POST['message']; ?></textarea>
      <br><br>
      <button id="click-me">Send</button>
    </form>
    <script>document.getElementById("click-me").click();</script>
  <?php
  }
  else{
    #Inserir uma mensagem de error qualquer
    echo "Token inválido\n";
  }
}
else{
  echo "Can't communicate with database in $hostDB\n";
}
?>
