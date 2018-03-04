<!DOCTYPE html>
<html>

<?php
#Get the ipaddress
$iter = 0;
$found = false;
$last_line = exec('ip addr show', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta
  $comp = substr($row, 4, 4);
  if(strcmp($comp, "eth0") == 0){
    $found = true;
  }
  if($found == true){
    $iter++;
  }
  if($iter == 3){
    $host = substr($row, 9, 9)."2";
    break;
  }
}
$conn_string = "host=$host;port=5432;dbname=vr;user=vr;password=vr";
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
  echo "Registado com sucesso\n";
}
else{
  echo "Erro a atualizar o userid\n";
}

$r = $conn->query($insertUser);
if($r !== false){

}
else{
  echo "Erro a inserir o novo utilizador\n";
}

$conn = null;

?>
