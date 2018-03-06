<?php

#Get the ipaddress
$iter = 0;
$found = false;
$last_line = exec('ifconfig', $full_output);
foreach($full_output as $row){
  #Encontrar a interface correta
  $comp = substr($row, 0, 4);
  if(strcmp($comp, "eth1") == 0){
    $found = true;
  }
  if($found == true){
    $iter++;
  }
  if($iter == 2){
    $host = substr($row, 20, 9)."3";
    break;
  }
}



$subject = $_POST['subject'];
$mailto = $_POST['emailto'];
$body = $_POST['message'];
echo "$subject $mailto $body\n";
/**
 * This example shows making an SMTP connection without using authentication.
 */

//Import the PHPMailer class into the global namespace
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;
require 'Exception.php';
require 'PHPMailer.php';
require 'SMTP.php';

//SMTP needs accurate times, and the PHP time zone MUST be set
//This should be done in your php.ini, but this is how to do it if you don't have access to that
date_default_timezone_set('Etc/UTC');

//require '../vendor/autoload.php';

//Create a new PHPMailer instance
$mail = new PHPMailer;
//Tell PHPMailer to use SMTP
$mail->isSMTP();
//Enable SMTP debugging
// 0 = off (for production use)
// 1 = client messages
// 2 = client and server messages
$mail->SMTPDebug = 2;
//Set the hostname of the mail server
$mail->Host = $host;
//Set the SMTP port number - likely to be 25, 465 or 587
$mail->Port = 25;
//We don't need to set this as it's the default value
//$mail->SMTPAuth = false;
//Set who the message is to be sent from
$mail->setFrom('mail@server.uminho.pt', 'Virt. Redes');
//Set an alternative reply-to address
$mail->addReplyTo('jrsmiguel@outlook.pt', 'Admin');
//Set who the message is to be sent to
$mail->addAddress($mailto, explode('@', $mailto)[0]);
//Set the subject line
$mail->Subject = $subject;
//Read an HTML message body from an external file, convert referenced images to embedded,
//convert HTML into a basic plain-text alternative body
//$mail->msgHTML(file_get_contents('contents.html'), __DIR__);
//Replace the plain text body with one created manually
$mail->Body = $body;
//Attach an image file
//$mail->addAttachment('images/phpmailer_mini.png');

//send the message, check for errors
if (!$mail->send()) {
    echo "Mailer Error: " . $mail->ErrorInfo;
} else {
    echo "Message sent!";
}
?>
