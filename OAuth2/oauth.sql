-- MySQL dump 10.17  Distrib 10.3.12-MariaDB, for osx10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: oauth2
-- ------------------------------------------------------
-- Server version	10.3.12-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


DROP SCHEMA IF EXISTS oauth2;
create schema oauth2 collate utf8mb4_general_ci;

USE oauth2;

--
-- Table structure for table `oauth_authorization`
--

DROP TABLE IF EXISTS `oauth_authorization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_authorization` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `state` varchar(256) NOT NULL,
  `authorization` varchar(256) DEFAULT NULL,
  `gAuthorization` varchar(256) DEFAULT NULL,
  `client_uuid` varchar(255) NOT NULL,
  `redirect_uuid` varchar(255) NOT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_authorization_key` (`authorization`),
  KEY `authorization_redirect_fk` (`redirect_uuid`),
  KEY `authorization_client_fk` (`client_uuid`),
  CONSTRAINT `authorization_client_fk` FOREIGN KEY (`client_uuid`) REFERENCES `oauth_client` (`uuid`) ON UPDATE CASCADE,
  CONSTRAINT `authorization_redirect_fk` FOREIGN KEY (`redirect_uuid`) REFERENCES `oauth_redirect` (`uuid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_authorization`
--


--
-- Table structure for table `oauth_client`
--

DROP TABLE IF EXISTS `oauth_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_client` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `fullName` varchar(255) NOT NULL,
  `cEmail` varchar(255) NOT NULL,
  `verifiedCEmail` tinyint(1) DEFAULT 1,
  `client_id` varchar(255) NOT NULL,
  `client_secret` varchar(255) NOT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_uuid_key` (`uuid`),
  UNIQUE KEY `oauth_email_key` (`cEmail`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_client`
--

--
-- Table structure for table `oauth_client_redirects`
--

DROP TABLE IF EXISTS `oauth_client_redirects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_client_redirects` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `client_uuid` varchar(255) NOT NULL,
  `redirect_uuid` varchar(255) NOT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`client_uuid`,`redirect_uuid`),
  UNIQUE KEY `oauth_uuid_key` (`uuid`),
  KEY `oauth_id_key` (`id`),
  KEY `cr_client_fk` (`client_uuid`),
  KEY `cr_redirect_fk` (`redirect_uuid`),
  CONSTRAINT `cr_client_fk` FOREIGN KEY (`client_uuid`) REFERENCES `oauth_client` (`uuid`) ON UPDATE CASCADE,
  CONSTRAINT `cr_redirect_fk` FOREIGN KEY (`redirect_uuid`) REFERENCES `oauth_redirect` (`uuid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_client_redirects`
--

--
-- Table structure for table `oauth_google`
--

DROP TABLE IF EXISTS `oauth_google`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_google` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `googleId` varchar(256) NOT NULL,
  `gAccessToken` varchar(256) NOT NULL,
  `gRefreshToken` varchar(256) NOT NULL,
  `gExpirationDate` datetime DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_uuid_key` (`uuid`),
  UNIQUE KEY `oauth_gAccess_key` (`gAccessToken`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_google`
--

--
-- Table structure for table `oauth_redirect`
--

DROP TABLE IF EXISTS `oauth_redirect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_redirect` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_uuid_key` (`uuid`),
  UNIQUE KEY `oauth_url_key` (`url`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_redirect`
--

--
-- Table structure for table `oauth_token`
--

DROP TABLE IF EXISTS `oauth_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_token` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `google_uuid` varchar(255) DEFAULT NULL,
  `user_uuid` varchar(255) NOT NULL,
  `client_uuid` varchar(255) NOT NULL,
  `access_token` varchar(256) DEFAULT NULL,
  `refresh_token` varchar(256) DEFAULT NULL,
  `expirationDate` datetime DEFAULT NULL,
  `roles` varchar(256) DEFAULT NULL,
  `expired` tinyint(1) DEFAULT 1,
  `locked` tinyint(1) DEFAULT 1,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_access_key` (`access_token`),
  KEY `token_google_fk` (`google_uuid`),
  KEY `token_user_fk` (`user_uuid`),
  KEY `token_client_fk` (`client_uuid`),
  CONSTRAINT `oauth_token_oauth_google_uuid_fk` FOREIGN KEY (`google_uuid`) REFERENCES `oauth_google` (`uuid`) ON UPDATE CASCADE,
  CONSTRAINT `token_client_fk` FOREIGN KEY (`client_uuid`) REFERENCES `oauth_client` (`uuid`) ON UPDATE CASCADE,
  CONSTRAINT `token_user_fk` FOREIGN KEY (`user_uuid`) REFERENCES `oauth_user` (`uuid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_token`
--


--
-- Table structure for table `oauth_user`
--

DROP TABLE IF EXISTS `oauth_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `verifiedEmail` tinyint(1) DEFAULT 1,
  `password` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `pictureURL` varchar(255) NOT NULL,
  `locale` varchar(255) NOT NULL,
  `familyName` varchar(255) NOT NULL,
  `givenName` varchar(255) NOT NULL,
  `scopesUpdated` tinyint(1) DEFAULT 1,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_uuid_key` (`uuid`),
  UNIQUE KEY `oauth_email_key` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_user`
--

--
-- Table structure for table `oauth_user_authorization`
--

DROP TABLE IF EXISTS `oauth_user_authorization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_user_authorization` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `authorization` int(11) unsigned DEFAULT NULL,
  `user` int(11) unsigned DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dateCreated` datetime DEFAULT current_timestamp(),
  `dateModified` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `oauth_user_authorization_uuid_uindex` (`uuid`),
  KEY `oauth_user_authorization_oauth_user_id_fk` (`user`),
  KEY `oauth_user_authorization_oauth_authorization_id_fk` (`authorization`),
  CONSTRAINT `oauth_user_authorization_oauth_authorization_id_fk` FOREIGN KEY (`authorization`) REFERENCES `oauth_authorization` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `oauth_user_authorization_oauth_user_id_fk` FOREIGN KEY (`user`) REFERENCES `oauth_user` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_user_authorization`
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-02-26 11:43:09
