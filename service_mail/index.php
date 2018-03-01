<!DOCTYPE html>
<html>
  <body>
    <form action="mail.php" method="POST">
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
  </body>

</html>


