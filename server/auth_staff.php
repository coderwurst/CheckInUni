
<?php
 
/*
 * Following code will send the data read in from a 
 * Barcode to the database, and authenticate the 
 * staff ID scanned with those stored.
 * All details are read from HTTP Post Request
 */

// array for JSON response
$response = array();
 
// check for required fields
if (isset($_POST['user_id'])) {
 
    $staff_id = $_POST['user_id'];
     
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql inserting a new row
    $result = mysql_query("SELECT staff_surname FROM staff WHERE staff_id = '$staff_id'");
 
    // check if row inserted or not
    if (mysql_num_rows($result) == 0) {
        
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Staff member in Database.";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>