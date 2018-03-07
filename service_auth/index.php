<!DOCTYPE html>
<html>

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
  if(strcmp($comp, "eth1") == 0){
    $found1 = true;
  }
  if($found0 == true || $found1 == true){
    $iter++;
  }
  if($iter == 2){
    if($found0 == true){
      $hostDB = substr($row, 20, 9)."2";
      $found0 = false;
    }
    else{
      $ownHost = substr($row, 20, 10);
      $found1 = false;
    }
    $iter = 0;
  }
}
$conn_string = "host=$hostDB;port=5432;dbname=vr;user=vr;password=vr";
try{
  $conn = new PDO("pgsql:".$conn_string);
  if($conn !== false){

  }
  else{
    echo "Connection to database failed\n";
  }
}catch (PDOException $e){
  // report error message
  echo $e->getMessage();
}

$users = 'CREATE TABLE IF NOT EXISTS users (
  userid serial,
  username VARCHAR(255) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL
);';

$tokens = 'CREATE TABLE IF NOT EXISTS tokens (
  tokenid integer NOT NULL,
  userid integer NOT NULL PRIMARY KEY
);';

$vars = 'CREATE TABLE IF NOT EXISTS vars (
  tokenid integer PRIMARY KEY NOT NULL UNIQUE,
  userid integer NOT NULL UNIQUE
);';

$varsExist = 'SELECT * from vars;';
$varsInit = 'INSERT INTO vars (tokenid, userid) VALUES (0,0);';

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
      $r = $conn->query($varsExist);
      if($r->rowCount()==0){
        $r = $conn->query($varsInit);
        if($r !== false){
        }
        else{
          echo "Error creating initial values for vars table\n";
        }
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
    <link rel="stylesheet" href="http://172.52.0.2/css/reset.css">
    <link rel="stylesheet" href="http://172.52.0.2/css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
      <div class="avatar">
        <img src="https://digitalnomadsforum.com/styles/FLATBOOTS/theme/images/user4.png">
      </div>
      <form action="<?php echo "http://$ownHost/sign.php";?>" method="post" target="_blank">
        <input name="username" type="text" placeholder="username" required>
        <div class="bar">
          <i></i>
        </div>
        <input name="password" type="password" placeholder="password" required>
        <br>
        <button type="submit">Register</button>
        <br>
        <button type="submit" formaction="<?php echo "http://$ownHost/login.php";?>">Login</button>
      </form>
    </div>
  </body>
</html>
