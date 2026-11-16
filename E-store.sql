-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.32 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.4.0.6659
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for wb2_shop
CREATE DATABASE IF NOT EXISTS `wb2_shop` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `wb2_shop`;

-- Dumping structure for table wb2_shop.address
CREATE TABLE IF NOT EXISTS `address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `line_1` text NOT NULL,
  `line_2` text NOT NULL,
  `postal_code` varchar(10) NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_address_user1_idx` (`user_id`),
  CONSTRAINT `fk_address_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.address: ~3 rows (approximately)
INSERT INTO `address` (`id`, `line_1`, `line_2`, `postal_code`, `user_id`) VALUES
	(1, 'kandy', 'kandy', '2323', 6),
	(2, 'kandy', 'kandy', '2323', 21),
	(3, 'kandy', 'kandy', '2323', 18);

-- Dumping structure for table wb2_shop.cart
CREATE TABLE IF NOT EXISTS `cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `qty` int NOT NULL,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_product1_idx` (`product_id`),
  KEY `fk_cart_user1_idx` (`user_id`),
  CONSTRAINT `fk_cart_product1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_cart_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.cart: ~5 rows (approximately)
INSERT INTO `cart` (`id`, `qty`, `product_id`, `user_id`) VALUES
	(139, 1, 16, 21),
	(140, 1, 19, 21),
	(141, 1, 16, 24),
	(143, 1, 9, 6),
	(144, 1, 19, 6);

-- Dumping structure for table wb2_shop.orders
CREATE TABLE IF NOT EXISTS `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `address_id` int NOT NULL,
  `user_id` int NOT NULL,
  `price` double NOT NULL,
  `order_status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_orders_address1_idx` (`address_id`),
  KEY `fk_orders_user1_idx` (`user_id`),
  KEY `fk_orders_order_status1_idx` (`order_status_id`),
  CONSTRAINT `fk_orders_address1` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`),
  CONSTRAINT `fk_orders_order_status1` FOREIGN KEY (`order_status_id`) REFERENCES `order_status` (`id`),
  CONSTRAINT `fk_orders_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.orders: ~3 rows (approximately)
INSERT INTO `orders` (`id`, `created_at`, `address_id`, `user_id`, `price`, `order_status_id`) VALUES
	(1, '2025-09-28 02:59:47', 1, 6, 10, 3),
	(2, '2025-09-28 12:59:28', 2, 21, 100, 3),
	(8, '2025-09-30 00:25:13', 1, 6, 44, 2),
	(12, '2025-11-22 07:04:26', 1, 6, 84, 1),
	(13, '2026-10-12 08:00:55', 3, 18, 14, 1);

-- Dumping structure for table wb2_shop.order_item
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `qty` int NOT NULL,
  `product_id` int NOT NULL,
  `orders_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_item_product_idx` (`product_id`),
  KEY `fk_order_item_orders1_idx` (`orders_id`),
  CONSTRAINT `fk_order_item_orders1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.order_item: ~7 rows (approximately)
INSERT INTO `order_item` (`id`, `qty`, `product_id`, `orders_id`) VALUES
	(1, 1, 9, 1),
	(2, 1, 20, 2),
	(8, 2, 9, 8),
	(16, 1, 19, 12),
	(17, 1, 16, 12),
	(18, 1, 13, 13),
	(19, 1, 9, 13);

-- Dumping structure for table wb2_shop.order_status
CREATE TABLE IF NOT EXISTS `order_status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.order_status: ~6 rows (approximately)
INSERT INTO `order_status` (`id`, `value`) VALUES
	(1, 'Pending'),
	(2, 'Processing'),
	(3, 'Shipped'),
	(4, 'Delivered'),
	(5, 'cancelled'),
	(6, 'Test Status');

-- Dumping structure for table wb2_shop.product
CREATE TABLE IF NOT EXISTS `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `title` text NOT NULL,
  `price` double NOT NULL,
  `qty` int NOT NULL,
  `description` text NOT NULL,
  `created_at` datetime NOT NULL,
  `status_id` int NOT NULL,
  `user_id` int NOT NULL,
  `seller_id` int NOT NULL,
  `image_1` varchar(250) NOT NULL,
  `image_2` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_status1_idx` (`status_id`),
  KEY `fk_product_user1_idx` (`user_id`),
  KEY `fk_product_seller1_idx` (`seller_id`),
  CONSTRAINT `fk_product_seller1` FOREIGN KEY (`seller_id`) REFERENCES `seller` (`id`),
  CONSTRAINT `fk_product_status1` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `fk_product_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.product: ~5 rows (approximately)
