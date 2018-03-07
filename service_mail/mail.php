<html>
  <head>
    <meta charset="UTF-8">
    <title>Mail Service</title>
    <link rel="stylesheet" href="http://172.52.0.3/css/reset.css">
    <link rel="stylesheet" href="http://172.52.0.3/css/style.css" media="screen" type="text/css" />
  </head>
  <body>
    <div class="wrap">
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
        echo "Subject: $subject<br>Sent to: $mailto<br>Message: $body<br><br><br>LOG:<br>";

        //Import the PHPMailer class into the global namespace
        use PHPMailer\PHPMailer\PHPMailer;
        use PHPMailer\PHPMailer\Exception;
        require 'Exception.php';
        require 'PHPMailer.php';
        require 'SMTP.php';

        //SMTP needs accurate times, and the PHP time zone MUST be set
        //This should be done in your php.ini, but this is how to do it if you don't have access to that
        date_default_timezone_set('Etc/UTC');

        $mail = new PHPMailer;
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
        //Set who the message is to be sent from
        $mail->setFrom('mail@vr-g9.gcom.di.uminho.pt', 'Virt-Redes');
        $mail->addReplyTo('jrsmiguel@outlook.pt', 'Admin');

        //Set who the message is to be sent to
        $mail->addAddress($mailto, explode('@', $mailto)[0]);
        $mail->Subject = $subject;
        $mail->Body = $body;

        //send the message, check for errors
        if (!$mail->send()) {
            echo "<br><b>Mailer Error: <b>" . $mail->ErrorInfo;
        } else {
            echo "<br><b>Message sent!</b>";
        }
      ?>
    </div>
  </body>

</html>
