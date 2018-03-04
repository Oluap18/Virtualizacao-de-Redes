<?php
#Get the ipaddress
$iter = 0;
$found0 = false;
$found1 = false;
$last_line = exec('ip addr show', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta, e guardar os ips
  $comp = substr($row, 4, 4);
  if(strcmp($comp, "eth0") == 0){
    $found0 = true;
    $found1 = false;
    $iter = 0;
  }
  if(strcmp($comp, "eth1") == 0){
    $found1 = true;
    $found0 = false;
    $iter = 0;
  }
  if($found0 == true || $found1 == true){
    $iter++;
  }
  if($iter == 3 && $found1 == false){
    $hostDB = substr($row, 9, 9)."2";
    break;
  }
  if($iter == 3 && $found0 == false){
    $hostPHP = substr($row, 9, 9)."2";
    break;
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

  #Fazer isto com javascript de maneira a que não seja necessário carregar no botão para
  #Fazer submite, de modo a executar isto automáticamente
  <form action="<?php echo $sendMail; ?>" method="POST">
    <input type="text" required name="token" placeholder="Token" #Colocar como value $_POST['token'] se necessário>
    <br>
    <input type="email" required name="emailto" placeholder="Receivers e-mail" #Colocar como value $_POST['emailto'] se necessário>
    <br>
    <input type="text" required name="subject" placeholder="Subject" #Colocar como value $_POST['subject'] se necessário>
    <br>
    <textarea class="msgtext" required name="message" placeholder="Type your message here" #Colocar como value $_POST['message'] se necessário></textarea>
    <br><br>
    <button>Send</button>
  </form>
}
