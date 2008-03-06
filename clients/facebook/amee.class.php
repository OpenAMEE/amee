<?php
/**
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
* @package amee
* @author Marcus Bointon for d::gen network <facebook@amee.cc>
* @author Andrew Conway for d::gen network <andrew@dgen.net>
* @version $Id: amee.class.php 1758 2008-02-18 13:07:46Z gavin $
*/
 
class AMEE {
	
	private $db;
	private $authToken = '';
	private $authexpires = 0;
	private $responses = array();
	protected $config;
	protected $AMEESESSION;
	protected $lastresult = '';
	protected $cookies = array();
	public $useCache = false;
	public $debugOn = false;
	
	/**
	* Initialise this AMEE instance
	*/
	public function __construct() {
		//Get config
		require_once 'Zend/Config/Ini.php';
		$this->config = new Zend_Config_Ini('amee.ini', 'production');

		//Connect to DB
		require_once 'Zend/Db.php';
		$this->db = Zend_Db::factory($this->config->database->adapter, array(
			'host' => $this->config->database->host,
			'username' => $this->config->database->username,
			'password' => $this->config->database->password,
			'dbname' => $this->config->database->name
		));
		$this->db->setFetchMode(Zend_Db::FETCH_ASSOC);

		//Create a session namespace just for us
		require_once 'Zend/Session/Namespace.php';
		$this->AMEESESSION = new Zend_Session_Namespace('AMEE');
	}
	
	/**
	* This function authenticates with AMEE - this needs to be done just once per session and
	* returns an authToken which is passed back to AMEE in all subsequent requests.
	* Throws an exception if authenticaton fails
	* Connection parameters come from the amee.ini file
	* @return boolean
	*/
	public function connect() {
		//Are we already authenticated with an unexpired key?
		if (!empty($this->authToken) and $this->authexpires > time()) {
			return true;
		}
		//Did we already connect in an earlier request and store it in the session?
		if (isset($this->AMEESESSION->authToken) and !empty($this->AMEESESSION->authToken) and $this->AMEESESSION->authexpires > time()) {
			$this->authToken = $this->AMEESESSION->authToken;
			$this->authexpire = $this->AMEESESSION->authexpires;
			return true;
		}
		//Explicitly call sendRequest as we want to get our hands on the headers
		$lines = $this->sendRequest('POST /auth', $this->makeQueryString(array('username' => $this->config->amee->username, 'password' => $this->config->amee->password)), false, false);
	
		foreach ($lines as $line) {
			$matches = array();
			if (preg_match('/^authToken: (.*)$/', $line, $matches)) {
				$this->authToken = trim($matches[1]);
				$this->AMEESESSION->authToken = $this->authToken;
				//AMEE sessions time out after 30 mins idle, so preemptively expire our auth before that
				$this->authexpires = strtotime('+29 minutes', time());
				$this->AMEESESSION->authToken = $this->authexpires;
				//var_dump($this);
				return true; //Don't bother looking at any other headers
			}
		}
		if(empty($this->authToken)) {
			$this->debug('AMEE authentication failed');
		}
		throw new Exception('AMEE authentication failed');
		return false;
	}
	
	/**
	* Trash auth token and session
	*/
	public function disconnect() {
		$this->authToken = '';
		$this->authexpires = 0;
		$this->AMEESESSION->authToken = '';
		$this->AMEESESSION->authexpires = 0;
	}
	
	/**
	* Reconnect after an auth failure or timeout
	*/
	public function reconnect() {
		$this->disconnect();
		$this->connect();
	}
	
	/**
	* @param string $line should be a cookie line from the HTTP response
	* Note: cookies are essential right now, but may be for live server
	*/
	private function storeCookie($line) {
		$p1 = strpos($line, ':');
		$s = substr($line, $p1 + 1);
		$ss = split('=', $s, 2);
		$this->cookies[trim($ss[0])] = trim($ss[1]);
	}
	
	/**
	* Creates header lines for cookies
	* @return string
	*/
	private function getCookieLines() {
		$lines = '';
		//goes through the cookiesMap and outputs appropriate header lines
		foreach($this->cookies as $name => $value){
			$lines .= 'Cookie: '.$name.'='.$value."\n";
		}
		return $lines;
	}

	/**
	* Wrapper to simplify AMEE POST requests
	* @param string $path The query path, such as "/auth"
	* @param array $params Associative array of parameters
	*/
	public function post($path, $params = array()) {
		return $this->sendRequest("POST $path", $this->makeQueryString($params));
	}

	/**
	* Wrapper to simplify AMEE PUT requests
	* @param string $path The query path, such as "/auth"
	* @param array $params Associative array of parameters
	*/
	public function put($path, $params = array()) {
		return $this->sendRequest("PUT $path", $this->makeQueryString($params));
	}

	/**
	* Wrapper to simplify AMEE GET requests
	* @param string $path The query path, such as "/auth"
	* @param array $params Associative array of parameters
	*/
	public function get($path, $params = array()) {
		if (count($params) > 0) {
			return $this->sendRequest("GET $path?".$this->makeQueryString($params), '');
		} else {
			return $this->sendRequest("GET $path", '');
		}
	}
	
