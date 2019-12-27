<?php
  header('Content-type: text/plain');
  require 'get_database_credentials.php'; // Provides $db_host,$db_usr,$db_pw,$db_name

  // Connect to database. Only one error message shown, or successful fetch of DB data
  $con=mysqli_connect($db_host,$db_usr,$db_pw,$db_name); // Read from non-public file in database_credentials.php
  if (mysqli_connect_errno($con)) {
    echo 'ERROR: Failed to connect to database'.PHP_EOL;
  }
  else{
    // Fetch user details from database
    $stmt = $con->prepare("SELECT * FROM plonka_zones");
    if($stmt->execute()){
      $result = $stmt->get_result();
      $num_zones = mysqli_num_rows($result);
      echo 'NUM_ZONES:'.$num_zones.PHP_EOL; // Used to naively read the correct number of lines, num_zones * 4 lines

      while( $row = $result->fetch_assoc() ) { // Use database column name as key
        $zone_identifier = $row['id'];
        $zone_description = $row['description'];
        $zone_position = $row['position'];
        $zone_balance = $row['balance'];
        echo "---".PHP_EOL; // Human readable separator
        echo $zone_identifier.PHP_EOL;
        echo $zone_description.PHP_EOL;
        echo $zone_position.PHP_EOL;
        echo $zone_balance.PHP_EOL;
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
