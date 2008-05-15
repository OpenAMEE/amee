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
* @version $Id: IndexController.php 1807 2008-04-17 15:32:33Z marcus $
*/

class IndexController extends Zend_Controller_Action {

	protected $amee;
	protected $facebook;
	protected $fbsession;
	protected $appsession;
	protected $db;
	protected $ameecalc = array();
	
	public function init() {
		global $config;
		//Get a database connection
		$this->db = Zend_Db::factory($config->database->adapter, array(
			'host' => $config->database->host,
			'username' => $config->database->username,
			'password' => $config->database->password,
			'dbname' => $config->database->name,
			'port' => $config->database->port
		));
		$this->db->setFetchMode(Zend_Db::FETCH_ASSOC);
	}

	public function preDispatch() {
		global $config;
		//Init FaceBook
		require APP_PATH . DIRECTORY_SEPARATOR . 'facebook.php';
		
		$this->facebook = new Facebook($config->facebook->appapikey, $config->facebook->appsecret);
		
		//Make sure we're logged in as a user
		$user = $this->facebook->require_login();
		
		//Start a PHP session using the FB session key
		//http://wiki.developers.facebook.com/index.php/PHP_Sessions
		session_id($this->facebook->api_client->session_key);
		Zend_Session::start();
		
		$this->fbsession = new Zend_Session_Namespace('facebook');
		
		//Catch the exception that gets thrown if the cookie has an invalid session_key in it
		try {
			if (!$this->facebook->api_client->users_isAppAdded()) {
				$this->facebook->redirect($this->facebook->get_add_url()); //Ask user to add this app
				exit;
			}
		} catch (Exception $ex) {
			//This will clear cookies for your application and redirect them to a login prompt
			$this->facebook->set_user(null, null);
			$this->facebook->redirect($config->facebook->appcallbackurl);
			exit;
		}
		
		//Get params and verify that this request came from FB
		$this->facebook->validate_fb_params();
		if (count($this->facebook->fb_params) <= 0) {
			throw new Exception('Authentication Error: We could not verify that this request came from Facebook. For your safety and privacy, we will not display the page');
		}
		require APP_PATH . DIRECTORY_SEPARATOR . 'amee.class.php';
		$this->amee = new AMEE($config);
		$this->amee->debugOn = true;
		
		//Get info about this user, if there is any
		$this->appsession = new Zend_Session_Namespace('ameecalc');
		if (property_exists($this->appsession, 'ameecalc')) {
			$this->ameecalc = $this->appsession->ameecalc;
		} else {
			$result = $this->db->fetchRow('SELECT * FROM profiles WHERE fb_user_id = ?', $this->facebook->fb_params['user']);
			if ($result) { //Existing user, so get db values
				$this->ameecalc = $result;
				$this->ameecalc['profileitems'] = $this->db->fetchAll('SELECT * FROM profileitems WHERE profile_id = ?', $this->ameecalc['id']);
				$this->ameecalc['cars'] = $this->db->fetchAll('SELECT * FROM cars WHERE profile_id = ?', $this->ameecalc['id']);
				$this->appsession->ameecalc = $this->ameecalc;
				$this->view->newuser = false;
			} else {
				$this->ameecalc = array(); //Empty for a new user
			}
		}
	}
	
	public function indexAction() {
		if (empty($this->ameecalc)) { //Must be a new user, so make an empty record for them
			$this->ameecalc = array();
			$this->ameecalc['amee_profile_id'] = '';
			$this->ameecalc['timestamp'] = gmdate('Y-m-d H:i:s');
			$this->ameecalc['country'] = 'GB';
			$this->ameecalc['elec'] = 0;
			$this->ameecalc['flightsdom'] = 0;
			$this->ameecalc['flightsshort'] = 0;
			$this->ameecalc['flightslong'] = 0;
			$this->ameecalc['fueluse'] = 'yes';
			$this->ameecalc['fueltype'] = 'gas';
			$this->ameecalc['datahash'] = 0;
			$this->ameecalc['carbonprofile'] = 0.0;
			$this->db->insert('profiles', array(
				'fb_user_id' => $this->facebook->fb_params['user'],
				'amee_profile_id' => $this->ameecalc['amee_profile_id'],
				'timestamp' => $this->ameecalc['timestamp'],
				'country' => $this->ameecalc['country'],
				'elec' => $this->ameecalc['elec'],
				'flightsdom' => $this->ameecalc['flightsdom'],
				'flightsshort' => $this->ameecalc['flightsshort'],
				'flightslong' => $this->ameecalc['flightslong'],
				'fueluse' => $this->ameecalc['fueluse'],
				'fueltype' => $this->ameecalc['fueltype'],
				'fuelquantity' => $this->ameecalc['fuelquantity'],
				'datahash' => $this->ameecalc['datahash'],
				'carbonprofile' => $this->ameecalc['carbonprofile']
			));
			$this->ameecalc['id'] = $this->db->lastInsertId();
			$this->ameecalc['profileitems'] = array();
			$this->ameecalc['cars'] = array();
			$this->appsession->ameecalc = $this->ameecalc; //Remember this info
			$this->view->newuser = true;
		}
		include APP_PATH.DIRECTORY_SEPARATOR.'iso3166.php'; //Country list
		$this->view->iso3166 = $iso3166;
		$this->view->ameecalc = $this->ameecalc;
	}
	
	public function saveAction() {
		
	}

	/**
	* Deliver an FBML chunk to select a car by fuel type
	*/
	public function getcarfuelsAction($selected = '') {
		$this->amee->get('/data/transport/car/generic/drill');
		$name = $this->amee->searchResponse("/Resources/DrillDownResource/Choices/Name");
		$options = $this->amee->searchResponse("/Resources/DrillDownResource/Choices/Choices/Choice/Value", true);
		$fuels = array();
		foreach($options as $fuel) {
			$fuels["$fuel"] = ucwords($fuel);
		}
		$this->_helper->layout->disableLayout(); //Chunk only, no layout
		$this->view->fuels = $fuels;
		$this->view->selected = $selected;
	}

	/**
	* Deliver an FBML chunk to select an engine size given a fuel type
	*/
	public function getcarsizesAction($fuel = '', $selected = '') {
		$this->amee->get('/data/transport/car/generic/drill', array('fuel' => $fuel));
		$name = $this->amee->searchResponse("/Resources/DrillDownResource/Choices/Name");
		$options = $this->amee->searchResponse("/Resources/DrillDownResource/Choices/Choices/Choice/Value", true);
		$sizes = array();
		foreach($options as $size) {
			$sizes["$size"] = ucwords($size);
		}
		$this->_helper->layout->disableLayout(); //Chunk only, no layout
		$this->view->sizes = $sizes;
		$this->view->selected = $selected;
	}

	/**
	* Deliver an FBML chunk used to create a new car entry
	*/
	public function addcarAction() {
		$this->_helper->layout->disableLayout(); //Chunk only, no layout
		$this->view->carid = count($this->ameecalc['cars']) + 1;
	}
}
