-- phpMyAdmin SQL Dump
-- version 2.11.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 07, 2008 at 02:30 PM
-- Server version: 5.0.45
-- PHP Version: 5.2.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `ameecalc`
--

-- --------------------------------------------------------

--
-- Table structure for table `ameecalc`
--

CREATE TABLE `ameecalc` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `fb_user_id` varchar(20) character set ascii collate ascii_bin NOT NULL,
  `amee_profile_id` varchar(12) character set ascii collate ascii_bin NOT NULL,
  `timestamp` datetime NOT NULL,
  `country` char(2) character set ascii NOT NULL default 'GB' COMMENT 'ISO 3166-1',
  `gas` int(11) NOT NULL default '0',
  `elec` int(11) NOT NULL default '0',
  `car` int(11) NOT NULL default '0',
  `fly` int(11) NOT NULL default '0',
  `datahash` varchar(32) character set ascii collate ascii_bin NOT NULL,
  `carbonprofile` float NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `fb_user_id` (`fb_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `cache`
--

CREATE TABLE `cache` (
  `path` varchar(320) character set ascii collate ascii_bin NOT NULL,
  `response` text character set utf8 NOT NULL,
  `timestamp` datetime NOT NULL,
  UNIQUE KEY `path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `profileitems`
--

CREATE TABLE `profileitems` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `ameecalc_id` int(10) unsigned NOT NULL,
  `dataitem_uid` varchar(12) character set ascii collate ascii_bin NOT NULL,
  `profileitem_uid` varchar(12) character set ascii collate ascii_bin NOT NULL,
  `inputvalue` float NOT NULL,
  `kgco2permonth` float NOT NULL,
  `name` varchar(20) character set ascii NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniqueitem` (`ameecalc_id`,`name`),
  KEY `ameecalc_id` (`ameecalc_id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `profileitems`
--
ALTER TABLE `profileitems`
  ADD CONSTRAINT `profileitems_ibfk_1` FOREIGN KEY (`ameecalc_id`) REFERENCES `ameecalc` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
