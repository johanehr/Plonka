<?php
  // Get all the necessary database credentials
  $file = fopen(__DIR__.'/non-public/db.txt', 'r');
  $db_host = trim(fgets($file));
  $db_usr = trim(fgets($file));
  $db_pw = trim(fgets($file));
  $db_name = trim(fgets($file));
  fclose($file);

?>
