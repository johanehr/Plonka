<?php
  header('Content-type: text/plain');
  require 'get_database_credentials.php'; // Provides $db_host,$db_usr,$db_pw,$db_name

  if (isset($_POST['user_id']) && isset($_POST['psw'])){
    // Gather all post data
    $user_id = strip_tags($_POST['user_id']);
    $password = strip_tags($_POST['psw']);

    // Connect to database. Only one error message shown, or successful fetch of DB data
    $con=mysqli_connect($db_host,$db_usr,$db_pw,$db_name); // Read from non-public file in database_credentials.php
    if (mysqli_connect_errno($con)) {
      echo 'ERROR: Failed to connect to database'.PHP_EOL;
    }
    else{
        // First, check that the user is actually authenticated (otherwise this PHP-script can be invoked from other sources, potentially revealing sensitive info)
        $stmt = $con->prepare("SELECT pw_hashed FROM plonka_users WHERE id = ?");
        $stmt->bind_param('s', $user_id);
        if($stmt->execute()){
          $result = $stmt->get_result();
          if ($result->num_rows == 1){
            // Only one unique user should have been found
            $row = $result->fetch_assoc(); // Use database column name as key
            $pw_hashed = $row['pw_hashed'];

            if (password_verify($password, $pw_hashed)){
              // Fetch user's shifts from database and echo out
              $stmt2 = $con->prepare("SELECT * FROM plonka_shifts WHERE user_id = ?");
              $stmt2->bind_param('s', $user_id);
              if($stmt2->execute()){
                $result2 = $stmt2->get_result();
                $num_shifts = mysqli_num_rows($result2);
                echo 'NUM_SHIFTS:'.$num_shifts.PHP_EOL; // Used to naively read the correct number of lines, num_shifts * 4 lines

                while( $row = $result2->fetch_assoc() ) { // Use database column name as key
                  $shift_user = $row['user_id'];
                  $shift_info = $row['information'];
                  $shift_zones = $row['zone_ids'];
                  $shift_status = $row['status'];

                  echo "---".PHP_EOL; // Human readable separator
                  echo $shift_zones.PHP_EOL;
                  echo $shift_info.PHP_EOL;
                  echo $shift_status.PHP_EOL;
                }
                mysqli_free_result($result2);
              }
            }
            else {
              echo 'ERROR: Access denied, wrong password.'.PHP_EOL;
            }
          }
          else if (mysqli_num_rows($result) == 0){
            echo 'ERROR: No existing user found with the provided user id.'.PHP_EOL;
          }
          else { // More than one user returned
           echo 'ERROR: Multiple users with the provided user id. Contact support.'.PHP_EOL;
          }
        mysqli_free_result($result);
      }
      else {
       echo 'ERROR: SQL statement not executed.'.PHP_EOL;
      }
      $stmt->close();
      mysqli_close($con);
    }
  }
  else {
    echo 'ERROR: POST-error, contact support.'.PHP_EOL;
  }
?>
