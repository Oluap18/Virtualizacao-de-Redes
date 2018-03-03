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
$insertUser = 'INSERT INTO users (userid, mail, password) VALUES ('.$user.',\''.$_POST['username'].'\',\''.$_POST['password'].'\');';
$r = $conn->exec($updateUser);
if($r !== false){

}
else{
  print("Erro no update\n");
}

$r = $conn->exec($insertUser);
if($r !== false){

}
else{
  print("Erro no insert\n");
}

header('Location: index.php');
exit;

?>
