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

$users = 'CREATE TABLE IF NOT EXISTS users (
  userid serial,
  mail VARCHAR(255) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL
);';

$tokens = 'CREATE TABLE IF NOT EXISTS tokens (
  tokenid integer NOT NULL PRIMARY KEY,
  userid integer NOT NULL
);';

$vars = 'CREATE TABLE IF NOT EXISTS vars (
  tokenid integer PRIMARY KEY NOT NULL UNIQUE,
  userid integer NOT NULL UNIQUE
);';

$varsInit = 'INSERT INTO vars (tokenid, userid) VALUES (0,0) ON CONFLICT DO NOTHING;';

$sql =  'CREATE TABLE IF NOT EXISTS test (
  id serial PRIMARY KEY,
  prename VARCHAR(50) NOT NULL
);';

$r = $conn->exec($users);
if($r !== false){
  $r = $conn->exec($tokens);
  if($r !== false){
    $r = $conn->exec($vars);
    if($r !== false){
      $r = $conn->query($varsInit);
      if($r !== false){
      }
      else{
        echo "Error creating initial values for vars table\n";
      }
    }
    else{
      echo "Error creating the vars table\n";
    }
  }else{
    echo "Error creating the tokens table\n";
  }
}else{
  echo "Error creating the users table\n";
}

$conn = null;
$r = null;

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
        <button type="submit">Sign in</button>
      </form>
    </div>
  </body>
</html>
