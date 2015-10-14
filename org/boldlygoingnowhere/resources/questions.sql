-- MySQL dump 9.11
--
-- Host: localhost    Database: Trivia
-- ------------------------------------------------------
-- Server version	4.0.24_Debian-10sarge2-log

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `question` text,
  `answer` text,
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;
