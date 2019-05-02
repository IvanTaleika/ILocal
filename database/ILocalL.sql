-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: mcuti
-- ------------------------------------------------------
-- Server version	8.0.12

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
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contacts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_type` varchar(255) DEFAULT NULL,
  `contact_value` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf9pc3faerry2wp3xnahv2b0rg` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contacts`
--

LOCK TABLES `contacts` WRITE;
/*!40000 ALTER TABLE `contacts` DISABLE KEYS */;
/*!40000 ALTER TABLE `contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_experience`
--

DROP TABLE IF EXISTS `job_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `job_experience` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(255) DEFAULT NULL,
  `position` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `work_place` varchar(255) DEFAULT NULL,
  `working_period` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9u21gflqu2fu0mekvc77d7k1x` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_experience`
--

LOCK TABLES `job_experience` WRITE;
/*!40000 ALTER TABLE `job_experience` DISABLE KEYS */;
/*!40000 ALTER TABLE `job_experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lang`
--

DROP TABLE IF EXISTS `lang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lang_def` varchar(255) DEFAULT NULL,
  `lang_icon` varchar(255) DEFAULT NULL,
  `lang_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lang`
--

LOCK TABLES `lang` WRITE;
/*!40000 ALTER TABLE `lang` DISABLE KEYS */;
INSERT INTO `lang` VALUES (1,'EN','United-Kingdom.png','English'),(2,'RU','Russia.png','Russian'),(3,'DE','Germany.png','German'),(4,'IT','Italy.png','Italian'),(5,'BE','Belarus.png','Belarusian'),(6,'PL','Poland.png','Polish'),(7,'ES','Spain.png','Spanish'),(8,'PT','Portugal.png','Portugal'),(9,'SR','Serbia.png','Serbian'),(10,'TR','Turkey.png','Turkish');
/*!40000 ALTER TABLE `lang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `project_name` varchar(255) DEFAULT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKte6bms4bq1ixfhn024qtysmcg` (`author_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,'as dfasd f','a sdf',1,NULL,NULL);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_contributor`
--

DROP TABLE IF EXISTS `project_contributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_contributor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9em8yi149o7cwppfpqlkn1e7f` (`user_id`),
  KEY `FK93eqt2uw8vn2uq9f5150fxf12` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_contributor`
--

LOCK TABLES `project_contributor` WRITE;
/*!40000 ALTER TABLE `project_contributor` DISABLE KEYS */;
INSERT INTO `project_contributor` VALUES (1,1,'MODERATOR',2);
/*!40000 ALTER TABLE `project_contributor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_lang`
--

DROP TABLE IF EXISTS `project_lang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_default` bit(1) NOT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `lang_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3cw7jcebeca6rlxtll2p7k0oc` (`lang_id`),
  KEY `FK321wirtmmela0rbli7cntdfa` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_lang`
--

LOCK TABLES `project_lang` WRITE;
/*!40000 ALTER TABLE `project_lang` DISABLE KEYS */;
INSERT INTO `project_lang` VALUES (1,_binary '',1,1),(2,_binary '\0',1,2),(3,_binary '\0',1,5),(4,_binary '\0',1,3),(5,_binary '\0',1,6),(6,_binary '\0',1,8),(7,_binary '\0',1,10);
/*!40000 ALTER TABLE `project_lang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stats`
--

DROP TABLE IF EXISTS `stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `contributor` bit(1) NOT NULL,
  `date` datetime DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stats`
--

LOCK TABLES `stats` WRITE;
/*!40000 ALTER TABLE `stats` DISABLE KEYS */;
INSERT INTO `stats` VALUES (1,'TRANSLATE',_binary '\0','2019-04-30 15:45:05',1,1),(2,'TRANSLATE',_binary '\0','2019-04-30 15:46:27',1,1),(3,'TRANSLATE',_binary '\0','2019-04-30 15:47:57',1,1),(4,'TRANSLATE',_binary '\0','2019-04-30 15:52:02',1,1),(5,'TRANSLATE',_binary '\0','2019-04-30 15:52:11',1,1),(6,'TRANSLATE',_binary '\0','2019-04-30 15:52:12',1,1),(7,'TRANSLATE',_binary '\0','2019-04-30 15:57:28',1,1),(8,'TRANSLATE',_binary '\0','2019-04-30 15:57:29',1,1),(9,'TRANSLATE',_binary '\0','2019-04-30 15:58:04',1,1),(10,'TRANSLATE',_binary '\0','2019-04-30 15:58:06',1,1);
/*!40000 ALTER TABLE `stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `term`
--

DROP TABLE IF EXISTS `term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `term` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) DEFAULT NULL,
  `term_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl5pykcf68mtplrb5tafk3v9ey` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `term`
--

LOCK TABLES `term` WRITE;
/*!40000 ALTER TABLE `term` DISABLE KEYS */;
INSERT INTO `term` VALUES (9,1,'as df');
/*!40000 ALTER TABLE `term` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `term_comment`
--

DROP TABLE IF EXISTS `term_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `term_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `term_id` bigint(20) DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3utw2y3do48emm2xbmir5axno` (`author_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `term_comment`
--

LOCK TABLES `term_comment` WRITE;
/*!40000 ALTER TABLE `term_comment` DISABLE KEYS */;
INSERT INTO `term_comment` VALUES (1,'2019-05-01 21:15:16',9,'ываыва',1),(2,'2019-05-01 21:15:23',9,'ывапваы\n\n',1);
/*!40000 ALTER TABLE `term_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `term_lang`
--

DROP TABLE IF EXISTS `term_lang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `term_lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified_date` datetime DEFAULT NULL,
  `project_lang_id` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `lang_id` bigint(20) DEFAULT NULL,
  `modified_by` bigint(20) DEFAULT NULL,
  `term_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7mayk1s2nwkkpvyv51dkcq5ju` (`lang_id`),
  KEY `FK5xoi0rjuomwcf4om3sjcqj4e6` (`modified_by`),
  KEY `FKt5d6moinlr0je1knsdvvtwigj` (`term_id`),
  KEY `FKgpk8cusc1bmujs0nd9qt87ox7` (`project_lang_id`)
) ENGINE=MyISAM AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `term_lang`
--

LOCK TABLES `term_lang` WRITE;
/*!40000 ALTER TABLE `term_lang` DISABLE KEYS */;
INSERT INTO `term_lang` VALUES (18,'2019-04-30 15:58:06',2,0,'123',2,1,9),(17,'2019-04-30 15:58:04',1,0,'asd f',1,1,9),(19,'2019-05-01 19:14:24',3,0,'',5,1,9),(20,'2019-05-01 19:14:27',4,0,'',3,1,9),(21,'2019-05-01 19:14:35',5,0,'',6,1,9),(22,'2019-05-01 19:14:43',6,0,'',8,1,9),(23,'2019-05-01 19:14:46',7,0,'',10,1,9);
/*!40000 ALTER TABLE `term_lang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activation_code` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `mailing_access` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_photo` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `refresh_token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,'hidumidu@proeasyweb.com','ivan','forever',_binary '\0','4d4538b28ea03889b2b5b1910fd3352c',NULL,'qwerty','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2VydHkiLCJ1c2VySWQiOiIxIiwiZGF0ZSI6MTU1Njc0NjQwMzMzMX0.RkacCQSvCVcOJnq95GTZJq1Px1Bamc2IzPAJOggwaZV-BhghCMhnRmugMa4u0trlOK8RCU4Tdfiw3a7Q9oyPQw'),(2,NULL,'adsffadsn@mail.ru','asdbfi','sdnflnsd',_binary '\0','4d4538b28ea03889b2b5b1910fd3352c','','qwerty1','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2VydHkxIiwidXNlcklkIjoiMiIsImRhdGUiOjE1NTY3NDU4MDQ5Nzh9.daPF8TC_XtqKeM-Yq_4g5YM_jKuZl_ZLiuFLikbFyerJHpPRB-AzPJZ4IocRNN_PNcbvo_tlPZMkDnsKTFoeGw'),(3,NULL,'ivan.foreve@gmail.com','ivan','forever',_binary '\0','4d4538b28ea03889b2b5b1910fd3352c','','qwer','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2VyIiwidXNlcklkIjoiMyIsImRhdGUiOjE1NTY3NDU4OTYzNTR9.hdS9QGVc-GmNghsO6uz4Ymay9CUiwkvWTn7mOM7ek2kQ2Y8sW4PoEZ-373r0c9i3uUBEmsY1_mC5OY-yxWNySQ'),(4,NULL,'ivan.for1@gmail.com','ivan','forever',_binary '\0','4d4538b28ea03889b2b5b1910fd3352c','','qwert','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2VydCIsInVzZXJJZCI6IjQiLCJkYXRlIjoxNTU2NzQ2MjYzMjk1fQ.6I344Q0ubAeMeKsnEhdVbos4uSpwRio-4RhTvUMi9XINrq4O8Z6v_2lN0zAl00yBMu8omgUizzXshLkzEygIUA');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_langs`
--

DROP TABLE IF EXISTS `user_langs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_langs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `experience` int(11) NOT NULL,
  `level` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `lang_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhtujsftv0063sy3r3ssbd2bcf` (`lang_id`),
  KEY `FK290536mjrue1ma7w14aywuejr` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_langs`
--

LOCK TABLES `user_langs` WRITE;
/*!40000 ALTER TABLE `user_langs` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_langs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-02  3:07:59
