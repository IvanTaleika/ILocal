-- MySQL dump 10.13  Distrib 8.0.14, for Win64 (x86_64)
--
-- Host: 172.20.11.243    Database: mcuti
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activation_code` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `mailing_access` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_photo` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5594 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'ca5593fe-d7e8-4838-9456-d75556c09cca',NULL,'desinger-sasa@mail.ru','Alex','Klimets',_binary '\0','71f27518155a8ce408f2e967a134a476',NULL,'qwerty'),(5547,'6084f4e5-e8f7-454d-8108-9f0511a853b1',NULL,'ivan.forever91@gmail.com','Ivan','Chyzhik',_binary '\0','71f27518155a8ce408f2e967a134a476',NULL,'qwerty1'),(5559,'24a23fa3-d3d0-443a-a8ff-065fcaae6724',NULL,'ivan.forever91@gmail.com',NULL,NULL,_binary '\0','71f27518155a8ce408f2e967a134a476',NULL,'1'),(5560,'1e42a97b-090b-4179-be0d-ca6eb9e96c18',NULL,'ivan.forever91@gmail.com',NULL,NULL,_binary '\0','b1d4ca9d11ca3752673a99bef660e144',NULL,'2'),(5561,'7b13e510-3bb3-480c-b8ef-d8b1775fe96f',NULL,'ivan.forever91@gmail.com','3','3',_binary '\0','3a2a9936daf7ef6823a3f4d3d7ce7da0',NULL,'3'),(5593,'0b252b30-6b06-44ce-b62c-aa6b96a7eac5',NULL,'ivan.forever91@gmail.com','1','1',_binary '\0','b1d4ca9d11ca3752673a99bef660e144',NULL,'6');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-01-24 16:27:38
