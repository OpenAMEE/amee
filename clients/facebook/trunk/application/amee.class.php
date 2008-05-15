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
* @version $Id: amee.class.php 1805 2008-04-11 13:15:48Z marcus $
*/
 
class AMEE {
	 /**
	 * @var Zend_Db $db An instance of a Zend database instance, used for caching
	 * @access protected
	 */
	protected $db;
	 /**
	 * @var string $authtoken The current authentication token value
	 * @access protected
	 */
	protected $authtoken = '';
	 /**
	 * @var integer $authexpires The timestamp for when the current authtoken expires
	 * @access protected
	 */
	protected $authexpires = 0;
	 /**
	 * @var array $responses A history of requests and reponses by this instance
	 * @access protected
	 */
	protected $responses = array();
	 /**
	 * @var Zend_Config $config A Zend_Config instance holding all config info
	 * @access protected
	 */
	protected $config;
	 /**
	 * @var Zend_Session_Namespace $AMEESESSION A Zend Session Namespace for session storage
	 * @access protected
	 */
	protected $AMEESESSION;
	 /**
	 * @var string $lastresponse The last XML response string received from AMEE
	 * @access protected
	 */
	protected $lastresponse = '';
	 /**
	 * @var string $lastrequesttype The type (GET/POST/PUT/DELETE) of the last request sent to AMEE
	 * @access protected
	 */
	protected $lastrequesttype = '';
	 /**
	 * @var array $cookies Cookies relating to the AMEE session
	 * @access protected
	 */
	protected $cookies = array();
	 /**
	 * @var boolean $usecache Whether to use a local cache for AMEE GET requests
	 * @access public
	 */
	public $usecache = false;
	 /**
	 * @var boolean $debugon Whether to log interesting events to amee.log
	 * @access public
	 */
	public $debugon = false;
	 /**
	 * @var string $authmode Can be 'session' (i.e. each client session gets their own auth) or 'global', where same auth key is shared for all sesions (via files or APC vars). The latter is more efficient, will make fewer login requests. Normally set from config.
	 * @access public
	 */
	public $authmode = 'global';
	 /**
	 * @var string $appname If more than 1 app uses this class and you have authmode == global, set the app name to keep their auth tokens separate. Normally set from config.
	 * @access public
	 */
	public $appname = '';
	
	/**
	* Initialise this AMEE instance
	* @param Zend_Config $config A Zend_Config instance containing the config elements we want, so config is integrated with host app
	*/
	public function __construct(Zend_Config $config) {
		$this->config = $config;

		//Connect to DB
		require_once 'Zend/Db.php';
		$this->db = Zend_Db::factory($this->config->amee->database->adapter, array(
			'host' => $this->config->amee->database->host,
			'username' => $this->config->amee->database->username,
			'password' => $this->config->amee->database->password,
			'dbname' => $this->config->amee->database->name,
			'port' => $this->config->amee->database->port
		));
		$this->db->setFetchMode(Zend_Db::FETCH_ASSOC);
		
		//Check for optional config items
		if (isset($this->config->amee->usecache)) {
			$this->usecache = ($this->config->amee->usecache == true or $this->config->amee->usecache == 'true');
		}
		if (isset($this->config->amee->debugon)) {
			$this->debugon = ($this->config->amee->debugon == true or $this->config->amee->debugon == 'true');
		}
		if (isset($this->config->amee->authmode) and ($this->config->amee->authmode == 'global' or $this->config->amee->authmode == 'session')) {
			$this->authmode = $this->config->amee->authmode;
		}
		if (isset($this->config->amee->appname)) {
			$this->appname = $this->config->amee->appname;
		}

		//Create a session namespace just for us, but note that we don't start a session, that's up to the host app
		require_once 'Zend/Session/Namespace.php';
		$this->AMEESESSION = new Zend_Session_Namespace('AMEE');
	}
	
