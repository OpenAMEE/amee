<?php
/**
* FaceBook AMEE Carbon Calculator App
* Remove application script
* This gets called when a user removes the ameecalc app. It doesn't generate any output as it's not called by a browser.
* @author Marcus Bointon for d::gen network <facebook@amee.cc>
* @package ameecalc
*/


//Get config
require_once 'Zend/Config/Ini.php';

$config = new Zend_Config_Ini('ameecalc.ini', 'production');

//Init FaceBook
require_once 'facebook.php';

$facebook = new Facebook($config->facebook->appapikey, $config->facebook->appsecret);

//Get a database connection
//Zend is clever enough not to connect until actually needed
require_once 'Zend/Db.php';

$db = Zend_Db::factory($config->database->adapter, array(
	'host' => $config->database->host,
	'username' => $config->database->username,
	'password' => $config->database->password,
	'dbname' => $config->database->name
));
$db->setFetchMode(Zend_Db::FETCH_ASSOC);

//Get params and verify that this request came from FB
$facebook->validate_fb_params();
if (count($facebook->fb_params) <= 0) {
	exit; //Looks suspicious, so ignore this request
}

$user = $facebook->get_loggedin_user();
if ($user != NULL && $facebook->fb_params['uninstall'] == 1) {
	//The user has removed your app
	$result = $db->fetchRow('SELECT amee_profile_id, country, gas, elec, car, fly FROM ameecalc WHERE fb_user_id = ?', $facebook->fb_params['user']);
	if ($result) { //Existing user, so get db values
		//Delete profile from AMEE
		require 'amee.class.php';
		$amee = new AMEE;
		try {
			$amee->connect();
		} catch (Exception $e) {
			displayerror('AMEE Error', $e->getMessage());
		}
		$amee->delete('/profiles/'.$result['amee_profile_id']);
		$amee->disconnect();
		$db->delete('ameecalc', 'fb_user_id = \''.$facebook->fb_params['user']."'");
		//profileitems will get deleted by DB cascade
	}
}
?>