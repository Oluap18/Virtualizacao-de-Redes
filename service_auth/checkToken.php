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
  if($found0 == true || $found1 == true){
    $iter++;
  }
  if($iter == 2){
    if(strcmp(substr($row, 29, 1),"3")==0){
      if($found0 == true){
        $hostDB = substr($row, 20, 9)."2";
        $found0 = false;
      }
      else{
        $hostPHP = substr($row, 20, 9)."2";
        $found1 = false;
      }
      $iter = 0;
    }
    else{
      if($found0 == true){
        $hostDB = substr($row, 20, 9)."3";
        $found0 = false;
      }
      else{
        $hostPHP = substr($row, 20, 9)."3";
        $found1 = false;
      }
      $iter = 0;
    }
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
if($r->rowCount() !== 0){
?>
  <script>document.getElementById("click-me " ) .click()</script>
  <form action="<?php echo $sendMail; ?>" method="POST">
    <input type="text" required name="token" placeholder="Token" #Colocar como value $_POST['token'] se necessário>
    <br>
    <input type="email" required name="emailto" placeholder="Receivers e-mail" #Colocar como value $_POST['emailto'] se necessário>
    <br>
    <input type="text" required name="subject" placeholder="Subject" #Colocar como value $_POST['subject'] se necessário>
    <br>
    <textarea class="msgtext" required name="message" placeholder="Type your message here" #Colocar como value $_POST['message'] se necessário></textarea>
    <br><br>
    <button id="click-me">Send</button>
  </form>
<?php
}
else{
  #Inserir uma mensagem de error qualquer
  echo "Token inválido\n";
}
?>
