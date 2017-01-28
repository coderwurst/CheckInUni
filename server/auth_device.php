
<?php
 
/*
 * Following code will send the data read in from a 
 * Barcode to the database, and authenticate the student
 * ID scanned with those stored.
 * All details are read from HTTP Post Request
 */

// array for JSON response
$response = array();
 
// check for required fields
if (isset($_POST['user_id']) && isset($_POST['device_id'])) {
 
    $user_id = $_POST['user_id'];
    $device_id = $_POST['device_id'];
     
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // initial check to determine if the device_id field for the student id is blank
    $result = mysql_query("SELECT * FROM student WHERE student_id = '$user_id' AND device_id IS NULL OR student_id = '$user_id' AND device_id = '$device_id'");

    // if the field is not blank, the user has already registered on another device
    if (mysql_num_rows($result) == 0) {
        
        // failed to find student ID
        $response["success"] = 0;
        $response["message"] = "Oops! This user has already registered on another device.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        
        $checkforID = mysql_query("SELECT * FROM student WHERE student_id = '$user_id' AND device_id IS NULL");

        if (mysql_num_rows($checkforID)!=0) {

        // add device details to user
        $addDeviceID = mysql_query("UPDATE student SET device_id = '$device_id' WHERE student_id = '$user_id'");
        
        }// checkforID

        // successfully found ID
        $response["success"] = 1;
        $response["message"] = "Device ID Check OK!";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Device ID Error!";
 
    // echoing JSON response
    echo json_encode($response);
}
?>