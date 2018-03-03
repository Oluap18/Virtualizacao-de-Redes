<?php
$conn_string = "host=192.168.1.7;port=5432;dbname=vr;user=vr;password=vr";
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
        echo $token."\n";
      }
      else{
        echo "Já efetuou o login\n";
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
