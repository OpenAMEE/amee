<?php
/**
* FaceBook AMEE Carbon Calculator App
*
* This file is part of the AMEE php calculator.
*
* The AMEE php calculator is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* The AMEE php calculator is free software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
* @author Marcus Bointon for d::gen network <facebook@amee.cc>
* @package ameecalc
* @version $Id: index.php 1798 2008-04-08 21:55:33Z marcus $
*/

//Get config
require_once 'Zend/Config/Ini.php';

$config = new Zend_Config_Ini('ameecalc.ini', 'production');

//Init FaceBook
require_once 'facebook.php';

$facebook = new Facebook($config->facebook->appapikey, $config->facebook->appsecret);

//Make sure we're logged in as a user
$user = $facebook->require_login();

//Start a PHP session using the FB session key
//http://wiki.developers.facebook.com/index.php/PHP_Sessions
session_id($facebook->api_client->session_key);
require_once 'Zend/Session.php';
Zend_Session::start();
require_once 'Zend/Session/Namespace.php';

$FBSESSION = new Zend_Session_Namespace('facebook');

//catch the exception that gets thrown if the cookie has an invalid session_key in it
try {
	if (!$facebook->api_client->users_isAppAdded()) {
		$facebook->redirect($facebook->get_add_url()); //Ask user to add this app
		exit;
	}
} catch (Exception $ex) {
	//this will clear cookies for your application and redirect them to a login prompt
	$facebook->set_user(null, null);
	$facebook->redirect($config->facebook->appcallbackurl);
	exit;
}

//Get params and verify that this request came from FB
$facebook->validate_fb_params();
if (count($facebook->fb_params) <= 0) {
	displayerror('Authentication Error', 'We could not verify that this request came from Facebook. For your safety and privacy, we will not display the page');
}

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