INSERT INTO `product` (`id`, `name`, `title`, `price`, `qty`, `description`, `created_at`, `status_id`, `user_id`, `seller_id`, `image_1`, `image_2`) VALUES
	(9, 'Atomic Habit', 'Book', 10, 4, 'Self help, personal development book', '2025-09-23 01:20:56', 1, 6, 1, 'assets/store/e8fbc71f-3727-4f90-b180-e50ef051d251.jpg', 'assets/store/a193c0c8-c446-4741-b57e-8b444476bb75.jpg'),
	(13, 'Power subconscious Mind', 'Book', 4, 2, 'Self-Help Psychological Book', '2025-09-24 16:35:57', 1, 6, 1, 'assets/store/b371d866-6371-4962-9c06-c7bf337e5065.jpg', 'assets/store/e1d6abac-02c7-4fb3-a487-1f1c8f8d3b70.jpg'),
	(16, 'casual watch blue', 'watch', 60, 3, 'Men casual watch black colour', '2025-09-25 02:00:10', 1, 6, 1, 'assets/store/5f8e7afc-96e4-4928-9b76-72edf261e009.jpg', 'assets/store/6bc994a6-f33d-437b-8e03-6e2498f493c5.jpg'),
	(19, 'Men Black shoes', 'shoes', 24, 6, 'men black casual shoe', '2025-09-28 11:38:40', 1, 21, 6, 'assets/store/3c602cd4-fbf7-4d06-92c5-a6b246d722d0.jpg', 'assets/store/e17d9098-6d62-4f58-a90a-bfe71607b0a6.jpg'),
	(20, 'test2', 'test1', 100, 2, 'test product', '2025-09-28 11:40:52', 1, 21, 6, 'assets/store/8ab48b5e-d3ef-4fcc-9cc3-8d72ec7afcfd.jpg', 'assets/store/e824cfbb-0bad-4f7e-bf50-8466f18d923b.jpg');

-- Dumping structure for table wb2_shop.seller
CREATE TABLE IF NOT EXISTS `seller` (
  `id` int NOT NULL AUTO_INCREMENT,
  `store_name` varchar(45) NOT NULL,
  `store_description` varchar(250) NOT NULL,
  `tax_id` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int NOT NULL,
  `store_email` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_seller_user1_idx` (`user_id`),
  CONSTRAINT `fk_seller_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.seller: ~3 rows (approximately)
INSERT INTO `seller` (`id`, `store_name`, `store_description`, `tax_id`, `created_at`, `user_id`, `store_email`) VALUES
	(1, 'AAA Store', 'Official demo store for electronic and books', 'TAX-11111', '2025-09-22 04:14:56', 6, 'store1@example.com'),
	(2, 'AAA5 Store', 'General retail outlet', 'TAX-22222', '2025-09-22 04:19:21', 6, 'store2@example.com'),
	(3, 'ZC Store', 'Apparel & accessories', 'TAX-33333', '2025-09-22 14:05:59', 6, 'store3@example.com'),
	(5, 'Books Hub', 'Online bookstore', 'TAX-44444', '2025-09-25 01:41:36', 20, 'books@example.com'),
	(6, 'Test Outlet', 'Electronics & Footwear', 'TAX-55555', '2025-09-28 11:38:03', 21, 'teststore@example.com'),
	(7, 'Ceylon Store', 'Ceylon quality products', 'TAX-66666', '2025-10-03 11:38:10', 24, 'ceylonstore@example.com');

-- Dumping structure for table wb2_shop.status
CREATE TABLE IF NOT EXISTS `status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.status: ~2 rows (approximately)
INSERT INTO `status` (`id`, `value`) VALUES
	(1, 'pending'),
	(2, 'Approved');

-- Dumping structure for table wb2_shop.user
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(45) NOT NULL,
  `last_name` varchar(45) NOT NULL,
  `email` varchar(45) NOT NULL,
  `verification` varchar(10) NOT NULL,
  `created_at` datetime NOT NULL,
  `mobile` varchar(15) NOT NULL,
  `password` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table wb2_shop.user: ~8 rows (approximately)
INSERT INTO `user` (`id`, `first_name`, `last_name`, `email`, `verification`, `created_at`, `mobile`, `password`) VALUES
	(6, 'Demo', 'User', 'demo.user1@example.com', '660880', '2025-07-31 02:17:21', '0710000001', 'DemoPass@123'),
	(15, 'Demo', 'Customer', 'demo.user2@example.com', '077475', '2025-07-31 03:03:18', '0710000002', 'DemoPass@123'),
	(16, 'Demo', 'Shopper', 'demo.user3@example.com', '526887', '2025-07-31 03:08:09', '0710000003', 'DemoPass@123'),
	(17, 'Demo', 'Buyer', 'demo.user4@example.com', '979104', '2025-07-31 22:15:11', '0710000004', 'DemoPass@123'),
	(18, 'Thilan', 'Kalhara', 'demo.thilan@example.com', '021029', '2025-09-22 19:10:44', '0710000005', 'DemoPass@123'),
	(20, 'Book', 'Seller', 'demo.books@example.com', '253648', '2025-09-25 01:39:47', '0710000006', 'DemoPass@123'),
	(21, 'Test', 'Account', 'demo.test@example.com', '066628', '2025-09-28 11:36:11', '0710000007', 'DemoPass@123'),
	(24, 'Ceylon', 'Store', 'demo.ceylon@example.com', '148047', '2025-10-03 11:35:32', '0710000008', 'DemoPass@123');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