	/**
	* Wrapper to simplify AMEE DELETE requests
	* @param string $path The query path, such as "/auth"
	*/
	public function delete($path) {
		return $this->sendRequest("DELETE $path", '');
	}

	/**
	* Make a request to the AMEE API
	* By default returns only the xml line, but if $xml_only=false then
	* an array containing all lines is returned.
	* @param string $path the URL path to access, including the HTTP request type, e.g. 'POST /auth'
	* @param string $body The request body formatted as application/x-www-form-urlencoded - only used for PUT and POST requests
	* @param boolean $xml_only Whether to return just the body of the response (true, default), or to include headers as well (false)
	* @param boolean $repeat Whether this is a repeated request after re-authentication, only used internally
	* @return string
	* @access protected
	*/
	protected function sendRequest($path, $body, $xml_only = true, $repeat = true) {
		if(!empty($this->authToken) && strpos($path, 'POST /auth') === false){
			$this->connect();
		}
		$header = $path." HTTP/1.0\n"
			.$this->getCookieLines() //insert cookies
			."Accept: application/xml\n";
		if(!empty($this->authToken)) {
			$header.="authToken: ".$this->authToken."\n";
		}
		$header .= "Host: ".$this->config->amee->host."\n"
		."Content-Type: application/x-www-form-urlencoded\n"
		."Content-Length: ".strlen($body)."\n"
		."\n"
		.$body;
		
		$s = socket_create(AF_INET, SOCK_STREAM, 0);
		$z = socket_connect($s, gethostbyname($this->config->amee->host), $this->config->amee->port);
		socket_write($s, $header, strlen($header));
		
		$lines = array();
		$lines[0] = '';
		$i = 0;
		$xml_line = '';
		while (true) {
			$c = @socket_read($s, 1);
			//echo($c);
			if ($c == "\n" || strlen($c) == 0) {
				//echo($lines[$i]."<br/>\n");
				if(strpos($lines[$i], 'Set-Cookie:') !== false) {
					$this->storeCookie($lines[$i]);
				} elseif(strpos($lines[$i], '<?xml') !== false) {
					$xml_line = $lines[$i];
				}
				if(strlen($c) == 0) {
					break;
				}
				$i++;
				$lines[$i] = "";
			} else {
				$lines[$i] .= $c;
			}
		}
		
		socket_close($s);
		
		if(strpos($lines[0], '401 UNAUTH') !== false){//auth failed, try again, try ONCE at getting new authToken then trying again
			if($repeat){
				$this->debug('Authentication failure - get a new token and try again.');
				$this->reconnect();
				return $this->sendRequest($path, $body, $xml_only, false); //try just one more time!
			} else {
				$this->debug('Authentication failure on second attempt.');
			}
		}
		if($this->debugOn) {
			$this->responses[] = array('request' => $header, 'response' => $lines);
			$this->debug($header);
			$this->debug(implode("\n", $lines));
		}
		if($xml_only) {
			$this->lastresponse = $xml_line;
			return $xml_line;
		} else {
			$this->lastresponse = '';
			return $lines;
		}
	}
	
	/**
	* This function creates a new user profile and returns the 12 digit UID.
	* @return string
	*/
	public function createNewProfile(){
		//It's meaningless to cache this request
		$caching = $this->useCache;
		$this->useCache = false;
		$xmlstr = $this->post('/profiles', array('profile' => 'true'));
		$profileUID = $this->getXmlElement($xmlstr, '/Resources/ProfilesResource/Profile/@uid');
		$this->useCache = $caching; //Restore cache state
		return $profileUID;
	}
	
	/**
	* Clear the cache - call if a profileItem creation fails, also see display_db_cache.php
	* @access protected
	*/
	protected function dbCacheClear() {
		return $this->db->query('DELETE FROM cache');
	}
	
	/**
	* Use the db cache-
	* tries to fetch result from cache first, if not present then sends request and saves xml to cache
	* NOTE: only works with GET requests with no body
	* e.g. $path is /e.g. GET /data/transport/car/generic/drill?, response is the xml
	* @param string $path the path to fetch
	* @return string
	*/
	protected function dbCacheRequest($path) {
		if(!$this->useCache or substr($path, 0, 3) != 'GET') {
			return $this->sendRequest($path, '');
		}
	
		$result = $this->db->fetchOne("SELECT response FROM cache WHERE request = ?", $this->db->quote($path));
	
		if(empty($result)) {
			$ret = $this->sendRequest($path, ''); //Not in cache, so go fetch it from AMEE
			if(!empty($ret)) {
				$query = "INSERT INTO cache SET request = ".$this->db->quote($path1).", response = ".$this->db->quote($ret1, $db).", timestamp = '".gmdate('Y-m-d H:i:s')."'";
				$query .= " ON DUPLICATE KEY UPDATE response = ".$this->db->quote($ret1, $db);
				$this->db->query($query);
				$this->debug("Adding to cache: $path");
			} else {
				$this->debug("Blank response in dbCacheRequest -  not caching.");
			}
		} else {
			$this->debug("Cache hit: $path");
			$ret = $result['response'];
		}
		//Occasionally clear old stuff out of the cache
		if (rand(1,100) == 42) {
			$this->db->delete('cache', 'timestamp < \''.gmdate('Y-m-d H:i:s', strtotime(gmdate('Y-m-d H:i:s').' -1 week'))."'");
		}
		return $ret;
	}
	