//What are we going to do?
$action = '';
if (array_key_exists('action', $_POST)) {
	$action = $_POST['action'];
} elseif (array_key_exists('action', $_GET)) {
	$action = $_GET['action'];
}
switch ($action) {
	case 'calc':
		//Validate calc form
		$ameecalc = array();
		if (array_key_exists('ameecalc', $_REQUEST) and is_array($_REQUEST['ameecalc'])) {
			if (array_key_exists('country', $_REQUEST['ameecalc'])) {
				require 'iso3166.php';
				if (array_key_exists($_REQUEST['ameecalc']['country'], $iso3166)) {
					$ameecalc['country'] = $_REQUEST['ameecalc']['country'];
				} else {
					$ameecalc['country'] = 'GB';
				}
			} else {
				$ameecalc['country'] = 'GB';
			}
			if (array_key_exists('gas', $_REQUEST['ameecalc'])) {
				$ameecalc['gas'] = intval($_REQUEST['ameecalc']['gas']);
			} else {
				$ameecalc['gas'] = 0;
			}
			if (array_key_exists('elec', $_REQUEST['ameecalc'])) {
				$ameecalc['elec'] = intval($_REQUEST['ameecalc']['elec']);
			} else {
				$ameecalc['elec'] = 0;
			}
			if (array_key_exists('car', $_REQUEST['ameecalc'])) {
				$ameecalc['car'] = intval($_REQUEST['ameecalc']['car']);
			} else {
				$ameecalc['car'] = 0;
			}
			if (array_key_exists('fly', $_REQUEST['ameecalc'])) {
				$ameecalc['fly'] = intval($_REQUEST['ameecalc']['fly']);
			} else {
				$ameecalc['fly'] = 0;
			}
		} else {
			displayerror('Submission error', 'You submitted corrupt data.');
		}
		//Get current user data
		$ameecalcdata = $db->fetchRow('SELECT * FROM ameecalc WHERE fb_user_id = ?', $facebook->fb_params['user']);
		//Get any existing AMEE data
		$profileitemdata = $db->fetchAll('SELECT * FROM profileitems WHERE ameecalc_id = ?', $ameecalcdata['id']);
		//Arrange things nicely
		$profileitems = array();
		foreach($profileitemdata as $pid) {
			$profileitems[$pid['name']] = $pid;
		}
		//Save user entered values
		$db->update('ameecalc', array(
			'country' => $ameecalc['country'],
			'gas' => $ameecalc['gas'],
			'elec' => $ameecalc['elec'],
			'car' => $ameecalc['car'],
			'fly' => $ameecalc['fly'],
			'timestamp' => gmdate('Y-m-d H:i:s')
		), 'fb_user_id = '.$facebook->fb_params['user']);
		//If the submitted data is the same as last time, don't bother calling AMEE
		$datahash = crc32(serialize($ameecalc)); //Note that amee_profile_id and carbonprofile are not included
		$ameecalc['amee_profile_id'] = $ameecalcdata['amee_profile_id'];
		if (true or $ameecalcdata['datahash'] != $datahash) { //Data is new or different, need to ask AMEE
			require 'amee.class.php';
			$amee = new AMEE;
			$amee->debugOn = true;
			try {
				$amee->connect();
			} catch (Exception $e) {
				displayerror('AMEE Error', $e->getMessage());
			}
			if (empty($ameecalc['amee_profile_id'])) { //We don't have an AMEE profile yet, so need to get one
				$ameecalc['amee_profile_id'] = $amee->createNewProfile();
			}
			
			//For sanity, we store everything in kgco2permonth so it's the same as in AMEE
			//Calculation of the final profile deals with multiplying it out

			//Gas
			if (!array_key_exists('gas', $profileitems)) { //We don't have data on this yet
				//Get DataItem UID
				$amee->get('/data/home/energy/quantity/drill', array('type' => 'gas'));
				$profileitems['gas']['dataitem_uid'] = (string)$amee->searchResponse("/Resources/DrillDownResource/Choices[Name='uid']/Choices/Choice/Value");
				//Make a profileItem attached to that dataitem
				$amee->post('/profiles/'.$ameecalc['amee_profile_id'].'/home/energy/quantity', array('dataItemUid' => $profileitems['gas']['dataitem_uid'], 'kWhPerMonth' => $ameecalc['gas'] / 12)); //Convert kwh/year into kwh/month
				$profileitems['gas']['profileitem_uid'] = (string)$amee->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/@uid');
				
				$profileitems['gas']['kgco2permonth'] =  floatval($amee->searchResponse("/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth"));
				//Insert new data into DB
				$db->insert('profileitems', array(
					'ameecalc_id' => $ameecalcdata['id'],
					'dataitem_uid' => $profileitems['gas']['dataitem_uid'],
					'profileitem_uid' => $profileitems['gas']['profileitem_uid'],
					'inputvalue' => $ameecalc['gas'],
					'kgco2permonth' => $profileitems['gas']['kgco2permonth'],
					'name' => 'gas'
				));
			} else {
				if ($ameecalc['gas'] != $profileitems['gas']['inputvalue']) { //This value has changed since last time
					//Update this profileitem
					$amee->put('/profiles/'.$ameecalc['amee_profile_id'].'/home/energy/quantity/'.$profileitems['gas']['profileitem_uid'], array('kWhPerMonth' => floatval($ameecalc['gas']) / 12)); //Convert kwh/year into kwh/month
					$profileitems['gas']['kgco2permonth'] = floatval($amee->searchResponse('/Resources/ProfileItemResource/ProfileItem/AmountPerMonth'));
					//Save changed data back into DB
					$db->update('profileitems', array(
						'inputvalue' => $ameecalc['gas'],
						'kgco2permonth' => $profileitems['gas']['kgco2permonth']
					), 'id = '.$profileitems['gas']['id']);
				}
			}

			//Electricity
			if (!array_key_exists('elec', $profileitems)) { //We don't have data on this yet
				//Get DataItem UID
				$amee->get('/data/home/energy/electricityiso'); //Gets list of all countries
				$profileitems['elec']['dataitem_uid'] = (string)$amee->searchResponse("/Resources/DataCategoryResource/Children/DataItems/DataItem[country='".$ameecalc['country']."']/@uid"); //Does this user's country appear in the list?
				if (empty($profileitems['elec']['dataitem_uid'])) {
					$profileitems['elec']['dataitem_uid'] = (string)$amee->searchResponse("/Resources/DataCategoryResource/Children/DataItems/DataItem[country='GB']/@uid"); //If not, just use UK
				}
				//Make a profileItem attached to that dataitem
				$amee->post('/profiles/'.$ameecalc['amee_profile_id'].'/home/energy/electricityiso', array('dataItemUid' => $profileitems['elec']['dataitem_uid'], 'kWhPerMonth' => $ameecalc['elec'] / 12)); //Convert kwh/year into kwh/month
				$profileitems['elec']['profileitem_uid'] = (string)$amee->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/@uid');
				
				$profileitems['elec']['kgco2permonth'] =  floatval($amee->searchResponse("/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth"));
				//Insert new data into DB
				$db->insert('profileitems', array(
					'ameecalc_id' => $ameecalcdata['id'],
					'dataitem_uid' => $profileitems['elec']['dataitem_uid'],
					'profileitem_uid' => $profileitems['elec']['profileitem_uid'],
					'inputvalue' => $ameecalc['elec'],
					'kgco2permonth' => $profileitems['elec']['kgco2permonth'],
					'name' => 'elec'
				));
			} else {
				//TODO does not handle changing of country
				if ($ameecalc['elec'] != $profileitems['elec']['inputvalue']) { //This value has changed since last time
					//Update this profileitem
					$amee->put('/profiles/'.$ameecalc['amee_profile_id'].'/home/energy/electricityiso/'.$profileitems['elec']['profileitem_uid'], array('kWhPerMonth' => floatval($ameecalc['elec']) / 12)); //Convert kwh/year into kwh/month
					$profileitems['elec']['kgco2permonth'] = floatval($amee->searchResponse('/Resources/ProfileItemResource/ProfileItem/AmountPerMonth'));
					//Save changed data back into DB
					$db->update('profileitems', array(
						'inputvalue' => $ameecalc['elec'],
						'kgco2permonth' => $profileitems['elec']['kgco2permonth']
					), 'id = '.$profileitems['elec']['id']);
				}
			}

			//Car
			if (!array_key_exists('car', $profileitems)) { //We don't have data on this yet
				//TODO does not handle cars in other countries
				$amee->get('/data/transport/car/generic/drill', array('fuel' => 'petrol', 'size' => 'medium'));
				$profileitems['car']['dataitem_uid'] = (string)$amee->searchResponse("/Resources/DrillDownResource/Choices[Name='uid']/Choices/Choice/Value");
				$amee->post('/profiles/'.$ameecalc['amee_profile_id'].'/transport/car/generic', array('dataItemUid' => $profileitems['car']['dataitem_uid'], 'distanceKmPerMonth' => (($ameecalc['car'] * 1000) / 12) * 1.609344)); //Convert kmiles/year into km/month
				$profileitems['car']['profileitem_uid'] = (string)$amee->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/@uid');
				$profileitems['car']['kgco2permonth'] = floatval($amee->searchResponse("/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth"));
				//Insert new data into DB
				$db->insert('profileitems', array(
					'ameecalc_id' => $ameecalcdata['id'],
					'dataitem_uid' => $profileitems['car']['dataitem_uid'],
					'profileitem_uid' => $profileitems['car']['profileitem_uid'],
					'inputvalue' => $ameecalc['car'],
					'kgco2permonth' => $profileitems['car']['kgco2permonth'],
					'name' => 'car'
				));
			} else {
				if ($ameecalc['car'] != $profileitems['car']['inputvalue']) { //This value has changed since last time
					//Update this profileitem
					$amee->put('/profiles/'.$ameecalc['amee_profile_id'].'/transport/car/generic/'.$profileitems['car']['profileitem_uid'], array('distanceKmPerMonth' => (($ameecalc['car'] * 1000) / 12) * 1.609344)); //Convert kmiles/year into km/month
					$profileitems['car']['kgco2permonth'] = floatval($amee->searchResponse('/Resources/ProfileItemResource/ProfileItem/AmountPerMonth'));
					//Save changed data back into DB
					$db->update('profileitems', array(
						'inputvalue' => $ameecalc['car'],
						'kgco2permonth' => $profileitems['car']['kgco2permonth']
					), 'id = '.$profileitems['car']['id']);
				}
			}

			//Flights
			if (!array_key_exists('fly', $profileitems)) { //We don't have data on this yet
				$amee->get('/data/transport/plane/generic/drill', array('size' => 'one way', 'type' => 'short haul'));
				$profileitems['fly']['dataitem_uid'] = (string)$amee->searchResponse("/Resources/DrillDownResource/Choices[Name='uid']/Choices/Choice/Value");
				$amee->post('/profiles/'.$ameecalc['amee_profile_id'].'/transport/plane/generic', array('dataItemUid' => $profileitems['fly']['dataitem_uid'], 'journeysPerYear' => $ameecalc['fly']));
				$profileitems['fly']['profileitem_uid'] = (string)$amee->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/@uid');
				$profileitems['fly']['kgco2permonth'] = floatval($amee->searchResponse("/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth"));
				//Insert new data into DB
				$db->insert('profileitems', array(
					'ameecalc_id' => $ameecalcdata['id'],
					'dataitem_uid' => $profileitems['fly']['dataitem_uid'],
					'profileitem_uid' => $profileitems['fly']['profileitem_uid'],
					'inputvalue' => $ameecalc['fly'],
					'kgco2permonth' => $profileitems['fly']['kgco2permonth'],
					'name' => 'fly'
				));
			} else {
				if ($ameecalc['fly'] != $profileitems['fly']['inputvalue']) { //This value has changed since last time
					//Update this profileitem
					$amee->put('/profiles/'.$ameecalc['amee_profile_id'].'/transport/plane/generic/'.$profileitems['fly']['profileitem_uid'], array('journeysPerYear' => $ameecalc['fly']));
					$profileitems['fly']['kgco2permonth'] = floatval($amee->searchResponse('/Resources/ProfileItemResource/ProfileItem/AmountPerMonth'));
					//Save changed data back into DB
					$db->update('profileitems', array(
						'inputvalue' => $ameecalc['fly'],
						'kgco2permonth' => $profileitems['fly']['kgco2permonth']
					), 'id = '.$profileitems['fly']['id']);
				}
			}
			
			//Now add them all up
			$ameecalc['carbonprofile'] = 0.0;
			foreach($profileitems as $profileitem) {
				$ameecalc['carbonprofile'] += $profileitem['kgco2permonth'] * 12;
			}
		} else {
			$ameecalc['carbonprofile'] = floatval($ameecalcdata['carbonprofile']); //Use cached version
		}
		//Cache result in local DB
		$db->update('ameecalc', array(
			'amee_profile_id' => $ameecalc['amee_profile_id'],
			'carbonprofile' => $ameecalc['carbonprofile'],
			'datahash' => $datahash,
		), 'fb_user_id = '.$facebook->fb_params['user']);
		//Publish changes to users's feed
		$facebook->api_client->feed_publishActionOfUser('<fb:userlink uid="'.$facebook->fb_params['user'].'" /> updated <fb:pronoun uid="'.$facebook->fb_params['user'].'" possessive="true" usethey="false" /> AMEE carbon profile.', 'It\'s now '.number_format($ameecalc['carbonprofile']).' kgCO<sub>2</sub>/year. Are you any better? <a href="http://apps.facebook.com/ameecalc/">Calculate yours now!</a>');
		//Deliver results
		include 'views/profile.php';
		break;
	case 'logout':
		break;
	case 'remove':
		//Delete user from DB
		$ameecalc = array();
		$result = $db->fetchRow('SELECT amee_profile_id, country, gas, elec, car, fly FROM ameecalc WHERE fb_user_id = ?', $facebook->fb_params['user']);
		if ($result) { //Existing user, so get db values
			$db->delete('ameecalc', 'fb_user_id = '.$facebook->fb_params['user']);
			//Delete profile from AMEE
			require 'amee.class.php';
			$amee = new AMEE;
			$amee->debugOn = true;
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
		include 'views/remove.php';
		break;
	case 'friends':
		//Get profiles of any friends we know
		$friends1 = $facebook->friends_getAppUsers(); //Gets all friends that also have this app
		if (!empty($facebook->fb_params['friends'])) {
			$friends = $db->fetchAll('SELECT amee_profile_id, country, gas, elec, car, fly, carbonprofile FROM ameecalc WHERE fb_user_id IN('.$facebook->fb_params['friends'].') AND carbonprofile > 0 ORDER BY carbonprofile DESC LIMIT 10');
		}
		break;
	case 'canvas':
	case 'edit':
	default:
		//Deliver a default welcome page, perhaps some global stats
		//Maybe a top-10 list
		$ameecalc = array();
		$result = $db->fetchRow('SELECT amee_profile_id, country, gas, elec, car, fly, carbonprofile FROM ameecalc WHERE fb_user_id = ?', $facebook->fb_params['user']);
		if ($result) { //Existing user, so get db values
			$ameecalc['amee_profile_id'] = $result['amee_profile_id'];
			$ameecalc['country'] = $result['country'];
			$ameecalc['gas'] = $result['gas'];
			$ameecalc['elec'] = $result['elec'];
			$ameecalc['car'] = $result['car'];
			$ameecalc['fly'] = $result['fly'];
			$ameecalc['carbonprofile'] = $result['carbonprofile'];
		} else { //New user, so set defaults and put new record into DB
			$ameecalc['amee_profile_id'] = '';
			$ameecalc['country'] = 'GB';
			$ameecalc['gas'] = 0;
			$ameecalc['elec'] = 0;
			$ameecalc['car'] = 0;
			$ameecalc['fly'] = 0;
			$ameecalc['carbonprofile'] = 0.0;
			$db->insert('ameecalc', array(
				'fb_user_id' => $facebook->fb_params['user'],
				'country' => $ameecalc['country'],
				'gas' => $ameecalc['gas'],
				'elec' => $ameecalc['elec'],
				'car' => $ameecalc['car'],
				'fly' => $ameecalc['fly'],
				'timestamp' => gmdate('Y-m-d H:i:s')
			));
		}
		if ($action == 'edit' or $ameecalc['carbonprofile'] <= 0) {
			include 'iso3166.php';
			include 'views/calc.php';
		} else {
			include 'views/profile.php';
		}
		break;
}

/**
* Present a FB-style fatal error message
* @param string $errortitle The title of the displayed error box
* @param string $errormessage The body of the displayed error box, may include fbml/html
*/
function displayerror($errortitle, $errormessage) {
	include 'views/error.php';
	exit;
}
?>