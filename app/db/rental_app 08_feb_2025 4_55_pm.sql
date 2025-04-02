-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 08, 2025 at 12:24 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `rental_app`
--

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_accounts`
--

CREATE TABLE `ra_tbl_accounts` (
  `account_id` int(11) NOT NULL,
  `account_username` varchar(255) NOT NULL,
  `account_password` varchar(255) NOT NULL,
  `account_portal_type` tinyint(1) NOT NULL COMMENT '1 - Customer, 2 - Guest, 3 - Marketing, 4 - Admin, 5 - Developer',
  `account_code` varchar(50) NOT NULL,
  `account_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `account_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_bookings`
--

CREATE TABLE `ra_tbl_bookings` (
  `booking_id` int(11) NOT NULL,
  `booking_code` varchar(50) NOT NULL,
  `booking_customer_id` int(11) NOT NULL,
  `booking_guest_id` int(11) NOT NULL,
  `booking_type` tinyint(1) NOT NULL COMMENT '1 - New, 2 - Rebooking',
  `booking_rebooking_id` int(11) DEFAULT NULL,
  `booking_vehicle_id` int(11) NOT NULL,
  `booking_slot_id` int(11) NOT NULL,
  `booking_start_time` time NOT NULL,
  `booking_end_time` time NOT NULL,
  `booking_date` date NOT NULL,
  `booking_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Pending, 2 - Booked, 3 - Cancelled',
  `booking_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_customers`
--

CREATE TABLE `ra_tbl_customers` (
  `customer_id` int(11) NOT NULL,
  `customer_account_id` int(11) NOT NULL,
  `customer_contact_name` varchar(255) NOT NULL,
  `customer_mobile_number` varchar(15) NOT NULL,
  `customer_email_id` varchar(255) DEFAULT NULL,
  `address_door_no` varchar(50) DEFAULT NULL,
  `address_street_name` varchar(255) DEFAULT NULL,
  `address_locality` varchar(255) DEFAULT NULL,
  `address_city` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_state` varchar(100) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT NULL,
  `address_postal_code` varchar(20) DEFAULT NULL,
  `address_latitude` varchar(50) DEFAULT NULL,
  `address_longitude` varchar(50) DEFAULT NULL,
  `customer_account_holder_name` varchar(255) DEFAULT NULL,
  `customer_bank_name` varchar(255) DEFAULT NULL,
  `customer_branch_name` varchar(255) DEFAULT NULL,
  `customer_ifsc_code` varchar(20) DEFAULT NULL,
  `customer_upi_number` varchar(50) DEFAULT NULL,
  `customer_business_name` varchar(255) NOT NULL,
  `customer_business_register_number` varchar(50) NOT NULL,
  `customer_business_gstin` varchar(50) DEFAULT NULL,
  `customer_business_logo_path` varchar(255) DEFAULT NULL,
  `customer_business_license_path` varchar(255) DEFAULT NULL,
  `customer_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `customer_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_error_logs`
--

CREATE TABLE `ra_tbl_error_logs` (
  `error_id` int(11) NOT NULL,
  `login_id` int(11) NOT NULL,
  `error_side` tinyint(1) NOT NULL COMMENT '1 - Client, 2 - Server, 3 - DB',
  `activity_page` varchar(255) DEFAULT NULL,
  `error_message` varchar(255) NOT NULL,
  `error_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_general`
--

CREATE TABLE `ra_tbl_general` (
  `general_id` int(11) NOT NULL,
  `general_group_id` int(11) NOT NULL,
  `general_title` varchar(255) NOT NULL,
  `general_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `general_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_guests`
--

CREATE TABLE `ra_tbl_guests` (
  `guest_id` int(11) NOT NULL,
  `guest_name` varchar(255) NOT NULL,
  `guest_mobile_number` varchar(15) NOT NULL,
  `guest_email_id` varchar(255) NOT NULL,
  `guest_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `guest_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_guest_ratings`
--

CREATE TABLE `ra_tbl_guest_ratings` (
  `guest_rating_id` int(11) NOT NULL,
  `guest_rating_customer_id` int(11) NOT NULL,
  `guest_rating_guest_id` int(11) NOT NULL,
  `guest_rating_booking_id` int(11) NOT NULL,
  `guest_rating` tinyint(1) NOT NULL CHECK (`guest_rating` between 0 and 5),
  `guest_rating_descriptions` varchar(255) DEFAULT NULL,
  `guest_rating_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `guest_rating_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_invoices`
--

CREATE TABLE `ra_tbl_invoices` (
  `invoice_id` int(11) NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `invoice_customer_id` int(11) NOT NULL,
  `invoice_guest_id` int(11) NOT NULL,
  `invoice_booking_id` int(11) NOT NULL,
  `invoice_date` date NOT NULL,
  `invoice_due_date` date NOT NULL,
  `invoice_subtotal` decimal(10,2) DEFAULT 0.00,
  `invoice_gst` tinyint(1) DEFAULT 0 COMMENT '0 - Without GST, 1 - With GST',
  `invoice_gst_type` tinyint(1) DEFAULT 0 COMMENT '0 - no GST, 1 - CGST, 2 - IGST',
  `invoice_cgst` decimal(10,2) DEFAULT 0.00,
  `invoice_sgst` decimal(10,2) DEFAULT 0.00,
  `invoice_igst` decimal(10,2) DEFAULT 0.00,
  `invoice_total_gst_amount` decimal(10,2) DEFAULT 0.00,
  `invoice_total_amount` decimal(10,2) DEFAULT 0.00,
  `invoice_adjustments` decimal(10,2) DEFAULT 0.00,
  `invoice_grand_total` decimal(10,2) DEFAULT 0.00,
  `invoice_amount_in_words` varchar(255) DEFAULT NULL,
  `invoice_payment_mode` tinyint(1) DEFAULT 1 COMMENT '1 - CASH, 2 - UPI, 3 - CARD',
  `invoice_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `invoice_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_invoice_vehicles`
--

CREATE TABLE `ra_tbl_invoice_vehicles` (
  `invoice_vehicle_id` int(11) NOT NULL,
  `invoice_vehicle_invoice_id` int(11) NOT NULL,
  `invoice_vehicle_vehicle_id` int(11) NOT NULL,
  `invoice_vehicle_discount_amount` decimal(10,2) DEFAULT 0.00,
  `invoice_vehicle_amount` decimal(10,2) DEFAULT 0.00,
  `invoice_vehicle_notes` varchar(255) DEFAULT NULL,
  `invoice_vehicle_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `invoice_vehicle_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_login_logs`
--

CREATE TABLE `ra_tbl_login_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `login_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `logout_time` datetime DEFAULT NULL,
  `user_ip_address` varchar(50) DEFAULT NULL,
  `successful_login` tinyint(1) DEFAULT NULL COMMENT '0 - Failed Login, 1 - Successful Login',
  `login_status` tinyint(1) DEFAULT NULL COMMENT '0 - Logout, 1 - Login, 2 - Mismatch',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_user_activity_logs`
--

CREATE TABLE `ra_tbl_user_activity_logs` (
  `activity_id` int(11) NOT NULL,
  `login_id` int(11) NOT NULL,
  `db_table_affected` varchar(255) DEFAULT NULL,
  `action_type` tinyint(1) NOT NULL COMMENT '1 - Fetch, 2 - Insert, 3 - Update, 4 - Delete',
  `activity_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_vehicles`
--

CREATE TABLE `ra_tbl_vehicles` (
  `vehicle_id` int(11) NOT NULL,
  `vehicle_customer_id` int(11) NOT NULL,
  `vehicle_type` tinyint(1) NOT NULL COMMENT '1 - Bike, 2 - Car, 3 - Bicycle',
  `vehicle_title` varchar(255) NOT NULL,
  `vehicle_brand` varchar(100) NOT NULL,
  `vehicle_model` varchar(100) NOT NULL,
  `vehicle_category` tinyint(1) DEFAULT NULL COMMENT '1 - Petrol, 2 - Diesel, 3 - CNG, 4 - EV',
  `vehicle_color` varchar(50) NOT NULL,
  `vehicle_seating_capacity` int(11) NOT NULL DEFAULT 0,
  `vehicle_luggage_capacity` int(11) NOT NULL DEFAULT 0,
  `vehicle_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `vehicle_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_vehicles_images`
--

CREATE TABLE `ra_tbl_vehicles_images` (
  `vehicles_images_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `vehicle_image_path` varchar(255) DEFAULT NULL,
  `vehicle_image_order` tinyint(1) NOT NULL,
  `vehicle_image_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `vehicle_image_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_vehicles_slots`
--

CREATE TABLE `ra_tbl_vehicles_slots` (
  `vehicle_slots_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `vehicle_slot_duration` varchar(50) DEFAULT NULL,
  `vehicle_slot_duration_type` tinyint(1) NOT NULL COMMENT '1 - min, 2 - hour',
  `vehicle_slot_price` decimal(10,2) DEFAULT 0.00,
  `vehicle_slot_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `vehicle_slot_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_wallets`
--

CREATE TABLE `ra_tbl_wallets` (
  `wallet_id` int(11) NOT NULL,
  `wallet_account_id` int(11) NOT NULL,
  `wallet_balance_amount` decimal(10,2) NOT NULL DEFAULT 0.00,
  `wallet_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `wallet_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_wallet_transactions`
--

CREATE TABLE `ra_tbl_wallet_transactions` (
  `wallet_transaction_id` int(11) NOT NULL,
  `wallet_transaction_wallet_id` int(11) NOT NULL,
  `wallet_transaction_amount` decimal(10,2) DEFAULT 0.00,
  `wallet_transaction_account_number` varchar(50) NOT NULL,
  `wallet_transaction_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Debit, 2 - Credit',
  `wallet_transaction_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `ra_tbl_accounts`
--
ALTER TABLE `ra_tbl_accounts`
  ADD PRIMARY KEY (`account_id`);

--
-- Indexes for table `ra_tbl_bookings`
--
ALTER TABLE `ra_tbl_bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `booking_customer_id` (`booking_customer_id`),
  ADD KEY `booking_guest_id` (`booking_guest_id`),
  ADD KEY `booking_rebooking_id` (`booking_rebooking_id`),
  ADD KEY `booking_vehicle_id` (`booking_vehicle_id`),
  ADD KEY `booking_slot_id` (`booking_slot_id`);

--
-- Indexes for table `ra_tbl_customers`
--
ALTER TABLE `ra_tbl_customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD KEY `customer_account_id` (`customer_account_id`);

--
-- Indexes for table `ra_tbl_error_logs`
--
ALTER TABLE `ra_tbl_error_logs`
  ADD PRIMARY KEY (`error_id`),
  ADD KEY `login_id` (`login_id`);

--
-- Indexes for table `ra_tbl_general`
--
ALTER TABLE `ra_tbl_general`
  ADD PRIMARY KEY (`general_id`);

--
-- Indexes for table `ra_tbl_guests`
--
ALTER TABLE `ra_tbl_guests`
  ADD PRIMARY KEY (`guest_id`);

--
-- Indexes for table `ra_tbl_guest_ratings`
--
ALTER TABLE `ra_tbl_guest_ratings`
  ADD PRIMARY KEY (`guest_rating_id`),
  ADD KEY `guest_rating_customer_id` (`guest_rating_customer_id`),
  ADD KEY `guest_rating_guest_id` (`guest_rating_guest_id`),
  ADD KEY `guest_rating_booking_id` (`guest_rating_booking_id`);

--
-- Indexes for table `ra_tbl_invoices`
--
ALTER TABLE `ra_tbl_invoices`
  ADD PRIMARY KEY (`invoice_id`),
  ADD KEY `invoice_customer_id` (`invoice_customer_id`),
  ADD KEY `invoice_guest_id` (`invoice_guest_id`),
  ADD KEY `invoice_booking_id` (`invoice_booking_id`);

--
-- Indexes for table `ra_tbl_invoice_vehicles`
--
ALTER TABLE `ra_tbl_invoice_vehicles`
  ADD PRIMARY KEY (`invoice_vehicle_id`),
  ADD KEY `invoice_vehicle_invoice_id` (`invoice_vehicle_invoice_id`),
  ADD KEY `invoice_vehicle_vehicle_id` (`invoice_vehicle_vehicle_id`);

--
-- Indexes for table `ra_tbl_login_logs`
--
ALTER TABLE `ra_tbl_login_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `ra_tbl_user_activity_logs`
--
ALTER TABLE `ra_tbl_user_activity_logs`
  ADD PRIMARY KEY (`activity_id`),
  ADD KEY `login_id` (`login_id`);

--
-- Indexes for table `ra_tbl_vehicles`
--
ALTER TABLE `ra_tbl_vehicles`
  ADD PRIMARY KEY (`vehicle_id`),
  ADD KEY `vehicle_customer_id` (`vehicle_customer_id`);

--
-- Indexes for table `ra_tbl_vehicles_images`
--
ALTER TABLE `ra_tbl_vehicles_images`
  ADD PRIMARY KEY (`vehicles_images_id`),
  ADD KEY `vehicle_id` (`vehicle_id`);

--
-- Indexes for table `ra_tbl_vehicles_slots`
--
ALTER TABLE `ra_tbl_vehicles_slots`
  ADD PRIMARY KEY (`vehicle_slots_id`),
  ADD KEY `vehicle_id` (`vehicle_id`);

--
-- Indexes for table `ra_tbl_wallets`
--
ALTER TABLE `ra_tbl_wallets`
  ADD PRIMARY KEY (`wallet_id`),
  ADD KEY `wallet_account_id` (`wallet_account_id`);

--
-- Indexes for table `ra_tbl_wallet_transactions`
--
ALTER TABLE `ra_tbl_wallet_transactions`
  ADD PRIMARY KEY (`wallet_transaction_id`),
  ADD KEY `wallet_transaction_wallet_id` (`wallet_transaction_wallet_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `ra_tbl_accounts`
--
ALTER TABLE `ra_tbl_accounts`
  MODIFY `account_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_bookings`
--
ALTER TABLE `ra_tbl_bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_customers`
--
ALTER TABLE `ra_tbl_customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_error_logs`
--
ALTER TABLE `ra_tbl_error_logs`
  MODIFY `error_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_general`
--
ALTER TABLE `ra_tbl_general`
  MODIFY `general_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_guests`
--
ALTER TABLE `ra_tbl_guests`
  MODIFY `guest_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_guest_ratings`
--
ALTER TABLE `ra_tbl_guest_ratings`
  MODIFY `guest_rating_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_invoices`
--
ALTER TABLE `ra_tbl_invoices`
  MODIFY `invoice_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_invoice_vehicles`
--
ALTER TABLE `ra_tbl_invoice_vehicles`
  MODIFY `invoice_vehicle_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_login_logs`
--
ALTER TABLE `ra_tbl_login_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_user_activity_logs`
--
ALTER TABLE `ra_tbl_user_activity_logs`
  MODIFY `activity_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_vehicles`
--
ALTER TABLE `ra_tbl_vehicles`
  MODIFY `vehicle_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_vehicles_images`
--
ALTER TABLE `ra_tbl_vehicles_images`
  MODIFY `vehicles_images_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_vehicles_slots`
--
ALTER TABLE `ra_tbl_vehicles_slots`
  MODIFY `vehicle_slots_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_wallets`
--
ALTER TABLE `ra_tbl_wallets`
  MODIFY `wallet_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_wallet_transactions`
--
ALTER TABLE `ra_tbl_wallet_transactions`
  MODIFY `wallet_transaction_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ra_tbl_bookings`
--
ALTER TABLE `ra_tbl_bookings`
  ADD CONSTRAINT `ra_tbl_bookings_ibfk_1` FOREIGN KEY (`booking_customer_id`) REFERENCES `ra_tbl_customers` (`customer_id`),
  ADD CONSTRAINT `ra_tbl_bookings_ibfk_2` FOREIGN KEY (`booking_guest_id`) REFERENCES `ra_tbl_guests` (`guest_id`),
  ADD CONSTRAINT `ra_tbl_bookings_ibfk_3` FOREIGN KEY (`booking_rebooking_id`) REFERENCES `ra_tbl_bookings` (`booking_id`),
  ADD CONSTRAINT `ra_tbl_bookings_ibfk_4` FOREIGN KEY (`booking_vehicle_id`) REFERENCES `ra_tbl_vehicles` (`vehicle_id`),
  ADD CONSTRAINT `ra_tbl_bookings_ibfk_5` FOREIGN KEY (`booking_slot_id`) REFERENCES `ra_tbl_vehicles_slots` (`vehicle_slots_id`);

--
-- Constraints for table `ra_tbl_customers`
--
ALTER TABLE `ra_tbl_customers`
  ADD CONSTRAINT `ra_tbl_customers_ibfk_1` FOREIGN KEY (`customer_account_id`) REFERENCES `ra_tbl_accounts` (`account_id`);

--
-- Constraints for table `ra_tbl_error_logs`
--
ALTER TABLE `ra_tbl_error_logs`
  ADD CONSTRAINT `ra_tbl_error_logs_ibfk_1` FOREIGN KEY (`login_id`) REFERENCES `ra_tbl_login_logs` (`log_id`);

--
-- Constraints for table `ra_tbl_guest_ratings`
--
ALTER TABLE `ra_tbl_guest_ratings`
  ADD CONSTRAINT `ra_tbl_guest_ratings_ibfk_1` FOREIGN KEY (`guest_rating_customer_id`) REFERENCES `ra_tbl_customers` (`customer_id`),
  ADD CONSTRAINT `ra_tbl_guest_ratings_ibfk_2` FOREIGN KEY (`guest_rating_guest_id`) REFERENCES `ra_tbl_guests` (`guest_id`),
  ADD CONSTRAINT `ra_tbl_guest_ratings_ibfk_3` FOREIGN KEY (`guest_rating_booking_id`) REFERENCES `ra_tbl_bookings` (`booking_id`);

--
-- Constraints for table `ra_tbl_invoices`
--
ALTER TABLE `ra_tbl_invoices`
  ADD CONSTRAINT `ra_tbl_invoices_ibfk_1` FOREIGN KEY (`invoice_customer_id`) REFERENCES `ra_tbl_customers` (`customer_id`),
  ADD CONSTRAINT `ra_tbl_invoices_ibfk_2` FOREIGN KEY (`invoice_guest_id`) REFERENCES `ra_tbl_guests` (`guest_id`),
  ADD CONSTRAINT `ra_tbl_invoices_ibfk_3` FOREIGN KEY (`invoice_booking_id`) REFERENCES `ra_tbl_bookings` (`booking_id`);

--
-- Constraints for table `ra_tbl_invoice_vehicles`
--
ALTER TABLE `ra_tbl_invoice_vehicles`
  ADD CONSTRAINT `ra_tbl_invoice_vehicles_ibfk_1` FOREIGN KEY (`invoice_vehicle_invoice_id`) REFERENCES `ra_tbl_invoices` (`invoice_id`),
  ADD CONSTRAINT `ra_tbl_invoice_vehicles_ibfk_2` FOREIGN KEY (`invoice_vehicle_vehicle_id`) REFERENCES `ra_tbl_vehicles` (`vehicle_id`);

--
-- Constraints for table `ra_tbl_login_logs`
--
ALTER TABLE `ra_tbl_login_logs`
  ADD CONSTRAINT `ra_tbl_login_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `ra_tbl_accounts` (`account_id`);

--
-- Constraints for table `ra_tbl_user_activity_logs`
--
ALTER TABLE `ra_tbl_user_activity_logs`
  ADD CONSTRAINT `ra_tbl_user_activity_logs_ibfk_1` FOREIGN KEY (`login_id`) REFERENCES `ra_tbl_login_logs` (`log_id`);

--
-- Constraints for table `ra_tbl_vehicles`
--
ALTER TABLE `ra_tbl_vehicles`
  ADD CONSTRAINT `ra_tbl_vehicles_ibfk_1` FOREIGN KEY (`vehicle_customer_id`) REFERENCES `ra_tbl_customers` (`customer_id`);

--
-- Constraints for table `ra_tbl_vehicles_images`
--
ALTER TABLE `ra_tbl_vehicles_images`
  ADD CONSTRAINT `ra_tbl_vehicles_images_ibfk_1` FOREIGN KEY (`vehicle_id`) REFERENCES `ra_tbl_vehicles` (`vehicle_id`);

--
-- Constraints for table `ra_tbl_vehicles_slots`
--
ALTER TABLE `ra_tbl_vehicles_slots`
  ADD CONSTRAINT `ra_tbl_vehicles_slots_ibfk_1` FOREIGN KEY (`vehicle_id`) REFERENCES `ra_tbl_vehicles` (`vehicle_id`);

--
-- Constraints for table `ra_tbl_wallets`
--
ALTER TABLE `ra_tbl_wallets`
  ADD CONSTRAINT `ra_tbl_wallets_ibfk_1` FOREIGN KEY (`wallet_account_id`) REFERENCES `ra_tbl_accounts` (`account_id`);

--
-- Constraints for table `ra_tbl_wallet_transactions`
--
ALTER TABLE `ra_tbl_wallet_transactions`
  ADD CONSTRAINT `ra_tbl_wallet_transactions_ibfk_1` FOREIGN KEY (`wallet_transaction_wallet_id`) REFERENCES `ra_tbl_wallets` (`wallet_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
