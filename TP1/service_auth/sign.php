  <!DOCTYPE html>
<html>

<?php
$conn_string = "host=db;port=5432;dbname=vr;user=vr;password=vr";
try{
  $conn = new PDO("pgsql:".$conn_string);
}catch (PDOException $e){
  // report error message
  echo $e->getMessage();
}

$getUser = 'SELECT userid FROM vars;';
$updateUser = 'UPDATE vars SET userid = userid + 1;';

foreach ($conn->query($getUser) as $row) {
  $user = $row['userid'];
}
$insertUser = 'INSERT INTO users (userid, username, password) VALUES ('.$user.',\''.$_POST['username'].'\',\''.$_POST['password'].'\');';
$r = $conn->query($insertUser);
if($r !== false){
  $r = $conn->query($updateUser);
  echo "Registado com sucesso\n";
}
else{
  echo "Username de utilizador já existente\n";
}

$conn = null;

?>

<html>
  <head>
    <meta charset="UTF-8">
    <title>Register Service</title>
    <link rel="stylesheet" href="/aut/css/reset.css">
    <link rel="stylesheet" href="/aut/css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
    </div>
  </body>
</html>
