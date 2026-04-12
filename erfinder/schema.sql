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
-- Table structure for table `facilities`
--

DROP TABLE IF EXISTS `facilities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facilities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `er_name` varchar(255) NOT NULL,
  `phonenum` varchar(20) NOT NULL,
  `address` varchar(255) NOT NULL,
  `zip` varchar(10) NOT NULL,
  `trauma_level` char(5) NOT NULL,
  `specialties` text NOT NULL,
  `bed_availability` int NOT NULL,
  `waitTime_Minutes` int NOT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `facilities_chk_1` CHECK ((`trauma_level` in (_utf8mb4'I',_utf8mb4'II',_utf8mb4'III',_utf8mb4'IV',_utf8mb4'V'))),
  CONSTRAINT `facilities_chk_2` CHECK ((`bed_availability` >= 0)),
  CONSTRAINT `facilities_chk_3` CHECK ((`waitTime_Minutes` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurances`
--

DROP TABLE IF EXISTS `insurances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurances` (
  `member_id` varchar(32) NOT NULL,
  `policy_num` int DEFAULT NULL,
  `group_num` int DEFAULT NULL,
  `plan_type` char(3) NOT NULL DEFAULT '--',
  `rx_bin` varchar(16) DEFAULT NULL,
  `rx_pcn` varchar(16) DEFAULT NULL,
  `rx_group` varchar(16) DEFAULT NULL,
  `rx_id` varchar(16) DEFAULT NULL,
  `customer_service_phone` varchar(45) DEFAULT NULL,
  `effective_date` date DEFAULT NULL,
  `insurer_name` varchar(64) NOT NULL,
  `userid` varchar(32) NOT NULL,
  `copay` decimal(10,2) NOT NULL DEFAULT '0.00',
  `coinsurance` decimal(2,2) NOT NULL DEFAULT '0.00',
  `out_of_pocket_max` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`member_id`,`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registration`
--

DROP TABLE IF EXISTS `registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registration` (
  `id` int NOT NULL AUTO_INCREMENT,
  `perm` varchar(8) NOT NULL,
  `salt` char(8) DEFAULT NULL,
  `userid` varchar(32) NOT NULL,
  `passhash` binary(32) DEFAULT NULL,
  `firstn` varchar(32) NOT NULL,
  `lastn` varchar(32) DEFAULT NULL,
  `legaln` varchar(128) NOT NULL,
  `dln` varchar(20) DEFAULT NULL,
  `ssn` varchar(20) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `email` varchar(128) NOT NULL,
  `addr` varchar(256) NOT NULL,
  `zip` varchar(20) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` varchar(32) DEFAULT NULL,
  `secquestion` varchar(64) DEFAULT NULL,
  `secanswer` binary(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userid` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `perm` varchar(8) NOT NULL,
  `salt` char(8) DEFAULT NULL,
  `userid` varchar(32) NOT NULL,
  `passhash` binary(32) DEFAULT NULL,
  `firstn` varchar(32) NOT NULL,
  `lastn` varchar(32) DEFAULT NULL,
  `legaln` varchar(128) NOT NULL,
  `dln` varchar(20) DEFAULT NULL,
  `ssn` varchar(20) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `email` varchar(128) NOT NULL,
  `addr` varchar(256) NOT NULL,
  `zip` varchar(20) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` varchar(32) DEFAULT NULL,
  `secquestion` varchar(64) DEFAULT NULL,
  `secanswer` binary(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userid` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'erfinder'
--
/*!50003 DROP PROCEDURE IF EXISTS `add_facility` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `add_facility`(
    IN p_name VARCHAR(255),
    IN p_phone VARCHAR(20),
    IN p_address VARCHAR(255),
    IN p_zip VARCHAR(10),
    IN p_trauma_level CHAR(5),
    IN p_specialties TEXT,
    IN p_beds INT,
    IN p_wait_time INT,
    IN p_lat DECIMAL(9,6),
    IN p_lng DECIMAL(9,6)
)
BEGIN
    INSERT INTO facilities (
        er_name,
        phonenum,
        address,
        zip,
        trauma_level,
        specialties,
        bed_availability,
        waitTime_Minutes,
        latitude,
        longitude
    )
    VALUES (
        p_name,
        p_phone,
        p_address,
        p_zip,
        p_trauma_level,
        p_specialties,
        p_beds,
        p_wait_time,
        p_lat,
        p_lng
    );
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CreateTestUser` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CreateTestUser`(
	IN tuserid varchar(32),
    IN tuserpass varchar(32),
    IN tusersecq varchar(64),
    IN tuserseca varchar(32)
)
BEGIN
	DECLARE _salt varchar(16);
	DECLARE _hashed binary(32);
    DECLARE _hashsq binary(32);
    SELECT SUBSTR(MD5(RAND()), 1, 8) INTO _salt;
    CALL erfinder.GetHashedVersion(tuserpass, _salt, _hashed);
    CALL erfinder.GetHashedVersion(tuserseca, _salt, _hashsq);
    
    INSERT INTO users (
		userid, 
        passhash, 
        salt, 
        perm,
        firstn,
        lastn,
        legaln,
        email,
        addr,
        secquestion,
        secanswer
	)
    VALUES (
        tuserid,
        _hashed,
        _salt,
        'test',
        '_testFN',
        '_testLN',
        '_testLegal',
        '_testEmail',
        '_testAddr',
        tusersecq,
        _hashsq
    );
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `GetHashedVersion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetHashedVersion`(
	IN pass varchar(32),
    IN salt char(8),
    OUT hashed binary(32)
)
BEGIN
	SET hashed = UNHEX(SHA2(CONCAT(pass, salt),256));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `GetSecurityQuestion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetSecurityQuestion`(
	IN tuserid varchar(32)
)
BEGIN
	SELECT secquestion FROM users WHERE userid = tuserid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PurgeTestUsers` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `PurgeTestUsers`()
BEGIN
	DELETE FROM users 
    WHERE perm='test';
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateWaitTime` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateWaitTime`(
    IN p_facility_id INT,
    IN p_wait_time INT)
BEGIN
    UPDATE facilities
    SET waitTime_Minutes = p_wait_time
    WHERE id = p_facility_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `update_facility` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_facility`(
	IN p_facility_id INT,
    IN p_beds INT,
    IN p_wait_time INT)
BEGIN
		UPDATE facilities
        SET bed_availability = p_beds,
			waitTime_Minutes = p_wait_time
		WHERE id = p_facility_id;
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `ValidatePassword` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `ValidatePassword`(
    IN for_userid varchar(32),
    IN to_validate varchar(32)
)
BEGIN
    DECLARE db_salt char(8);
    DECLARE db_pass binary(32);
    DECLARE _hashed binary(32);
    DECLARE db_status varchar(16);

    SELECT salt, passhash, status INTO db_salt, db_pass, db_status
    FROM Users WHERE userid = for_userid;
    CALL GetHashedVersion(to_validate, db_salt, _hashed);
    SELECT (_hashed = db_pass) AS is_valid, db_status AS status;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `ValidateSecurity` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `ValidateSecurity`(
	IN for_userid varchar(32),
    IN to_validate varchar(32)
)
BEGIN
	DECLARE db_salt char(8);
    DECLARE db_ans binary(32);
    DECLARE _hashsq binary(32);
	SELECT salt, secanswer INTO db_salt, db_ans
		FROM users WHERE userid = for_userid;
    CALL GetHashedVersion(to_validate, db_salt, _hashsq);
    SELECT _hashsq = db_ans;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `ValidateSecurityAnswer` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `ValidateSecurityAnswer`(
	IN for_userid varchar(32),
    IN to_validate varchar(32)
)
BEGIN
	DECLARE db_salt char(8);
    DECLARE db_ans binary(32);
    DECLARE _hashsq binary(32);
	SELECT salt, secanswer INTO db_salt, db_ans
		FROM users WHERE userid = for_userid;
    CALL GetHashedVersion(to_validate, db_salt, _hashsq);
    SELECT _hashsq = db_ans;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `ValidateSecurityQuestion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `ValidateSecurityQuestion`(
	IN for_userid varchar(32),
    IN to_validate varchar(32)
)
BEGIN
	DECLARE db_salt char(8);
    DECLARE db_ans binary(32);
    DECLARE _hashsq binary(32);
	SELECT salt, secanswer INTO db_salt, db_ans
		FROM users WHERE userid = for_userid;
    CALL GetHashedVersion(to_validate, db_salt, _hashsq);
    SELECT _hashsq = db_ans;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-12 10:07:34