	/**
	* This function authenticates with AMEE - this needs to be done  once per session and
	* returns an authtoken which is passed back to AMEE in all subsequent requests.
	* Throws an exception if authenticaton fails
	* Connection parameters come from the config submitted to the constructor
	* @return boolean
	*/
	public function connect() {
		//Are we already authenticated with an unexpired key?
		if (!empty($this->authtoken) and $this->authexpires > time()) {
			return true;
		}
		//Did we already connect in an earlier request and store it in the session?
		if (isset($this->AMEESESSION->authtoken) and !empty($this->AMEESESSION->authtoken) and $this->AMEESESSION->authexpires > time()) {
			$this->authtoken = $this->AMEESESSION->authtoken;
			$this->authexpires = $this->AMEESESSION->authexpires;
			return true;
		}
		//We don't have an auth key in the session
		if ($this->authmode == 'global') {
			if (extension_loaded('apc')) { //We have APC, so look up auth in there first
				$cachedauth = apc_fetch(array('AMEE_authtoken_'.$this->appname, 'AMEE_authexpires_'.$this->appname));
				if (!empty($cachedauth)) {
					if (array_key_exists('AMEE_authtoken_'.$this->appname, $cachedauth) and !empty($cachedauth['AMEE_authtoken_'.$this->appname])) {
						$this->authtoken = $cachedauth['AMEE_authtoken_'.$this->appname];
						if (array_key_exists('AMEE_authexpires_'.$this->appname, $cachedauth)) {
							$this->authexpires = $cachedauth['AMEE_authexpires_'.$this->appname];
						}
						if ($this->authexpires > time()) { //It's still valid, so no further action needed
							return true;
						} else { //Expired, so clear auth and cache
							$this->authtoken = '';
							apc_delete('AMEE_authtoken_'.$this->appname);
							apc_delete('AMEE_authexpires_'.$this->appname);
						}
					}
				}
			} else { //Fall back to files
				$filedir = dirname(__FILE__).DIRECTORY_SEPARATOR;
				if (file_exists($filedir.'AMEE_auth_cache_'.$this->appname)) {
					$cachedauth = file_get_contents($filedir.'AMEE_auth_cache_'.$this->appname);
					list($token, $expires) = split($cachedauth, ',');
					if (!empty($token) and $expires > time()) {
						$this->authtoken = $token;
						$this->authexpires = $expires;
						return true;
					} else {
						if (is_writable($filedir.'AMEE_auth_cache_'.$this->appname)) {
							unlink($filedir.'AMEE_auth_cache_'.$this->appname);
						}
					}
				}
			}
		}
		//Explicitly call sendRequest as we want to get our hands on the headers
		$lines = $this->sendRequest('POST /auth', http_build_query(array('username' => $this->config->amee->username, 'password' => $this->config->amee->password), NULL, '&'), false, false);
	
		foreach ($lines as $line) {
			$matches = array();
			if (preg_match('/^authToken: (.*)$/', $line, $matches)) {
				$this->authtoken = trim($matches[1]);
				$this->AMEESESSION->authtoken = $this->authtoken;
				//AMEE sessions time out after 30 mins idle, so preemptively expire our auth before that
				$this->authexpires = strtotime('+29 minutes', time());
				$this->AMEESESSION->authexpires = $this->authexpires;
				if ($this->authmode == 'global') {
					if (extension_loaded('apc')) { //Store in global cache for allowed expiry time
						apc_store('AMEE_authtoken_'.$this->appname, $this->authtoken, 29 * 60);
						apc_store('AMEE_authexpires_'.$this->appname, $this->authexpires, 29 * 60);
					} else {
						file_put_contents(dirname(__FILE__).DIRECTORY_SEPARATOR.'AMEE_auth_cache_'.$this->appname, $this->authtoken.','.$this->authexpires);
					}
				}
				return true; //Don't bother looking at any other headers
			}
		}
		if(empty($this->authtoken)) {
			$this->debug('AMEE authentication failed');
		}
		throw new Exception('AMEE authentication failed');
		return false;
	}
	
	/**
	* Trash auth token and session
	*/
	public function disconnect() {
		$this->authtoken = '';
		$this->authexpires = 0;
		$this->AMEESESSION->authtoken = '';
		$this->AMEESESSION->authexpires = 0;
		if ($this->authmode == 'global') {
			if (extension_loaded('apc')) { //Trash auth from cache too
				apc_delete('AMEE_authtoken_'.$this->appname);
				apc_delete('AMEE_authexpires_'.$this->appname);
			} else {
				$filedir = dirname(__FILE__).DIRECTORY_SEPARATOR;
				if (file_exists($filedir.'AMEE_auth_cache_'.$this->appname) and is_writable($filedir.'AMEE_auth_cache_'.$this->appname)) {
					unlink($filedir.'AMEE_auth_cache_'.$this->appname);
				}
			}
		}
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
		return $this->sendRequest("POST $path", http_build_query($params, NULL, '&'));
	}

	/**
	* Wrapper to simplify AMEE PUT requests
	* @param string $path The query path, such as "/auth"
	* @param array $params Associative array of parameters
	*/
	public function put($path, $params = array()) {
		return $this->sendRequest("PUT $path", http_build_query($params, NULL, '&'));
	}

