<!DOCTYPE html>
<html>
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
        <form>
          </div>
            <input type="text" placeholder="username" required>
          <div class="bar">
          <i></i>
        <form>
      </div>
      <input type="password" placeholder="password" required>
      <a href="" class="fl"></a>
      <button>Sign in</button>
    </div>

    <?php
      echo $_POST["name"];
      echo $_POST["email"];
    ?>
  </body>
</html>
