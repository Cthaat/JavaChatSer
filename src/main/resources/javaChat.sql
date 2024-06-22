-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: javachat
-- ------------------------------------------------------
-- Server version	8.0.36

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
-- Table structure for table `p2p_relationship`
--

DROP TABLE IF EXISTS `p2p_relationship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `p2p_relationship` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户',
  `friend_name` varchar(50) NOT NULL COMMENT '好友',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `p2p_relationship`
--

LOCK TABLES `p2p_relationship` WRITE;
/*!40000 ALTER TABLE `p2p_relationship` DISABLE KEYS */;
INSERT INTO `p2p_relationship` VALUES (29,'admin','testUser3'),(30,'testUser3','admin');
/*!40000 ALTER TABLE `p2p_relationship` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `p2p_text`
--

DROP TABLE IF EXISTS `p2p_text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `p2p_text` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息id',
  `send_user_name` varchar(50) NOT NULL COMMENT '发消息的人',
  `recive_user_name` varchar(50) NOT NULL COMMENT '收消息的人',
  `text` text COMMENT '消息内容',
  `date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户对话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `p2p_text`
--

LOCK TABLES `p2p_text` WRITE;
/*!40000 ALTER TABLE `p2p_text` DISABLE KEYS */;
INSERT INTO `p2p_text` VALUES (1,'admin','testUser3','Hello World','2024-06-14 13:45:57'),(2,'admin','testUser3','Hello World','2024-06-14 13:46:03'),(3,'admin','testUser3','Hello World','2024-06-14 13:46:03'),(4,'admin','testUser3','Hello World','2024-06-14 13:46:04'),(5,'admin','testUser3','Hello World','2024-06-14 13:46:04'),(6,'admin','testUser3','Hello World','2024-06-14 13:46:05'),(7,'admin','testUser3','Hello World','2024-06-14 13:46:05'),(8,'admin','testUser3','Hello World','2024-06-14 13:46:06'),(9,'admin','testUser3','Hello World','2024-06-14 13:46:06'),(10,'admin','testUser3','Hello World','2024-06-14 13:46:07'),(11,'admin','testUser3','Hello World','2024-06-14 13:46:07'),(12,'admin','testUser3','Hello World','2024-06-14 13:46:08'),(13,'admin','testUser3','Hello World','2024-06-14 13:46:08'),(14,'admin','testUser3','Hello World','2024-06-14 13:46:10'),(15,'admin','testUser3',NULL,'2024-06-16 16:47:49'),(16,'admin','testUser3',NULL,'2024-06-16 16:48:45'),(17,'admin','testUser3',NULL,'2024-06-16 16:51:39'),(18,'admin','testUser3',NULL,'2024-06-16 16:53:07'),(19,'admin','testUser3','123','2024-06-16 16:53:25'),(20,'admin','testUser3','hello','2024-06-16 17:03:28'),(21,'testUser3','admin','hello','2024-06-16 17:18:13'),(22,'admin','testUser3','hello1','2024-06-16 17:28:05'),(23,'testUser3','admin','hello2','2024-06-16 17:28:10'),(24,'admin','testUser3','123','2024-06-17 19:12:31'),(25,'testUser3','admin','123','2024-06-17 19:12:34'),(26,'admin','testUser3','123','2024-06-17 19:18:54'),(27,'admin','testUser3','123','2024-06-17 19:19:02'),(28,'admin','testUser3','hellp','2024-06-17 19:20:04'),(29,'admin','testUser3','123','2024-06-17 19:20:57'),(30,'admin','testUser3','123','2024-06-17 19:21:52'),(31,'admin','testUser3','123','2024-06-17 19:24:48'),(32,'admin','testUser3','123','2024-06-17 19:25:44'),(33,'admin','testUser3','123','2024-06-17 19:29:34'),(34,'admin','testUser3','123','2024-06-17 19:49:14'),(35,'admin','testUser3','123','2024-06-17 19:50:09'),(36,'testUser3','admin','456','2024-06-17 19:50:16'),(37,'admin','testUser3','123','2024-06-17 19:51:58'),(38,'admin','testUser3','123','2024-06-17 19:54:11'),(39,'admin','testUser3','123','2024-06-20 13:26:47'),(40,'admin','testUser3','123','2024-06-20 13:29:15'),(41,'admin','testUser3','3','2024-06-20 13:29:16'),(42,'admin','testUser3','123','2024-06-20 13:31:42'),(43,'admin','testUser3','123456','2024-06-20 13:34:27'),(44,'admin','testUser3','123','2024-06-20 13:36:24'),(45,'admin','testUser3','123','2024-06-20 13:36:52'),(46,'admin','testUser3','123','2024-06-20 13:37:00'),(47,'admin','testUser3','123','2024-06-20 13:37:44'),(48,'admin','testUser3','123456','2024-06-20 13:45:12'),(49,'admin','testUser3','123123','2024-06-20 13:45:28'),(50,'admin','testUser3','123456','2024-06-20 13:48:18'),(51,'testUser3','admin','123','2024-06-20 13:48:53'),(52,'testUser3','admin','123','2024-06-20 13:50:33'),(53,'testUser3','admin','123','2024-06-20 14:05:21'),(54,'testUser3','admin','6666','2024-06-20 14:51:15'),(55,'admin','testUser3','999','2024-06-21 14:17:29'),(56,'admin','testUser3','3666','2024-06-22 13:18:08');
/*!40000 ALTER TABLE `p2p_text` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `public_chat_room`
--

DROP TABLE IF EXISTS `public_chat_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `public_chat_room` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息条数',
  `send_user_name` varchar(50) DEFAULT NULL COMMENT '发送用户用户名',
  `send_user_id` int DEFAULT NULL COMMENT '发送用户ID',
  `text` text NOT NULL COMMENT '内容',
  `date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `public_chat_room`
--

LOCK TABLES `public_chat_room` WRITE;
/*!40000 ALTER TABLE `public_chat_room` DISABLE KEYS */;
INSERT INTO `public_chat_room` VALUES (1,'admin',10000001,'Hello World','2024-06-13 20:46:45'),(2,'admin',10000001,'Hello World','2024-06-13 22:34:46'),(3,'admin',10000001,'Hello World','2024-06-13 22:34:46'),(4,'admin',10000001,'Hello World','2024-06-13 22:34:46'),(5,'admin',10000001,'Hello World','2024-06-13 22:34:47'),(6,'admin',10000001,'Hello World','2024-06-13 22:34:47'),(7,'admin',10000001,'Hello World','2024-06-13 22:34:50'),(8,'admin',10000001,'Hello World','2024-06-13 22:34:50'),(9,'admin',10000001,'Hello World','2024-06-13 22:34:51'),(10,'admin',10000001,'Hello World','2024-06-13 22:34:52'),(11,'admin',10000001,'Hello World','2024-06-13 22:34:53'),(12,'admin',10000001,'Hello World','2024-06-13 22:34:54'),(13,'admin',10000001,'Hello World','2024-06-13 22:34:55'),(14,'admin',10000001,'Hello World','2024-06-13 22:34:55'),(15,'admin',10000001,'Hello World','2024-06-13 22:34:56'),(16,'admin',10000001,'Hello World','2024-06-13 22:34:56'),(17,'admin',10000001,'Hello World','2024-06-13 22:34:57');
/*!40000 ALTER TABLE `public_chat_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(30) DEFAULT NULL COMMENT '用户名',
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `password` varchar(30) NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (10000001,'Admin','admin','123456..a'),(10000002,'TextUser','testUser1','123456..a'),(10000003,'TextUser','testUser2','123456..a'),(10000004,'TextUser','testUser3','123456..a'),(10000005,'TextUser','textUser4','123456..a');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-22 16:21:54
