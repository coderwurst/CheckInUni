
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
if (isset($_POST['student_id'])) {
 
    $student_id = $_POST['student_id'];
         
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql searching for the matching row with student number as scanned
    $result = mysql_query("SELECT stu_forename FROM student WHERE student_id = '$student_id'");
    $row = mysql_fetch_row($result);

    // check if row there
    if (mysql_num_rows($result) == 0) {
        
        // failed to find student ID
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        
        // successfully found ID
        $response["success"] = 1;
        $response["message"] = $row[0];
 
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