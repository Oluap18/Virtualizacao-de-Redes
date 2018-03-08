<?php
$conn_string = "host=db;port=5432;dbname=vr;user=vr;password=vr";
try{
  $conn = new PDO("pgsql:".$conn_string);
}catch (PDOException $e){
  // report error message
  echo $e->getMessage();
}

$checkToken = 'SELECT userid FROM tokens WHERE tokenid = '.$_GET['token'].';';
$r = $conn->query($checkToken);
if($r!== false){
  if($r->rowCount() !== 0){
  ?>
    <form action="http://localhost:9000/mail/mail.php" method="POST">
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
