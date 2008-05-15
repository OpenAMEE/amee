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
* @version $Id: index.php 1804 2008-04-09 23:55:48Z marcus $
*/

//NOT FOR PRODUCTION
error_reporting(E_ALL|E_STRICT);

define('BASE_PATH', dirname(dirname(__FILE__)));
define('APP_PATH', BASE_PATH . DIRECTORY_SEPARATOR . 'application');

require_once 'Zend/Loader.php';
Zend_Loader::registerAutoload();

$viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
$viewRenderer->initView();
$viewRenderer->view->addHelperPath('Zend/View/Helper');

//Get config
$config = new Zend_Config_Ini(APP_PATH . DIRECTORY_SEPARATOR . 'ameecalc.ini', 'production');

Zend_Layout::startMvc(array('layoutPath' => APP_PATH . DIRECTORY_SEPARATOR . 'layouts'));

$frontController = Zend_Controller_Front::getInstance();
//NOT FOR PRODUCTION
$frontController->throwExceptions(true); 
$frontController->setControllerDirectory(APP_PATH . DIRECTORY_SEPARATOR . 'controllers');
$frontController->dispatch();
?>