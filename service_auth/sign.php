<!DOCTYPE html>
<html>

<?php
$conn_string = "host=192.168.1.7;port=5432;dbname=vr;user=vr;password=vr";
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
$r = $conn->query($updateUser);
if($r !== false){

}
else{
  print("Erro no update\n");
}

$r = $conn->query($insertUser);
if($r !== false){

}
else{
  print("Erro no insert\n");
}

$conn = null;

?>
  <head>
    <meta charset="UTF-8">
    <title>Auth Service</title>
    <link rel="stylesheet" href="css/reset.css">
    <link rel="stylesheet" href="css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
      <div class="avatar">
        <img src="https://digitalnomadsforum.com/styles/FLATBOOTS/theme/images/user4.png">
      </div>
      <form action="sign.php" method="post">
        <input name="username" type="text" placeholder="username" required>
        <div class="bar">
          <i></i>
        </div>
        <input name="password" type="password" placeholder="password" required>
        <button type="submit">Register</button>
        <button type="submit" formaction="login.php">Login</button>
      </form>
      </form>
    </div>
  </body>
</html>

?>
