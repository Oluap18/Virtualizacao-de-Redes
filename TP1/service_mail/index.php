<!DOCTYPE html>

<?php
#Get the ipaddress
$iter = 0;
$found = false;
$last_line = exec('ifconfig', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta
  $comp = substr($row, 0, 4);
  if(strcmp($comp, "eth0") == 0){
    $found = true;
  }
  if($found == true){
    $iter++;
  }
  if($iter == 2){
      $host = substr($row, 20, 9)."2";
    break;
  }
}
$token_received = $_GET['token'];
$token = 'aut/checkToken.php?token='.$token_received;
if ($token_received == NULL) {
	echo "You need to login first!";
}
?>

<html>
  <head>
    <meta charset="UTF-8">
    <title>Mail Service</title>
    <link rel="stylesheet" href="mail/css/reset.css">
    <link rel="stylesheet" href="mail/css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
      <div class="avatar">
        <img src="https://digitalnomadsforum.com/styles/FLATBOOTS/theme/images/user4.png">
      </div>
      <br>
      <form action="<?php echo $token; ?>" method="POST">
        <input type="email" required name="emailto" placeholder="Receivers e-mail">
        <br>
        <input type="text" required name="subject" placeholder="Subject">
        <br>
        <textarea class="msgtext" required name="message" placeholder="Type your message here"></textarea>
        <br><br>
        <button>Send</button>
      </form>
    </div>
  </body>

</html>
