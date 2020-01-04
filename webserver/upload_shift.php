<?php
  header('Content-type: text/plain');
  require 'get_database_credentials.php'; // Provides $db_host,$db_usr,$db_pw,$db_name

  // Save uploaded image file:
  // Heavily based on example from: https://www.w3schools.com/php/php_file_upload.asp
  // Check whether has file, with additional security: https://stackoverflow.com/questions/946418/how-to-check-whether-the-user-uploaded-a-file-in-php
  $file_included = (file_exists($_FILES['file']['tmp_name']) && is_uploaded_file($_FILES['file']['tmp_name']));
  if ($file_included){
    $target_dir = "files/";
    $file = $_FILES["file"]["name"];
    $target_file = $target_dir.basename($file);

    if (!move_uploaded_file($_FILES['file']['tmp_name'], $target_file)) {
      echo 'ERROR: File couldn\'t be uploaded.'.PHP_EOL;
      exit();
    }
  }
  // Include if proof of work (video, image, etc) is required.
  /*
  else {
    echo 'ERROR: No image file seems to have been sent in request.'.PHP_EOL;
    exit();
  }
  */

  // Note: Only one message should be shown (either "ERROR: <Message>" or "SUCCESS"), which is read by the calling script.
  if (isset($_POST['user_id']) && isset($_POST['psw']) && isset($_POST['location_info']) && isset($_POST['zone_ids']) && isset($_POST['img_path'])){ // TODO: Get attached image file
    // Gather all post data
    $user_id = strip_tags($_POST['user_id']);
    $password = strip_tags($_POST['psw']);
    $loc_info = strip_tags($_POST['location_info']);
    $zone_ids = strip_tags($_POST['zone_ids']);
    $img_path = strip_tags($_POST['img_path']); // Matches corresponding image on webserver
    $status = "Pending"; // Always start with pending status

    // Connect to database.
    $con=mysqli_connect($db_host,$db_usr,$db_pw,$db_name); // Read from non-public file in database_credentials.php
    if (mysqli_connect_errno($con)) {
      echo 'ERROR: Failed to connect to database'.PHP_EOL;
    }
    else {
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
            // Use a prepared statement to protect against SQL injections
            $stmt2 = $con->prepare("INSERT INTO plonka_shifts (user_id, zone_ids, information, status, img_path) VALUES (?, ?, ?, ?, ?)");
            $stmt2->bind_param("sssss", $user_id, $zone_ids, $loc_info, $status, $img_path);
            if($stmt2->execute()){
              echo 'Uploaded successfully!'.PHP_EOL;
            }
            else{
              echo 'ERROR: Session upload to database failed.'.PHP_EOL;
            }
            $stmt2->close();
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
       echo 'ERROR: Password matching not executed.'.PHP_EOL;
      }
      $stmt->close();
    }
  }
  else {
    echo 'ERROR: POST-error, contact support.'.PHP_EOL;
  }
?>