	/**
	* If drill down is incomplete, returns an html select of possible drill options
	* if complete, a data item uid is returned.
	* @param string $path the request path
	* @param string $body the request body
	* @return string
	* @todo This does not use the AMEE wrapper functions like {@link get()} yet
	*/
	public function createDropDown($path, $body){
		$xmlstr = $this->dbCacheRequest($path, $body);
		$optionArray = array();
		$optionArray = $this->getXmlElement($xmlstr,"/Resources/DrillDownResource/Choices/Name", true);
		$name = $optionArray[0];
	
		$optionArray = array();
		$optionArray = $this->getXmlElement($xmlstr,"/Resources/DrillDownResource/Choices/Choices/Choice/Value", true);
		if($name=="uid")
			$ret = $optionArray[0];
		else {
			$ret="<input type=\"hidden\" name=\"choice\" value=\"$name\"/>\n";
			$ret.="<select name=\"$name\" class=\"dropdown\" onChange=\"calculatorSubmit()\">\n";
			$ret.="\t<option>Choose $name</option>\n";
			//echo the <option> values from the array, comparing to posted values to get previously SELECTED option.
			$selected = ''; //Where do we get this value from?
			foreach($optionArray as $option) {
				//Is this entry a divider?
				if ($option == '-') {
					$ret .= "\t<option disabled=\"disabled\">--</option>";
					continue;
				}
				$out = htmlentities($option);
				$ret .= "\t<option value=\"$out\"";
				if ($option == $selected) {
					$ret .= ' selected="selected"';
				}
				$ret .= ">$out</option>\n";
			}
			$ret .= "</select>\n";
		}
		return $ret;
	}
	
	/**
	* This function takes an XML string and an xpath route as its parameters and returns the element(s)
	* specified by the xpath route.
	* By default, this function returns a single value. If $array is set to true when calling the function,
	* an array of values is returned.
	* @param string $xmlInput An XML string
	* @param string $xpathRoute An XPath route
	* @param boolean $array Whether to return a single value or an array of 1 or more matched elements
	* @return mixed
	*/
	public function getXmlElement($xmlInput, $xpathRoute, $array = false){
		$xml = simplexml_load_string($xmlInput);
		if($xml === false) {
			return false;
		}
		if(!$array){
			$name = $xml->xpath($xpathRoute);
			if (is_array($name) and count($name) > 0) {
				return $name[0];
			}
			return '';
		} else {
			$optionArray = array();
			$name = $xml->xpath($xpathRoute);
			if(!empty($name)){
				foreach ($name as $name){
					$optionArray[] = $name;
				}
			}
			return $optionArray;
		}
	}
	
	/**
	* A simplifying wrapper for getXmlElement that automatically searches in the last response received
	* Saves creating a local variable to store the XML response in
	* @param string $xpathRoute An XPath route
	* @param boolean $array Whether to return a single value or an array of 1 or more matched elements
	* @return mixed
	*/
	public function searchResponse($xpathRoute, $array = false) {
		if (empty($this->lastresponse)) {
			if ($array) {
				return array();
			} else {
				return '';
			}
		}
		return $this->getXmlElement($this->lastresponse, $xpathRoute, $array);
	}
	
	/**
	* Reverse of {@link parse_str()}
	* Converts an associative array into a URL query string, adding URL encoding where necessary
	* For example array('a' => 'a b c') would return: a=a%20b%20c
	* @param array $params An array of key/value pairs to convert
	* @return string
	*/
	public function makeQueryString($params) {
		$str = '';
		if (count($params) > 0) {
			foreach ($params as $key => $value) {
				$str .= ($str == '')?'':'&'; //Don't start string with an &
				if (rawurlencode($value) != $value) { //Does the param contain url-unsafe chars?
					if (rawurlencode($key) != $key) { //Does the key contain url-unsafe chars?
						$str .= rawurlencode($key).'='.rawurlencode($value);
					} else {
						$str .= $key.'='.rawurlencode($value);
					}
				} else {
					if (rawurlencode($key) != $key) { //Does the key contain url-unsafe chars?
						$str .= rawurlencode($key).'='.$value;
					} else {
						$str .= $key.'='.$value;
					}
				}
			}
		}
		return $str;
	}
	
	/**
	* Log a timestamped message to the debug log
	*/
	protected function debug($msg) {
		if ($this->debugOn) {
			file_put_contents('ameecalc.log', date('Y-m-d H:i:s')."\t$msg\n", FILE_APPEND | LOCK_EX);
		}
	}
}
?>