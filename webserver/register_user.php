<?php
  header('Content-type: text/plain');
  require 'get_database_credentials.php'; // Provides $db_host,$db_usr,$db_pw,$db_name

  if (isset($_POST['name']) && isset($_POST['email']) && isset($_POST['phone']) && isset($_POST['psw1']) && isset($_POST['psw2'])  && isset($_POST['birthday'])  && isset($_POST['conditions'])){
    $registration_status = 0;  // 0 = all good, OK to register

    // Gather all post data
    $name = $_POST['name'];
    $email = $_POST['email'];
    $phone = $_POST['phone'];
    $password = $_POST['psw1'];
    $password_verify = $_POST['psw2'];
    $personal_number = $_POST['personal_number'];
    $conditions = $_POST['conditions'];
    $timestamp_registration = date('Y-m-d');

    // Check that passwords match
    if ($password != $password_verify) {
      $registration_status = 1;
    }

    // Double-check that conditions accepted - changing the browser HTML works around client side check!
    if ($conditions != 'accept') {
      $registration_status = 2;
    }

    // Verify age above 18 years old
    $birthday = $personal_number; // TODO: Substring right part
    $birthday_DateTime=DateTime::createFromFormat('Ymd', $birthday);
    $ageLimit_DateTime=new DateTime('-18 years');
    if($birthday_DateTime > $ageLimit_DateTime){
      $registration_status = 3;
    }

    switch ($registration_status) {
      case 0:
        // Prepare for database insertion
        $name = strip_tags($name);
        $email = strip_tags($email);
        $phone = strip_tags($phone);
        $personal_number = strip_tags($personal_number);
        $password_hashed = password_hash($password, PASSWORD_BCRYPT);

        // Connect to database
        echo 'Attempting to connect to database...'.PHP_EOL;
        $con=mysqli_connect($db_host,$db_usr,$db_pw,$db_name); // Read from non-public file in database_credentials.php
        if (mysqli_connect_errno($con)) {
          echo 'Failed to connect to database'.PHP_EOL;
        }
        else{
          echo 'Successfully connected to database'.PHP_EOL;

          // Use a prepared statement to protect against SQL injections
          $stmt = $con->prepare("INSERT INTO plonka_users (name, email, phone, personal_number, pw_hashed, registration_date) VALUES (?, ?, ?, ?, ?, ?)");
          $stmt->bind_param("ssssss", $name, $email, $phone, $personal_number, $password_hashed, $timestamp_registration);
          if($stmt->execute()){
            echo 'Registration successful! You can now log in through the app.'.PHP_EOL;
          }
          else{
            echo 'Registration failed! Please try again.'.PHP_EOL;
          }
          $stmt->close();
          mysqli_close($con);
        }
        break;
      case 1:
        echo 'Error! Please make sure that your passwords match.'.PHP_EOL;
        break;
      case 2:
        echo 'Error! Please accept the Terms and Conditions.'.PHP_EOL;
        break;
      case 3:
        echo 'Error! You need to be at least 18 years old to sign up.'.PHP_EOL;
        break;
    }

  }
  else {
    echo 'ERROR! If you received this message after trying to make an account for the MVP app, contact us via email.'.PHP_EOL;
  }

?>
