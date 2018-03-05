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
    if(strcmp(substr($row, 29, 1),"2")==0){
      $host = substr($row, 20, 9)."4";
    }
    else{
      $host = substr($row, 20, 9)."2";
    }
    break;
  }
}

$token = 'http://'.$host.'/checkToken.php';
?>

<html>
  <head>
    <meta charset="UTF-8">
    <title>Mail Service</title>
    <link rel="stylesheet" href="css/reset.css">
    <link rel="stylesheet" href="css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
      <div class="avatar">
        <img src="https://digitalnomadsforum.com/styles/FLATBOOTS/theme/images/user4.png">
      </div>
      <br>
      <form action="<?php echo $token; ?>" method="POST">
        <input type="text" required name="token" placeholder="Token">
        <br>
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