	/**
	* Wrapper to simplify AMEE GET requests
	* @param string $path The query path, such as "/auth"
	* @param array $params Associative array of parameters
	*/
	public function get($path, $params = array()) {
		if (count($params) > 0) {
			return $this->sendRequestCached("GET $path?".http_build_query($params, NULL, '&'));
		} else {
			return $this->sendRequestCached("GET $path");
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
		if(!empty($this->authtoken) && strpos($path, 'POST /auth') === false){
			$this->connect();
		}
		$matches = array();
		if (preg_match('/^(GET|POST|PUT|DELETE)/', $path, $matches)) {
			$this->lastrequesttype = $matches[1];
		} else {
			$this->debug('Invalid path requested: '.$path);
		}
		$header = $path." HTTP/1.0\n"
			.$this->getCookieLines() //insert cookies
			."Accept: application/xml\n";
		if(!empty($this->authtoken)) {
			$header.="authToken: ".$this->authtoken."\n";
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
		
		if(strpos($lines[0], '401 UNAUTH') !== false){ //Auth failed, try again, try ONCE at getting new authtoken then trying again
			if($repeat){
				$this->debug('Authentication failure - get a new token and try again.');
				$this->reconnect();
				return $this->sendRequest($path, $body, $xml_only, false); //Try one more time!
			} else {
				$this->debug('Authentication failure on second attempt.');
			}
		}
		if($this->debugon) {
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
		$xmlstr = $this->post('/profiles', array('profile' => 'true'));
		$profileUID = (string)$this->getXmlElement($xmlstr, '/Resources/ProfilesResource/Profile/@uid');
		return $profileUID;
	}
	
	/**
	* Clear the cache
	* @access public
	*/
	public function clearCache() {
		return $this->db->query('DELETE FROM cache');
	}
	
	/**
	* Do an AMEE request but check the cache first
	* Tries to fetch result from cache first, if not present then sends request and saves xml to cache
	* Stores a hash of the request rather than the whole request
	* Uses a 1-week timeout, purges old items on average every 100 requests
	* NOTE: only works with GET requests with no body
	* e.g. $path is e.g. GET /data/transport/car/generic/drill?, response is the xml
	* @param string $path the path to fetch
	* @return string
	*/
	protected function sendRequestCached($path) {
		//Don't cache if debug is on or cache is off or it's not a GET request
		if(!$this->usecache or substr($path, 0, 3) != 'GET') {
			return $this->sendRequest($path, '');
		}
		
		//TODO implement APC caching
		
		$response = $this->db->fetchOne("SELECT response FROM cache WHERE request = ?", md5($path));
	
		if(empty($response)) {
			$response = $this->sendRequest($path, ''); //Not in cache, so go fetch it from AMEE
			if(!empty($response)) {
				$query = "INSERT INTO cache SET request = '".md5($path)."', response = ".$this->db->quote($response).", timestamp = '".gmdate('Y-m-d H:i:s')."'";
				$query .= " ON DUPLICATE KEY UPDATE response = ".$this->db->quote($response).", timestamp = '".gmdate('Y-m-d H:i:s')."'";
				$this->db->query($query);
				$this->debug("Adding to cache: $path");
			} else {
				$this->debug("Blank response in sendRequestCached -  not caching.");
			}
		} else {
			$this->debug("Cache hit: $path");
		}
		//Occasionally clear old stuff out of the cache
		if (rand(1,100) == 42) {
			$this->db->delete('cache', 'timestamp < \''.gmdate('Y-m-d H:i:s', strtotime(gmdate('Y-m-d H:i:s').' -1 week'))."'");
		}
		$this->lastresponse = $response;
		return $response;
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
		$xmlstr = $this->sendRequestCached($path, $body);
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
	* Converts all found elements to strings
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
		$xml = $this->getXmlElement($this->lastresponse, $xpathRoute, $array);
		if ($array) {
			$result = array();
			foreach($xml as $res) {
				$result[] = (string)$res;
			}
			return $result;
		} else {
			return (string)$xml;
		}
	}
	
	/**
	* A simplifying wrapper to extract the kgco2value out of the last response
	* Only use when you expec the previous request to return an AmountPerMonth value!
	* @return float
	*/
	public function getKgCo2() {
		if (!empty($this->lastresponse)) {
			if ($this->lastrequesttype == 'POST') {
				return (float)$this->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/AmountPerMonth');
			} else {
				return (float)$this->searchResponse('/Resources/ProfileItemResource/ProfileItem/AmountPerMonth');
			}
		} else {
			return 0.0;
		}
	}

	/**
	* A simplifying wrapper to extract a new profileitem UID out of the last POST response
	* Only use when you expec the previous request to return a profileItem UID!
	* @return float
	*/
	public function getProfileItemUID() {
		if (!empty($this->lastresponse)) {
			return (string)$this->searchResponse('/Resources/ProfileCategoryResource/ProfileItem/@uid');
		} else {
			return '';
		}
	}

	/**
	* A simplifying wrapper to extract the dataitem UID out of the last drill response
	* Only use when you expec the previous request to return a dataItem UID!
	* @return float
	*/
	public function getDataItemUID() {
		if (!empty($this->lastresponse)) {
			return (string)$this->searchResponse("/Resources/DrillDownResource/Choices[Name='uid']/Choices/Choice/Value");
		} else {
			return '';
		}
	}

	/**
	* Log a timestamped message to the debug log
	*/
	protected function debug($msg) {
		if ($this->debugon) {
			file_put_contents(dirname(__FILE__).DIRECTORY_SEPARATOR.'amee.log', date('Y-m-d H:i:s')."\t$msg\n", FILE_APPEND | LOCK_EX);
		}
	}
}
?>