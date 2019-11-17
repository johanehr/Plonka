<?php
  header('Content-type: text/plain');
  require 'get_database_credentials.php'; // Provides $db_host,$db_usr,$db_pw,$db_name

  // Gather POST data fields
  $provided_user = $_POST['provided_user'];
  $provided_pw = $_POST['provided_pw'];

  // Connect to database. Only one error message shown, or successful fetch of DB data
  $con=mysqli_connect($db_host,$db_usr,$db_pw,$db_name); // Read from non-public file in database_credentials.php
  if (mysqli_connect_errno($con)) {
    echo 'ERROR: Failed to connect to database'.PHP_EOL;
  }
  else{
    // Fetch user details from database
    $stmt = $con->prepare("SELECT pw_hashed, id, name FROM plonka_users WHERE personal_number = ?");
    $stmt->bind_param('s', $provided_user);
    if($stmt->execute()){
      $result = $stmt->get_result();
      if ($result->num_rows == 1){
        // Only one unique user found
        $row = $result->fetch_assoc(); // Use database column name as key
        $pw_hashed = $row['pw_hashed'];

        // Check whether correct password was provided before outputting personal data
        if (password_verify($provided_pw, $pw_hashed)){
          echo 'SUCCESS: Access granted, password correct.'.PHP_EOL;
          echo 'AccountId:'.$row['id'].PHP_EOL;
          echo 'Name:'.$row['name'].PHP_EOL;
          // Add more details here if needed
        }
        else {
          echo 'ERROR: Access denied, wrong password.'.PHP_EOL;
        }
      }
      else if (mysqli_num_rows($result) == 0){
        echo 'ERROR: No existing user found with the provided personal number. Please register!'.PHP_EOL;
      }
      else { // More than one user returned
       echo 'ERROR: Multiple users with the provided personal number. Contact support.'.PHP_EOL;
      }
      mysqli_free_result($result);
    }
    else {
     echo 'ERROR: SQL statement not executed.'.PHP_EOL;
    }
    $stmt->close();
    mysqli_close($con);
  }
?>
