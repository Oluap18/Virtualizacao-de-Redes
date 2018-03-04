<!DOCTYPE html>

<?php
#Get the ipaddress
$iter = 0;
$last_line = exec('ip addr show', $full_output);
foreach($full_output as $row){
  if($iter == 10){
    $host = substr($row, 9, 9).'3';
    break;
  }
  $iter++;
}

$token = 'http://'.$host.'/checkToken.php';
echo $login;
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
