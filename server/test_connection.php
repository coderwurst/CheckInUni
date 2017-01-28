
<?php
 
/*
 * Following code will list all the students
 * and is used as an initial test to establish
 * whether the app is connecting with the 
 * Database
 */
 
// array for JSON response
$response = array();
 
// include db connect class
require_once __DIR__ . '/db_connect.php';
 
// connecting to db
$db = new DB_CONNECT();
 
// get all products from products table
$result = mysql_query("SELECT *FROM student") or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all results
    // products node
    $response["students"] = array();
 
    while ($row = mysql_fetch_array($result)) {
        // temp user array
        $student = array();
        $student["id"] = $row["student_id"];
        $student["name"] = $row["stu_surname"];
         
        // push single student into final response array
        array_push($response["students"], $student);
    }
    // success
    $response["success"] = 1;
    $response["message"] = "connection confirmed";

    // echoing JSON response
    echo json_encode($response);
} else {
    // no products found
    $response["success"] = 0;
    $response["message"] = "No students found";
 
    // echo no users JSON
    echo json_encode($response);
}
?>