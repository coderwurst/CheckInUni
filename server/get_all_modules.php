
<?php
 
/*
 * Following code will list all the modules
 * and is used within the lecturer functions
 * to recall the appropriate QR-Code image
 * relating to a specific module
 */
 
// array for JSON response
$response = array();
 
// include db connect class
require_once __DIR__ . '/db_connect.php';
 
// connecting to db
$db = new DB_CONNECT();
 
// get all products from products table
$result = mysql_query("SELECT * FROM module_codes") or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all results
    // products node
    $response["modules"] = array();
 
    while ($row = mysql_fetch_array($result)) {
        // temp user array
        $modules = array();
        $modules["id"] = $row["module_code"];
        $modules["name"] = $row["module_name"];
        $modules["lectureUrl"] = $row["lecture"];
        $modules["tutorialUrl"] = $row["tutorial"];
         
        // push single student into final response array
        array_push($response["modules"], $modules);
    }
    // success
    $response["success"] = 1;
 
    // echoing JSON response
    echo json_encode($response);
} else {
    // no products found
    $response["success"] = 0;
    $response["message"] = "No modules found";
 
    // echo no users JSON
    echo json_encode($response);
}
?>