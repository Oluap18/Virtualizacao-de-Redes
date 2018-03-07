<?php
#Get the ipaddress
$iter = 0;
$found = false;
$last_line = exec('ifconfig', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta
  $comp = substr($row, 0, 4);
  if(strcmp($comp, "eth0") == 0){
    $found0 = true;
  }
  if($found0 == true){
    $iter++;
  }
  if($iter == 2){
    $hostDB = substr($row, 20, 9)."2";
    break;
  }
}
$conn_string = "host=$hostDB;port=5432;dbname=vr;user=vr;password=vr";
try{
  $conn = new PDO("pgsql:".$conn_string);
}catch (PDOException $e){
  // report error message
  echo $e->getMessage();
}

$getToken = 'SELECT tokenid FROM vars;';
$updateToken = 'UPDATE vars SET tokenid = tokenid + 1;';
$checkUser = 'SELECT * FROM users WHERE username = \''.$_POST['username'].'\';';

$r = $conn->query($checkUser);
if($r->rowCount() !== 0){
  foreach ($r as $row) {
    $password = $row['password'];
    if(strcmp($password, $_POST['password'])==0){
      $res = $conn->query($getToken);
      foreach ($res as $rowTok) {
        $token = $rowTok['tokenid'];
      }
      $createToken = 'INSERT INTO tokens (tokenid, userid) VALUES ('.$token.','.$row['userid'].');';
      $res = $conn->query($createToken);
      if($res !== false){
        $res = $conn->query($updateToken);
        header("Location: http://localhost:9000/mail?token=".$token);
        echo "Token a ser utilizador no mail: $token\n";
      }
      else{
        $updateUser = 'UPDATE tokens SET tokenid = '.$token.' WHERE userid = '.$row['userid'].';';
        $res = $conn->query($updateUser);
        $res = $conn->query($updateToken);
        header("Location: http://localhost:9000/mail?token=".$token);
        echo "Token a ser utilizador no mail: $token\n";
      }
    }
    else{
      echo "Password errada\n";
    }
  }
}
else{
  echo "Não existe utilizador\n";
}

?>


<html>
  <head>
    <meta charset="UTF-8">
    <title>Login Token</title>
    <link rel="stylesheet" href="http://172.52.0.2/css/reset.css">
    <link rel="stylesheet" href="http://172.52.0.2/css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
    </div>
  </body>
</html>
