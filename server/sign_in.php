
<?php
 
/*
 * Following code will send the data read in from a 
 * QR-Code to the database, and register the student
 * as being in attendance.
 * All product details are read from HTTP Post Request
 */

// array for JSON response
$response = array();
 
// check for required fields
if (isset($_POST['student_id']) && isset($_POST['module_id']) && isset($_POST['type'])) {
 
    $student_id = $_POST['student_id'];
    $module_id = $_POST['module_id'];
    $type = $_POST['type'];
 
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql inserting a new row
    $result = mysql_query("INSERT INTO attendance(student_id, module_id, type) VALUES('$student_id', '$module_id', '$type')");
 
    // check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Sign-in completed.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
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