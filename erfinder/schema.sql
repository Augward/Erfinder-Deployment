-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: erfinder
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `erfinder`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `erfinder` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `erfinder`;

--
-- Table structure for table `registration`
--

DROP TABLE IF EXISTS `registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registration` (
  `perm` varchar(8) NOT NULL,
  `salt` char(8) DEFAULT NULL,
  `userid` varchar(32) NOT NULL,
  `passhash` binary(32) DEFAULT NULL,
  `firstn` varchar(32) NOT NULL,
  `lastn` varchar(32) DEFAULT NULL,
  `legaln` varchar(128) NOT NULL,
  `dln` int DEFAULT NULL,
  `ssn` int DEFAULT NULL,
  `phone` int DEFAULT NULL,
  `contact` int DEFAULT NULL,
  `email` varchar(128) NOT NULL,
  `addr` varchar(256) NOT NULL,
  `zip` int DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` varchar(32) DEFAULT NULL,
  `secquestion` varchar(64) DEFAULT NULL,
  `secanswer` binary(32) DEFAULT NULL,
  UNIQUE KEY `userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `perm` varchar(8) NOT NULL,
  `salt` char(8) DEFAULT NULL,
  `userid` varchar(32) NOT NULL,
  `passhash` binary(32) DEFAULT NULL,
  `firstn` varchar(32) NOT NULL,
  `lastn` varchar(32) DEFAULT NULL,
  `legaln` varchar(128) NOT NULL,
  `dln` int DEFAULT NULL,
  `ssn` int DEFAULT NULL,
  `phone` int DEFAULT NULL,
  `contact` int DEFAULT NULL,
  `email` varchar(128) NOT NULL,
  `addr` varchar(256) NOT NULL,
  `zip` int DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` varchar(32) DEFAULT NULL,
  `secquestion` varchar(64) DEFAULT NULL,
  `secanswer` binary(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-10 15:19:32
