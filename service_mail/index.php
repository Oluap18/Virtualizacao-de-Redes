<!DOCTYPE html>
<html>
  <body>

    <form>
      Sender token:<br>
      <input type="text" name="token">
      <br>
      To:<br>
      <input type="email" name="emailto">
      <br>
      Subject:<br>
      <input type="text" name="subject">
      <br>
      Message:<br>
      <textarea name="message" rows="20" cols="60">
      Enter message here.
      </textarea>
      <br>
      <input type="submit" value="Send">
    </form>

    <?php
      // Import PHPMailer classes into the global namespace
      // These must be at the top of your script, not inside a function
      use PHPMailer\PHPMailer\PHPMailer;
      use PHPMailer\PHPMailer\Exception;

      //Load composer's autoloader
      require 'Exception.php';
      require 'PHPMailer.php';
      require 'SMTP.php';

      $mail = new PHPMailer(true);                              // Passing `true` enables exceptions
      try {
        //Server settings
        $mail->SMTPDebug = 2;                                 // Enable verbose debug output
        $mail->isSMTP();                                      // Set mailer to use SMTP
        $mail->Host = '172.21.0.2';                           // Specify main and backup SMTP servers
        $mail->Port = 25;                                    // TCP port to connect to

        //Recipients
        $mail->setFrom('jrsmiguel@outlook.pt', 'João Rui');
        $mail->addAddress('jrsmiguel@outlook.pt', 'Joe User');     // Add a recipient
        //$mail->addAddress('ellen@example.com');               // Name is optional
        //$mail->addReplyTo('info@example.com', 'Information');
        //$mail->addCC('cc@example.com');
        //$mail->addBCC('bcc@example.com');

        //Attachments
        //$mail->addAttachment('/var/tmp/file.tar.gz');         // Add attachments
        //$mail->addAttachment('/tmp/image.jpg', 'new.jpg');    // Optional name

        //Content
        $mail->isHTML(true);                                  // Set email format to HTML
        $mail->Subject = 'Here is the subject';
        $mail->Body    = 'This is the HTML message body <b>in bold!</b>';
        $mail->AltBody = 'This is the body in plain text for non-HTML mail clients';

        $mail->send();
        echo 'Message has been sent';
      } catch (Exception $e) {
        echo 'Message could not be sent. Mailer Error: ', $mail->ErrorInfo;
      }
      ?>
  </body>
</html>
