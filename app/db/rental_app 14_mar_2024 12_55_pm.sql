-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 14, 2025 at 08:24 AM
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

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `check_login_username` (IN `p_username` VARCHAR(255))   BEGIN
    -- Declare a variable to hold the count
    DECLARE v_count INT DEFAULT 0;

    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        -- Log the error
        CALL insert_error_log(
            0,           -- login_id from input parameter
            3,                    -- error_side: 3 for DB
            'check_login_username',     -- activity_page: procedure name
            error_message         -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        -- Get the warning error number
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        -- Only increment warning_count for non-"no data" warnings (1329)
        IF warning_errno != 1329 THEN
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            -- Log the warning
            CALL insert_error_log(
                0,           -- login_id from input parameter
                3,                    -- error_side: 3 for DB
                'check_login_username',     -- activity_page: procedure name
                warning_message       -- warning_message from diagnostics
            );
        END IF;
    END;
    -- Check if the username exists and is active/not deleted
    SELECT COUNT(*)
    INTO v_count
    FROM `ra_tbl_accounts`
    WHERE `account_username` = p_username
    AND `account_status` = 1 -- Active
    AND `account_deleted` = 0; -- Not Deleted

    -- Set the exists flag
    IF v_count > 0 THEN

        -- Retrieve the account details
        SELECT 
            `account_id`,
            `account_portal_type`,
            `account_code`,
            `salt_value`,
            `master_key`,
            `iterations_value`,
            `iv_value`,
            `v_count` AS exist
        FROM `ra_tbl_accounts`
        WHERE `account_username` = p_username
        AND `account_status` = 1
        AND `account_deleted` = 0
        LIMIT 1; -- Ensure only one row is returned

    ELSE
        SELECT `v_count` AS exist;
    END IF;

        -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'User Name Checked Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `check_username_existence` (IN `p_account_username` VARCHAR(255), IN `p_account_portal_type` TINYINT(1))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE username_exists INT DEFAULT 0;
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        -- Log the error
        CALL insert_error_log(
            0,           -- login_id from input parameter
            3,                    -- error_side: 3 for DB
            'check_username_existence', -- activity_page: procedure name
            error_message         -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            -- Log the warning
            CALL insert_error_log(
                0,           -- login_id from input parameter
                3,                    -- error_side: 3 for DB
                'check_username_existence', -- activity_page: procedure name
                warning_message       -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Check if username exists with the specified portal type
    SELECT COUNT(*) INTO username_exists
    FROM ra_tbl_accounts
    WHERE account_username = p_account_username
    AND account_portal_type = p_account_portal_type
    AND account_deleted = 0;

    -- Return result based on username existence
    IF username_exists > 0 THEN
        SELECT 300 AS status_code, 'warning' AS status, 
               'Username already exists' AS message;
    ELSE
        SELECT 200 AS status_code, 'success' AS status, 
               'Username available' AS message;
    END IF;

    -- Return status based on warnings (if any additional warnings occurred)
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 'warning' AS status, 
               CONCAT('Query executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `create_account` (IN `p_account_username` VARCHAR(255), IN `p_account_password` TEXT, IN `p_account_portal_type` TINYINT(1))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE last_account_code VARCHAR(50);
    DECLARE new_account_code VARCHAR(50);
    DECLARE v_master_key TEXT;
    DECLARE v_salt VARCHAR(255);
    DECLARE v_iterations INT;
    DECLARE v_iv VARCHAR(255);
    DECLARE last_inserted_id INT;
    DECLARE warning_errno INT;  -- Added to store warning error number

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        -- Log the error
        CALL insert_error_log(
            0,           -- login_id from input parameter
            3,                    -- error_side: 3 for DB
            'create_account',     -- activity_page: procedure name
            error_message         -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        -- Get the warning error number
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        -- Only increment warning_count for non-"no data" warnings (1329)
        IF warning_errno != 1329 THEN
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            -- Log the warning
            CALL insert_error_log(
                0,           -- login_id from input parameter
                3,                    -- error_side: 3 for DB
                'create_account',     -- activity_page: procedure name
                warning_message       -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Extract JSON values from account_password
    SET v_master_key = JSON_UNQUOTE(JSON_EXTRACT(p_account_password, '$.master_key'));
    SET v_salt = JSON_UNQUOTE(JSON_EXTRACT(p_account_password, '$.salt'));
    SET v_iterations = JSON_UNQUOTE(JSON_EXTRACT(p_account_password, '$.iterations'));
    SET v_iv = JSON_UNQUOTE(JSON_EXTRACT(p_account_password, '$.iv'));

    -- Get the last account code for this portal type
    SELECT account_code INTO last_account_code
    FROM ra_tbl_accounts
    WHERE account_portal_type = p_account_portal_type
    AND account_deleted = 0
    ORDER BY account_id DESC
    LIMIT 1;

    -- Generate new account code (increment last code by 1)
    IF last_account_code IS NULL THEN
        -- Determine the prefix based on p_account_portal_type
        SET new_account_code = CONCAT(
            CASE p_account_portal_type
                WHEN 1 THEN 'V-'
                WHEN 2 THEN 'C-'
                WHEN 3 THEN 'M-'
                WHEN 4 THEN 'A-'
                WHEN 5 THEN 'D-'
                ELSE 'ACC' -- Default prefix if portal type is invalid
            END,
            '1' -- Start with 1
        );
    ELSE
        -- Extract the numeric part of the last account code and increment it
        SET new_account_code = CONCAT(
            CASE p_account_portal_type
                WHEN 1 THEN 'V-'
                WHEN 2 THEN 'C-'
                WHEN 3 THEN 'M-'
                WHEN 4 THEN 'A-'
                WHEN 5 THEN 'D-'
                ELSE 'ACC' -- Default prefix if portal type is invalid
            END,
            CAST(SUBSTRING(last_account_code, 3) AS UNSIGNED) + 1
        );
    END IF;

    -- Insert new account
    INSERT INTO ra_tbl_accounts (
        account_username,
        account_portal_type,
        account_code,
        master_key,
        salt_value,
        iterations_value,
        iv_value,
        account_status,
        account_deleted,
        created_at,
        updated_at
    ) VALUES (
        p_account_username,
        p_account_portal_type,
        new_account_code,
        v_master_key,
        v_salt,
        v_iterations,
        v_iv,
        1,  -- default active status
        0,  -- default not deleted
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

    -- Get the last inserted ID
    SET last_inserted_id = LAST_INSERT_ID();

    -- Return inserted ID
    SELECT last_inserted_id AS account_id;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Account created with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Account created successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_customer_individual_vehicle` (IN `p_vehicle_id` INT)   BEGIN
     -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'delete_customer_individual_vehicle', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'delete_customer_individual_vehicle', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Update ra_tbl_vehicles: set status to Inactive (2) and deleted to 1
    UPDATE `ra_tbl_vehicles`
    SET 
        `vehicle_status` = 2,
        `vehicle_deleted` = 1
    WHERE `vehicle_id` = p_vehicle_id
    AND `vehicle_deleted` = 0; -- Ensure it’s not already marked as deleted

    -- Update ra_tbl_vehicles_images: set status to Inactive (2) and deleted to 1 for related images
    UPDATE `ra_tbl_vehicles_images`
    SET 
        `vehicle_image_status` = 2,
        `vehicle_image_deleted` = 1
    WHERE `vehicle_id` = p_vehicle_id
    AND `vehicle_image_deleted` = 0; -- Ensure images aren’t already marked as deleted

    -- Return success message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
                'warning' AS status, 
                CONCAT('Vehicle Deleted with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
                'success' AS status, 
                'Vehicle Deleted successfully' AS message;
    END IF;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetch_customer_individual_vehicle_brands` (IN `p_as_vehicleType` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'fetch_customer_individual_vehicle_brands', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'fetch_customer_individual_vehicle_brands', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Select relevant columns from ra_tbl_general with conditional vehicle type filtering
    SELECT 
        general_id AS brand_id,
        general_title AS brand_name
    FROM 
        ra_tbl_general
    WHERE 
        (p_as_vehicleType = 0 OR general_group_id = p_as_vehicleType)
        AND general_status = 1
        AND general_deleted = 0
    ORDER BY 
        general_title ASC;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Vehicles Brands Fetched Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetch_customer_individual_vehicle_info` (IN `p_vehicle_id` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(0, 3, 'fetch_customer_individual_vehicle_info', error_message);
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(0, 3, 'fetch_customer_individual_vehicle_info', warning_message);
        END IF;
    END;

    -- Start fetching data
    BEGIN
        -- Fetch vehicle basic info with category/type names and brand name from general table
        SELECT 
            v.vehicle_id,
            v.vehicle_customer_id,
            v.vehicle_type,
            CASE v.vehicle_type
                WHEN 1 THEN 'Bike'
                WHEN 2 THEN 'Car'
                WHEN 3 THEN 'Bicycle'
                ELSE 'Unknown'
            END AS vehicle_type_name,
            v.vehicle_title,
            v.vehicle_brand,
            g.general_title AS vehicle_brand_name,  -- Fetch brand name from general table
            v.vehicle_model,
            v.vehicle_category,
            CASE v.vehicle_category
                WHEN 1 THEN 'Petrol'
                WHEN 2 THEN 'Diesel'
                WHEN 3 THEN 'CNG'
                WHEN 4 THEN 'EV'
                WHEN 5 THEN 'Hybrid'
                WHEN 6 THEN 'None'
                ELSE 'Unknown'
            END AS vehicle_category_name,
            v.vehicle_color,
            v.vehicle_seating_capacity,
            v.vehicle_luggage_capacity,
            v.vehicle_number,
            v.rc_path,
            v.vehicle_status,
            v.vehicle_deleted,
            v.created_by,
            v.updated_by,
            v.created_at,
            v.updated_at
        FROM 
            ra_tbl_vehicles v
        LEFT JOIN 
            ra_tbl_general g
            ON CAST(v.vehicle_brand AS UNSIGNED) = g.general_id  -- Convert varchar to integer for matching
            AND g.general_group_id = v.vehicle_type
            AND g.general_status = 1
            AND g.general_deleted = 0
        WHERE 
            v.vehicle_id = p_vehicle_id
            AND v.vehicle_status = 1  
            AND v.vehicle_deleted = 0;

        -- Fetch vehicle images
        SELECT 
            vi.vehicles_images_id,  -- Ensure correct column name
            vi.vehicle_id,
            vi.vehicle_image_path,
            vi.vehicle_image_order,
            vi.vehicle_image_status,
            vi.vehicle_image_deleted,
            vi.created_by,
            vi.updated_by,
            vi.created_at,
            vi.updated_at
        FROM 
            ra_tbl_vehicles_images vi
        WHERE 
            vi.vehicle_id = p_vehicle_id
            AND vi.vehicle_image_status = 1  
            AND vi.vehicle_image_deleted = 0
        ORDER BY 
            vi.vehicle_image_order ASC;

        -- Fetch vehicle slots
        SELECT 
            vs.vehicle_slots_id,
            vs.vehicle_id,
            vs.vehicle_slot_duration,
            vs.vehicle_slot_duration_type,
            vs.vehicle_slot_price,
            vs.vehicle_slot_status,
            vs.vehicle_slot_deleted,
            vs.created_by,
            vs.updated_by,
            vs.created_at,
            vs.updated_at
        FROM 
            ra_tbl_vehicles_slots vs
        WHERE 
            vs.vehicle_id = p_vehicle_id
            AND vs.vehicle_slot_status = 1  
            AND vs.vehicle_slot_deleted = 0;
    END;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Vehicle Info Fetched Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetch_customer_vehicles_list` (IN `p_customer_account_id` INT, IN `p_vehicle_type` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'fetch_customer_vehicles_list', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'fetch_customer_vehicles_list', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Select vehicle details with associated image and additional information
    SELECT 
        v.vehicle_id,
        v.vehicle_title,
        v.vehicle_brand,
        v.vehicle_model,
        v.vehicle_type AS vehicle_type_id,
        CASE v.vehicle_type
            WHEN 1 THEN 'Bike'
            WHEN 2 THEN 'Car'
            WHEN 3 THEN 'Bicycle'
            ELSE 'Unknown'
        END AS vehicle_type_name,
        v.vehicle_category AS vehicle_category_id,
        CASE v.vehicle_category
            WHEN 1 THEN 'Petrol'
            WHEN 2 THEN 'Diesel'
            WHEN 3 THEN 'CNG'
            WHEN 4 THEN 'EV'
            WHEN 5 THEN 'Hybrid'
            WHEN 6 THEN 'None'
            ELSE 'Unknown'
        END AS vehicle_category_name,
        v.vehicle_color,
        v.vehicle_seating_capacity,
        v.vehicle_luggage_capacity,
        v.vehicle_status,
        vi.vehicle_image_path,
        g.general_title AS brand_name
    FROM 
        `ra_tbl_vehicles` v
    LEFT JOIN 
        `ra_tbl_vehicles_images` vi 
        ON v.vehicle_id = vi.vehicle_id 
        AND vi.vehicle_image_order = 1 
        AND vi.vehicle_image_status = 1 
        AND vi.vehicle_image_deleted = 0
    LEFT JOIN 
        `ra_tbl_general` g ON v.vehicle_brand = g.general_id  -- Convert varchar to integer for matching
        AND g.general_group_id = p_vehicle_type
        AND g.general_status = 1
        AND g.general_deleted = 0
    WHERE 
        v.vehicle_customer_id = p_customer_account_id
        AND (p_vehicle_type IS NULL OR v.vehicle_type = p_vehicle_type)
        AND v.vehicle_deleted = 0;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Vehicles List Fetched Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetch_individual_customer_info` (IN `p_customer_account_id` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        -- Log the error
        CALL insert_error_log(
            0,           -- login_id (default 0 as placeholder)
            3,           -- error_side: 3 for DB
            'fetch_individual_customer_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (default 0 as placeholder)
                3,           -- error_side: 3 for DB
                'fetch_individual_customer_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Fetch Personal Info
    SELECT 
        customer_contact_name AS contactName,
        customer_mobile_number AS mobileNumber,
        customer_email_id AS emailId
    FROM ra_tbl_customers
    WHERE customer_account_id = p_customer_account_id
    AND customer_deleted = 0;

    -- Fetch Address Info
    SELECT 
        address_door_no AS doorNo,
        address_street_name AS streetName,
        address_locality AS locality,
        address_city AS city,
        address_district AS district,
        address_state AS state,
        address_country AS country,
        address_postal_code AS postalCode,
        address_latitude AS latitude,
        address_longitude AS longitude
    FROM ra_tbl_customers
    WHERE customer_account_id = p_customer_account_id
    AND customer_deleted = 0;

    -- Fetch Bank Info
    SELECT 
        customer_account_holder_name AS accountHolderName,
        customer_bank_name AS bankName,
        customer_branch_name AS branchName,
        customer_account_number	AS accountNumber,
        customer_ifsc_code AS ifscCode,
        customer_upi_number AS upiNumber
    FROM ra_tbl_customers
    WHERE customer_account_id = p_customer_account_id
    AND customer_deleted = 0;

    -- Fetch Business Info
    SELECT 
        customer_business_name AS businessName,
        customer_business_register_number AS registerNumber,
        customer_business_gstin AS gstin,
        customer_business_logo_path AS logoPath,
        customer_business_license_path AS licensePath
    FROM ra_tbl_customers
    WHERE customer_account_id = p_customer_account_id
    AND customer_deleted = 0;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Details Fetched Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetch_individual_guest_info` (IN `p_guest_account_id` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'fetch_individual_guest_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'fetch_individual_guest_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Fetch Guest Personal Info
    SELECT 
        guest_name AS guest_name,
        guest_mobile_number AS guest_mobile_number,
        guest_email_id AS guest_email_id
    FROM ra_tbl_guests
    WHERE guest_id = p_guest_account_id
    AND guest_status = 1
    AND guest_deleted = 0;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Guest Details Fetched Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_error_log` (IN `p_login_id` INT, IN `p_error_side` TINYINT(1), IN `p_activity_page` VARCHAR(255), IN `p_error_message` VARCHAR(255))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE last_inserted_id INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        SET warning_count = warning_count + 1;
        GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
    END;

    -- Insert error log
    INSERT INTO ra_tbl_error_logs (
        login_id,
        error_side,
        activity_page,
        error_message
    ) VALUES (
        p_login_id,
        p_error_side,
        p_activity_page,
        p_error_message
    );


    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Error log created with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Error log created successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `login_log` (IN `p_username` VARCHAR(255), IN `p_account_id` INT, IN `p_user_ip_address` VARCHAR(50), IN `p_successful_login` TINYINT, IN `p_login_status` TINYINT, IN `p_account_portal_type` TINYINT, IN `p_type` TINYINT, IN `p_login_id` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE record_exists INT DEFAULT 0;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            p_login_id,       -- Use p_login_id instead of 0
            3,                -- error_side: 3 for DB
            'login_log',      -- activity_page: procedure name
            error_message     -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                p_login_id,       -- Use p_login_id instead of 0
                3,                -- error_side: 3 for DB
                'login_log',      -- activity_page: procedure name
                warning_message   -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Handle different p_type values
    IF p_type = 3 THEN
        -- Update existing login record for p_type = 3
        UPDATE `ra_tbl_login_logs`
        SET 
            `login_status` = 1,
            `logout_time` = CURRENT_TIMESTAMP,
            `updated_by` = p_account_id
        WHERE `log_id` = p_login_id;
    ELSE
        -- Insert new login log into ra_tbl_login_logs for other p_type values
        INSERT INTO `ra_tbl_login_logs` (
            `account_id`,
            `user_ip_address`,
            `successful_login`,
            `login_status`,
            `created_by`,
            `updated_by`
        )
        VALUES (
            p_account_id,
            p_user_ip_address,
            p_successful_login,
            p_login_status,
            p_account_id, -- Assuming created_by is the account_id
            p_account_id  -- Assuming updated_by is the account_id
        );
    END IF;

    -- Fetch user details only if p_type = 1
    IF p_type = 1 THEN
        IF p_account_portal_type = 1 THEN
            -- Customer (portal type = 1)
            SELECT 
                `customer_business_name` AS name,
                `customer_id` AS user_id,
                `customer_business_logo_path` AS image_url
            FROM `ra_tbl_customers`
            WHERE `customer_account_id` = p_account_id
            AND `customer_status` = 1 -- Active
            AND `customer_deleted` = 0 -- Not Deleted
            LIMIT 1;

        ELSEIF p_account_portal_type = 2 THEN
            -- Guest (portal type = 2)
            SELECT 
                `guest_name` AS name,
                `guest_id` AS user_id,
                '' AS image_url
            FROM `ra_tbl_guests`
            WHERE `guest_id` = p_account_id -- Assuming guest_id matches account_id
            AND `guest_status` = 1 -- Active
            AND `guest_deleted` = 0 -- Not Deleted
            LIMIT 1;

        ELSE
            -- For other portal types (3, 4, 5), return empty defaults
            SELECT 
                '' AS name,
                '' AS image_url;
        END IF;
    END IF;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               IF(p_type = 3, 'Login status updated successfully', 'Login logged successfully') AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_address_info` (IN `p_customer_account_id` INT, IN `p_doorNo` VARCHAR(50), IN `p_streetName` VARCHAR(255), IN `p_locality` VARCHAR(255), IN `p_city` VARCHAR(100), IN `p_district` VARCHAR(100), IN `p_state` VARCHAR(100), IN `p_country` VARCHAR(100), IN `p_postalCode` VARCHAR(20), IN `p_latitude` VARCHAR(50), IN `p_longitude` VARCHAR(50))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE record_exists INT DEFAULT 0;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_customer_address_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_customer_address_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

        -- Update existing record with address info including district
        UPDATE ra_tbl_customers
        SET 
            address_door_no = p_doorNo,
            address_street_name = p_streetName,
            address_locality = p_locality,
            address_city = p_city,
            address_district = p_district,  -- Added district mapping
            address_state = p_state,
            address_country = p_country,
            address_postal_code = p_postalCode,
            address_latitude = p_latitude,
            address_longitude = p_longitude,
            updated_at = CURRENT_TIMESTAMP
        WHERE customer_account_id = p_customer_account_id
        AND customer_status = 1
        AND customer_deleted = 0;
 

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Address Info Updated Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_bank_info` (IN `p_customer_account_id` INT, IN `p_accountHolderName` VARCHAR(255), IN `p_bankName` VARCHAR(255), IN `p_branchName` VARCHAR(255), IN `p_ifscCode` VARCHAR(20), IN `p_upiNumber` VARCHAR(50), IN `p_accountNumber` VARCHAR(255))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE record_exists INT DEFAULT 0;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_customer_bank_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_customer_bank_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

        -- Update existing record with bank info including account number
        UPDATE ra_tbl_customers
        SET 
            customer_account_holder_name = p_accountHolderName,
            customer_bank_name = p_bankName,
            customer_branch_name = p_branchName,
            customer_ifsc_code = p_ifscCode,
            customer_upi_number = p_upiNumber,
            customer_account_number = p_accountNumber, -- Added new field
            updated_at = CURRENT_TIMESTAMP
        WHERE customer_account_id = p_customer_account_id
        AND customer_status = 1
        AND customer_deleted = 0;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Bank Info Updated Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_business_info` (IN `p_customer_account_id` INT, IN `p_businessName` VARCHAR(255), IN `p_registerNumber` VARCHAR(50), IN `p_gstin` VARCHAR(50), IN `p_logo_link` TEXT, IN `p_license_link` TEXT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE record_exists INT DEFAULT 0;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_customer_business_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_customer_business_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;


    UPDATE ra_tbl_customers
    SET 
        customer_business_name = p_businessName,
        customer_business_register_number = p_registerNumber,
        customer_business_gstin = p_gstin,
        customer_business_logo_path = p_logo_link,
        customer_business_license_path = p_license_link
    WHERE customer_account_id = p_customer_account_id
    AND customer_status = 1
    AND customer_deleted = 0;


    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Business Info Updated Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_personal_info` (IN `p_customer_account_id` INT, IN `p_contactName` VARCHAR(255), IN `p_mobileNumber` VARCHAR(15), IN `p_emailId` VARCHAR(255))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE record_exists INT DEFAULT 0;

    -- Variables to hold existing record data
    DECLARE v_address_door_no VARCHAR(50);
    DECLARE v_address_street_name VARCHAR(255);
    DECLARE v_address_locality VARCHAR(255);
    DECLARE v_address_city VARCHAR(100);
    DECLARE v_address_district VARCHAR(100);
    DECLARE v_address_state VARCHAR(100);
    DECLARE v_address_country VARCHAR(100);
    DECLARE v_address_postal_code VARCHAR(20);
    DECLARE v_address_latitude VARCHAR(50);
    DECLARE v_address_longitude VARCHAR(50);
    DECLARE v_customer_account_holder_name VARCHAR(255);
    DECLARE v_customer_bank_name VARCHAR(255);
    DECLARE v_customer_branch_name VARCHAR(255);
    DECLARE v_customer_ifsc_code VARCHAR(20);
    DECLARE v_customer_upi_number VARCHAR(50);
    DECLARE v_customer_business_name VARCHAR(255);
    DECLARE v_customer_business_register_number VARCHAR(50);
    DECLARE v_customer_business_gstin VARCHAR(50);
    DECLARE v_customer_business_logo_path VARCHAR(255);
    DECLARE v_customer_business_license_path VARCHAR(255);
    DECLARE v_created_by INT;
    DECLARE v_updated_by INT;
    DECLARE v_created_at TIMESTAMP;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_customer_personal_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_customer_personal_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Check if a record exists with the given customer_account_id
    SELECT COUNT(*) INTO record_exists
    FROM ra_tbl_customers
    WHERE customer_account_id = p_customer_account_id
    AND customer_status = 1
    AND customer_deleted = 0;

    IF record_exists > 0 THEN
        -- Fetch existing record data
        SELECT 
            address_door_no,
            address_street_name,
            address_locality,
            address_city,
            address_district,
            address_state,
            address_country,
            address_postal_code,
            address_latitude,
            address_longitude,
            customer_account_holder_name,
            customer_bank_name,
            customer_branch_name,
            customer_ifsc_code,
            customer_upi_number,
            customer_business_name,
            customer_business_register_number,
            customer_business_gstin,
            customer_business_logo_path,
            customer_business_license_path,
            created_by,
            updated_by,
            created_at
        INTO 
            v_address_door_no,
            v_address_street_name,
            v_address_locality,
            v_address_city,
            v_address_district,
            v_address_state,
            v_address_country,
            v_address_postal_code,
            v_address_latitude,
            v_address_longitude,
            v_customer_account_holder_name,
            v_customer_bank_name,
            v_customer_branch_name,
            v_customer_ifsc_code,
            v_customer_upi_number,
            v_customer_business_name,
            v_customer_business_register_number,
            v_customer_business_gstin,
            v_customer_business_logo_path,
            v_customer_business_license_path,
            v_created_by,
            v_updated_by,
            v_created_at
        FROM ra_tbl_customers
        WHERE customer_account_id = p_customer_account_id
        AND customer_status = 1
        AND customer_deleted = 0
        LIMIT 1;

        -- Update existing record to inactive
        UPDATE ra_tbl_customers
        SET customer_status = 2,
            updated_at = CURRENT_TIMESTAMP
        WHERE customer_account_id = p_customer_account_id
        AND customer_status = 1
        AND customer_deleted = 0;

        -- Insert new record with updated personal info and copied other fields
        INSERT INTO ra_tbl_customers (
            customer_account_id,
            customer_contact_name,
            customer_mobile_number,
            customer_email_id,
            address_door_no,
            address_street_name,
            address_locality,
            address_city,
            address_district,
            address_state,
            address_country,
            address_postal_code,
            address_latitude,
            address_longitude,
            customer_account_holder_name,
            customer_bank_name,
            customer_branch_name,
            customer_ifsc_code,
            customer_upi_number,
            customer_business_name,
            customer_business_register_number,
            customer_business_gstin,
            customer_business_logo_path,
            customer_business_license_path,
            customer_status,
            customer_deleted,
            created_by,
            updated_by,
            created_at,
            updated_at
        ) VALUES (
            p_customer_account_id,
            p_contactName,
            p_mobileNumber,
            p_emailId,
            v_address_door_no,
            v_address_street_name,
            v_address_locality,
            v_address_city,
            v_address_district,
            v_address_state,
            v_address_country,
            v_address_postal_code,
            v_address_latitude,
            v_address_longitude,
            v_customer_account_holder_name,
            v_customer_bank_name,
            v_customer_branch_name,
            v_customer_ifsc_code,
            v_customer_upi_number,
            v_customer_business_name,
            v_customer_business_register_number,
            v_customer_business_gstin,
            v_customer_business_logo_path,
            v_customer_business_license_path,
            1,  -- Active status for new record
            0,  -- Not deleted
            v_created_by,
            v_updated_by,
            v_created_at,
            CURRENT_TIMESTAMP
        );
    ELSE
        -- Insert new record with provided personal info and default values for other fields
        INSERT INTO ra_tbl_customers (
            customer_account_id,
            customer_contact_name,
            customer_mobile_number,
            customer_email_id,
            customer_business_name,
            customer_business_register_number,
            customer_status,
            customer_deleted,
            created_at,
            updated_at
        ) VALUES (
            p_customer_account_id,
            p_contactName,
            p_mobileNumber,
            p_emailId,
            '',  -- Default empty business name (required field)
            '',  -- Default empty register number (required field)
            1,   -- Active status
            0,   -- Not deleted
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END IF;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Customer Personal Info Updated Successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_vehicle_basic_info` (IN `p_vehicle_id` INT, IN `p_vehicle_customer_id` INT, IN `p_vehicle_type` TINYINT(1), IN `p_vehicle_title` VARCHAR(255), IN `p_vehicle_brand` INT, IN `p_vehicle_model` VARCHAR(100), IN `p_vehicle_category` TINYINT(1), IN `p_vehicle_color` VARCHAR(50), IN `p_vehicle_seating_capacity` INT, IN `p_vehicle_luggage_capacity` INT, IN `p_vehicle_number` VARCHAR(50), IN `p_rc_link` TEXT)   BEGIN
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE p_new_vehicle_id INT;
    DECLARE existing_count INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(0, 3, 'update_customer_vehicle_basic_info', error_message);
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;
    
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN 
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(0, 3, 'update_customer_vehicle_basic_info', warning_message);
        END IF;
    END;

    -- Check if the existing record has any changes (including rc_path)
    SELECT COUNT(*) INTO existing_count FROM `ra_tbl_vehicles`
    WHERE `vehicle_id` = p_vehicle_id
    AND `vehicle_customer_id` = p_vehicle_customer_id
    AND `vehicle_type` = p_vehicle_type
    AND `vehicle_title` = p_vehicle_title
    AND `vehicle_brand` = p_vehicle_brand
    AND `vehicle_model` = p_vehicle_model
    AND `vehicle_category` = p_vehicle_category
    AND `vehicle_color` = p_vehicle_color
    AND `vehicle_seating_capacity` = p_vehicle_seating_capacity
    AND `vehicle_luggage_capacity` = p_vehicle_luggage_capacity
    AND `vehicle_number` = p_vehicle_number
    AND (`rc_path` = p_rc_link OR (`rc_path` IS NULL AND p_rc_link IS NULL))
    AND `vehicle_deleted` = 0;

    IF existing_count > 0 THEN
        -- No changes detected
        SELECT p_vehicle_id AS new_vehicle_id;
        SELECT 200 AS status_code, 'success' AS status, 'No changes detected' AS message;
    ELSE
        -- If p_vehicle_id = 0, insert new record
        IF p_vehicle_id = 0 THEN
            INSERT INTO `ra_tbl_vehicles` (
                `vehicle_customer_id`, `vehicle_type`, `vehicle_title`, `vehicle_brand`, `vehicle_model`, 
                `vehicle_category`, `vehicle_color`, `vehicle_seating_capacity`, `vehicle_luggage_capacity`, 
                `vehicle_number`, `rc_path`, `created_by`, `updated_by`
            ) VALUES (
                p_vehicle_customer_id, p_vehicle_type, p_vehicle_title, p_vehicle_brand, p_vehicle_model, 
                p_vehicle_category, p_vehicle_color, p_vehicle_seating_capacity, p_vehicle_luggage_capacity, 
                p_vehicle_number, p_rc_link, p_vehicle_customer_id, p_vehicle_customer_id
            );
            SET p_new_vehicle_id = LAST_INSERT_ID();
        ELSE
            -- Update existing record to inactive
            UPDATE `ra_tbl_vehicles`
            SET `vehicle_status` = 2, `vehicle_deleted` = 1, `updated_by` = p_vehicle_customer_id, `updated_at` = CURRENT_TIMESTAMP
            WHERE `vehicle_id` = p_vehicle_id AND `vehicle_deleted` = 0;
            
            -- Insert new record
            INSERT INTO `ra_tbl_vehicles` (
                `vehicle_customer_id`, `vehicle_type`, `vehicle_title`, `vehicle_brand`, `vehicle_model`, 
                `vehicle_category`, `vehicle_color`, `vehicle_seating_capacity`, `vehicle_luggage_capacity`, 
                `vehicle_number`, `rc_path`, `created_by`, `updated_by`
            ) VALUES (
                p_vehicle_customer_id, p_vehicle_type, p_vehicle_title, p_vehicle_brand, p_vehicle_model, 
                p_vehicle_category, p_vehicle_color, p_vehicle_seating_capacity, p_vehicle_luggage_capacity, 
                p_vehicle_number, p_rc_link, p_vehicle_customer_id, p_vehicle_customer_id
            );
            SET p_new_vehicle_id = LAST_INSERT_ID();
        END IF;
    
        -- Return inserted/updated ID
        SELECT p_new_vehicle_id AS new_vehicle_id;
        
        -- Return status message
        IF warning_count > 0 THEN
            SELECT 300 AS status_code, 'warning' AS status, CONCAT('Vehicle Updated with ', warning_count, ' warning(s): ', warning_message) AS message;
        ELSE
            SELECT 200 AS status_code, 'success' AS status, 'Vehicle Updated successfully' AS message;
        END IF;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_vehicle_images_info` (IN `p_vehicle_id` INT, IN `p_image_link` JSON)   BEGIN
    DECLARE image_count INT DEFAULT 0;
    DECLARE i INT DEFAULT 1;
    DECLARE single_image VARCHAR(255);
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE username_exists INT DEFAULT 0;
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        -- Log the error
        CALL insert_error_log(
            0,           -- login_id from input parameter
            3,                    -- error_side: 3 for DB
            'update_customer_vehicle_images_info', -- activity_page: procedure name
            error_message         -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            -- Log the warning
            CALL insert_error_log(
                0,           -- login_id from input parameter
                3,                    -- error_side: 3 for DB
                'update_customer_vehicle_images_info', -- activity_page: procedure name
                warning_message       -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Step 1: Update existing records for the vehicle_id to inactive status
    UPDATE `ra_tbl_vehicles_images`
    SET `vehicle_image_status` = 2, -- Inactive
        `vehicle_image_deleted` = 1,
        `updated_at` = CURRENT_TIMESTAMP
    WHERE `vehicle_id` = p_vehicle_id
    AND `vehicle_image_deleted` = 0;

    -- Step 2: Decode JSON and insert new records
    -- Get the number of images in the JSON array
    SET image_count = JSON_LENGTH(p_image_link);

    -- Loop through the JSON array and insert each image
    WHILE i <= image_count DO
        -- Extract each image path from the JSON array (index starts at 0)
        SET single_image = JSON_UNQUOTE(JSON_EXTRACT(p_image_link, CONCAT('$[', i-1, ']')));

        -- Insert new record
        INSERT INTO `ra_tbl_vehicles_images` (
            `vehicle_id`,
            `vehicle_image_path`,
            `vehicle_image_order`,
            `vehicle_image_status`,
            `vehicle_image_deleted`,
            `created_by`,
            `updated_by`,
            `created_at`,
            `updated_at`
        ) VALUES (
            p_vehicle_id,
            single_image,
            i, -- Incremental order starting from 1
            1, -- Active
            0, -- Not deleted
            NULL, -- Assuming created_by is optional; adjust if you have a value
            NULL, -- Assuming updated_by is optional; adjust if you have a value
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Increment the loop counter
        SET i = i + 1;
    END WHILE;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Vehicle Images Updated with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               'Vehicle Images Updated successfully' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_vehicle_slots_info` (IN `p_input` JSON)   BEGIN
    DECLARE slot_count INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE vehicle_id_val INT;
    DECLARE slot_duration_val VARCHAR(50);
    DECLARE duration_type_val TINYINT;
    DECLARE slot_price_val DECIMAL(10,2);
    DECLARE slot_id_val INT;
    DECLARE existing_count INT DEFAULT 0;
    
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    
    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(0, 3, 'update_customer_vehicle_slots_info', error_message);
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;
    
    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(0, 3, 'update_customer_vehicle_slots_info', warning_message);
        END IF;
    END;
    
    -- Get the vehicle_id from the first element
    SET vehicle_id_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, '$[0].vehicle_id'));

    -- Step 1: Check if the same records already exist
    SELECT COUNT(*) INTO existing_count FROM `ra_tbl_vehicles_slots`
    WHERE `vehicle_id` = vehicle_id_val
    AND `vehicle_slot_status` = 1
    AND `vehicle_slot_deleted` = 0;
    
    IF existing_count = JSON_LENGTH(p_input) THEN
        -- No changes detected
        SELECT 200 AS status_code, 'success' AS status, 'No changes detected' AS message;
    ELSE
        -- Step 2: Update existing slots to inactive
        UPDATE `ra_tbl_vehicles_slots`
        SET `vehicle_slot_status` = 2,`vehicle_slot_deleted` = 1, `updated_at` = CURRENT_TIMESTAMP
        WHERE `vehicle_id` = vehicle_id_val AND `vehicle_slot_deleted` = 0;

        -- Step 3: Insert new records
        SET slot_count = JSON_LENGTH(p_input);

        WHILE i < slot_count DO
            SET vehicle_id_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, CONCAT('$[', i, '].vehicle_id')));
            SET slot_duration_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, CONCAT('$[', i, '].vehicle_slot_duration')));
            SET duration_type_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, CONCAT('$[', i, '].vehicle_slot_duration_type')));
            SET slot_price_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, CONCAT('$[', i, '].vehicle_slot_price')));
            SET slot_id_val = JSON_UNQUOTE(JSON_EXTRACT(p_input, CONCAT('$[', i, '].vehicle_slots_id')));

            INSERT INTO `ra_tbl_vehicles_slots` (
                `vehicle_id`, `vehicle_slot_duration`, `vehicle_slot_duration_type`,
                `vehicle_slot_price`, `vehicle_slot_status`, `vehicle_slot_deleted`
            ) VALUES (
                CAST(vehicle_id_val AS UNSIGNED), slot_duration_val,
                CAST(duration_type_val AS UNSIGNED), CAST(slot_price_val AS DECIMAL(10,2)),
                1, 0
            );

            SET i = i + 1;
        END WHILE;

        -- Return status message
        IF warning_count > 0 THEN
            SELECT 300 AS status_code, 'warning' AS status, CONCAT('Vehicle Slots Updated with ', warning_count, ' warning(s): ', warning_message) AS message;
        ELSE
            SELECT 200 AS status_code, 'success' AS status, 'Vehicle Slots Updated successfully' AS message;
        END IF;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_customer_vehicle_status` (IN `p_vehicleId` INT, IN `p_isActive` INT)   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_customer_vehicle_status', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_customer_vehicle_status', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Update vehicle status in ra_tbl_vehicles
    UPDATE `ra_tbl_vehicles`
    SET `vehicle_status` = p_isActive
    WHERE 
        `vehicle_id` = p_vehicleId
        AND `vehicle_deleted` = 0;

    -- Check if any rows were affected
    IF ROW_COUNT() = 0 THEN
        SELECT 404 AS status_code, 
               'error' AS status, 
               CONCAT('Vehicle with ID ', p_vehicleId, ' not found or already deleted') AS message;
    ELSE
        -- Return success message
        IF warning_count > 0 THEN
            SELECT 300 AS status_code, 
                   'warning' AS status, 
                   CONCAT('Vehicle status updated with ', warning_count, ' warning(s): ', warning_message) AS message;
        ELSE
            SELECT 200 AS status_code, 
                   'success' AS status, 
                   CONCAT('Vehicle status updated successfully for vehicle ID ', p_vehicleId) AS message;
        END IF;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_guest_personal_info` (IN `p_guest_account_id` INT, IN `p_guestName` VARCHAR(255), IN `p_guestMobileNumber` VARCHAR(15), IN `p_guestEmailId` VARCHAR(255))   BEGIN
    -- Declare variables for error and warning handling
    DECLARE error_code VARCHAR(5);
    DECLARE error_message VARCHAR(255);
    DECLARE warning_count INT DEFAULT 0;
    DECLARE warning_message VARCHAR(255) DEFAULT '';
    DECLARE warning_errno INT;
    DECLARE rows_affected INT DEFAULT 0;

    -- Error handling block
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_code = RETURNED_SQLSTATE, error_message = MESSAGE_TEXT;
        CALL insert_error_log(
            0,           -- login_id (placeholder)
            3,           -- error_side: 3 for DB
            'update_guest_personal_info', -- activity_page: procedure name
            error_message -- error_message from diagnostics
        );
        SELECT 500 AS status_code, 'error' AS status, error_message AS message;
    END;

    -- Warning handling block
    DECLARE CONTINUE HANDLER FOR SQLWARNING
    BEGIN
        GET DIAGNOSTICS CONDITION 1 warning_errno = MYSQL_ERRNO;
        IF warning_errno != 1329 THEN  -- Ignore "No data" warning
            SET warning_count = warning_count + 1;
            GET DIAGNOSTICS CONDITION 1 warning_message = MESSAGE_TEXT;
            CALL insert_error_log(
                0,           -- login_id (placeholder)
                3,           -- error_side: 3 for DB
                'update_guest_personal_info', -- activity_page: procedure name
                warning_message -- warning_message from diagnostics
            );
        END IF;
    END;

    -- Update the guest personal information
    UPDATE ra_tbl_guests 
    SET 
        guest_name = p_guestName,
        guest_mobile_number = p_guestMobileNumber,
        guest_email_id = p_guestEmailId,
        updated_at = CURRENT_TIMESTAMP
    WHERE 
        guest_id = p_guest_account_id
        AND guest_deleted = 0
        AND guest_status = 1;

    -- Get the number of rows affected by the update
    SET rows_affected = ROW_COUNT();

    -- If no rows were updated, insert a new record
    IF rows_affected = 0 THEN
        INSERT INTO ra_tbl_guests (
            guest_id,
            guest_name,
            guest_mobile_number,
            guest_email_id,
            guest_status,
            guest_deleted,
            created_at,
            updated_at
        ) VALUES (
            p_guest_account_id,
            p_guestName,
            p_guestMobileNumber,
            p_guestEmailId,
            1,           -- Active status
            0,           -- Not deleted
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END IF;

    -- Return status message
    IF warning_count > 0 THEN
        SELECT 300 AS status_code, 
               'warning' AS status, 
               CONCAT('Query Executed with ', warning_count, ' warning(s): ', warning_message) AS message;
    ELSE
        SELECT 200 AS status_code, 
               'success' AS status, 
               IF(rows_affected > 0, 'Guest Personal Info Updated Successfully', 'Guest Personal Info Inserted Successfully') AS message;
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_accounts`
--

CREATE TABLE `ra_tbl_accounts` (
  `account_id` int(11) NOT NULL,
  `account_username` varchar(255) NOT NULL,
  `account_portal_type` tinyint(1) NOT NULL COMMENT '1 - Customer, 2 - Guest, 3 - Marketing, 4 - Admin, 5 - Developer',
  `account_code` varchar(50) NOT NULL,
  `master_key` text DEFAULT NULL,
  `salt_value` varchar(255) DEFAULT NULL,
  `iterations_value` int(11) DEFAULT NULL,
  `iv_value` varchar(255) DEFAULT NULL,
  `account_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `account_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ra_tbl_accounts`
--

INSERT INTO `ra_tbl_accounts` (`account_id`, `account_username`, `account_portal_type`, `account_code`, `master_key`, `salt_value`, `iterations_value`, `iv_value`, `account_status`, `account_deleted`, `created_at`, `updated_at`) VALUES
(1, 'ajay', 1, 'V-1', 'iot/oMqea2eFoAnIcngGA90WpqVPTKRWrS3gmcdMqRxegaXovUQw3hndcZkBuFVq', 'UWo9OdIwvQ40PAQAPlg2vQ==', 20697, '/AuQTOBKiU72jNNwhR8WxQ==', 1, 0, '2025-02-27 15:17:19', '2025-02-27 15:17:19'),
(2, 'thiru', 2, 'C-1', 'UK/6N1hYgzVXtkc2RFTt6rRl9CGNlTnPl30cmfMV0kmFLB9ShBly9btZgZ/mH5EK', 'XeldMUg27F7xGNaNaPIi8g==', 10614, 'wgwWMD/ctA/wplM8+2b2hw==', 1, 0, '2025-02-28 04:38:55', '2025-02-28 04:38:55'),
(4, 'jeni', 1, 'V-2', '2FZ+qHwqWBQcEzZirGwLir08Pbe7PLAQ7n77WGh6ES9bqa0DKcVpWrt2taII+Aoe', 'H4YthuxWiBrZc2xYQ0LNhg==', 22096, '+KHp0m9jZz+rIqs0jkTDpw==', 1, 0, '2025-02-28 04:47:58', '2025-02-28 04:47:58'),
(5, 'vady', 1, 'V-3', 'nV47mGPMKoLw6qoYTyGq/xyVE2FLS4GhU6KStumT/SqamxFqN5LX2wdwt2aUfUSt', 'uQTTvIVqPGxi1RW7ZjrAtQ==', 38635, 'GfBVTXJWKm57qdOJd+qiBg==', 1, 0, '2025-02-28 14:10:45', '2025-02-28 14:10:45');

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
  `customer_account_number` varchar(255) DEFAULT NULL,
  `customer_ifsc_code` varchar(20) DEFAULT NULL,
  `customer_upi_number` varchar(50) DEFAULT NULL,
  `customer_business_name` varchar(255) DEFAULT NULL,
  `customer_business_register_number` varchar(50) DEFAULT NULL,
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

--
-- Dumping data for table `ra_tbl_customers`
--

INSERT INTO `ra_tbl_customers` (`customer_id`, `customer_account_id`, `customer_contact_name`, `customer_mobile_number`, `customer_email_id`, `address_door_no`, `address_street_name`, `address_locality`, `address_city`, `address_district`, `address_state`, `address_country`, `address_postal_code`, `address_latitude`, `address_longitude`, `customer_account_holder_name`, `customer_bank_name`, `customer_branch_name`, `customer_account_number`, `customer_ifsc_code`, `customer_upi_number`, `customer_business_name`, `customer_business_register_number`, `customer_business_gstin`, `customer_business_logo_path`, `customer_business_license_path`, `customer_status`, `customer_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(1, 1, 'Ajay', '8754857015', 'ajay2kkid@gmail.com', '9', 'VOC Street', 'Santhi Nagar', 'Lawspet', 'Puducherry', 'Puducherry', 'India', '605008', '11.9620575', '79.8163834', 'Ajay S', 'Indian Overseas Bank', 'Lawspet', 'ajayshanmugavel2002@gmail', '828262528385', 'djcuv7e83828wj', 'Dewiz IT', 'DHH4DS35G4', '5RG43S5G4SSD', 'a.jpeg', 'Brain Storm InfoTech Solutions - 7282udhdusi - BUSINESS_LICENSE - license_1740718780338-28-02-2025 10 32 06 AM.pdf', 1, 0, NULL, NULL, '2025-02-27 15:17:28', '2025-03-04 04:51:56'),
(3, 4, 'Jenifer', '9629805430', 'jeni@gmail.com', '11', 'College Road', 'Ariyur', 'Pangur', 'Ariyur', 'Puducherry', 'India', '605110', '11.904566', '79.701347', 'Jenifer', 'Indian Bank', 'Pangur', 'jeni@gmail', '1738292920384', 'IB739392', 'Brain Storm InfoTech Solutions', '7282udhdusi', '277wdhe77', 'Brain Storm InfoTech Solutions - 7282udhdusi - BUSINESS_LOGO - logo_1740718776001-28-02-2025 10 32 06 AM.jpeg', 'Brain Storm InfoTech Solutions - 7282udhdusi - BUSINESS_LICENSE - license_1740718780338-28-02-2025 10 32 06 AM.pdf', 1, 0, NULL, NULL, '2025-02-28 04:48:22', '2025-02-28 05:02:06'),
(4, 5, 'Sarasvathy', '9629862944', 'ajay2kkid@gmail.com', '11', 'VOC Street', 'Santhi Nagar', 'Lawspet', 'Puducherry', 'Puducherry', 'India', '605008', '11.9620533', '79.8164098', 'shsj', 'sghshs', 'svhsus7', 'w7x7xhwh', 'yw72727272', 'ys6w62', 'Vady', 'ywyw7', 'wuw77sy', 'Vady - ywyw7 - BUSINESS_LOGO - logo_1740751966556-28-02-2025 07 43 04 PM.jpeg', 'Vady - ywyw7 - BUSINESS_LICENSE - license_1740751973252-28-02-2025 07 43 04 PM.pdf', 1, 0, NULL, NULL, '2025-02-28 14:11:06', '2025-02-28 14:13:04');

-- --------------------------------------------------------

--
-- Table structure for table `ra_tbl_error_logs`
--

CREATE TABLE `ra_tbl_error_logs` (
  `error_id` int(11) NOT NULL,
  `login_id` int(11) NOT NULL DEFAULT 0,
  `error_side` tinyint(1) DEFAULT NULL COMMENT '1 - Client, 2 - Server, 3 - DB',
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
  `general_group_id` int(11) NOT NULL COMMENT '1 - Bike, 2 - Car, 3 - Bicycle',
  `general_title` varchar(255) NOT NULL,
  `general_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `general_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ra_tbl_general`
--

INSERT INTO `ra_tbl_general` (`general_id`, `general_group_id`, `general_title`, `general_status`, `general_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(1, 1, 'Royal Enfield', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(2, 1, 'Bajaj Auto', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(3, 1, 'TVS Motor Company', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(4, 1, 'Hero MotoCorp', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(5, 1, 'Mahindra Two Wheelers', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(6, 1, 'Jawa Motorcycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(7, 1, 'Yezdi Motorcycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(8, 1, 'Vespa', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(9, 1, 'Yamaha', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(10, 1, 'Suzuki', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(11, 1, 'Kawasaki', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(12, 1, 'Harley-Davidson', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(13, 1, 'Ducati', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(14, 1, 'KTM', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(15, 1, 'Honda Motorcycle', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(16, 1, 'BMW Motorrad', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(17, 1, 'Triumph', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(18, 1, 'Aprilia', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(19, 1, 'Benelli', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(20, 2, 'Tata Motors', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(21, 2, 'Mahindra & Mahindra', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(22, 2, 'Maruti Suzuki', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(23, 2, 'Ashok Leyland', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(24, 2, 'Force Motors', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(25, 2, 'Hindustan Motors', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(26, 2, 'Eicher Motors', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(27, 2, 'SML Isuzu', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(28, 2, 'Porsche', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(29, 2, 'Morris Garage', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(30, 2, 'Range Rover', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(31, 2, 'Land Rover', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(32, 2, 'Toyota', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(33, 2, 'Honda', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(34, 2, 'Hyundai', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(35, 2, 'Volkswagen', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(36, 2, 'BMW', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(37, 2, 'Mercedes-Benz', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(38, 2, 'Audi', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(39, 2, 'Kia', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(40, 2, 'Jaguar', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(41, 2, 'Volvo', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(42, 2, 'Skoda', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(43, 2, 'Jeep', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(44, 2, 'Renault', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(45, 2, 'Nissan', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(46, 2, 'Ford', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(47, 2, 'Lamborghini', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(48, 3, 'Hero Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(49, 3, 'Atlas Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(50, 3, 'Hercules Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(51, 3, 'BSA Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(52, 3, 'Avon Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(53, 3, 'TI Cycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(54, 3, 'La Sovereign', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(55, 3, 'Giant Bicycles', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(56, 3, 'Trek Bikes', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(57, 3, 'Cannondale', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(58, 3, 'Specialized', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(59, 3, 'Merida Bikes', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(60, 3, 'Scott Sports', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(61, 3, 'Bianchi', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(62, 3, 'Schwinn', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06'),
(63, 3, 'Fuji Bikes', 1, 0, NULL, NULL, '2025-03-10 06:09:06', '2025-03-10 06:09:06');

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

--
-- Dumping data for table `ra_tbl_guests`
--

INSERT INTO `ra_tbl_guests` (`guest_id`, `guest_name`, `guest_mobile_number`, `guest_email_id`, `guest_status`, `guest_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(2, 'Thiruvarasan', '8778652647', 'thiru@gmail.com', 1, 0, NULL, NULL, '2025-02-28 04:43:45', '2025-02-28 04:45:08');

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
  `account_id` int(11) NOT NULL,
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
  `vehicle_brand` int(11) NOT NULL,
  `vehicle_model` varchar(100) NOT NULL,
  `vehicle_category` tinyint(1) DEFAULT NULL COMMENT '1 - Petrol, 2 - Diesel, 3 - CNG, 4 - EV',
  `vehicle_color` varchar(50) NOT NULL,
  `vehicle_seating_capacity` int(11) NOT NULL DEFAULT 0,
  `vehicle_luggage_capacity` int(11) NOT NULL DEFAULT 0,
  `vehicle_number` varchar(50) DEFAULT NULL,
  `rc_path` text DEFAULT NULL,
  `vehicle_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 - Active, 2 - Inactive',
  `vehicle_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 - Not Deleted, 1 - Deleted',
  `created_by` int(11) DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ra_tbl_vehicles`
--

INSERT INTO `ra_tbl_vehicles` (`vehicle_id`, `vehicle_customer_id`, `vehicle_type`, `vehicle_title`, `vehicle_brand`, `vehicle_model`, `vehicle_category`, `vehicle_color`, `vehicle_seating_capacity`, `vehicle_luggage_capacity`, `vehicle_number`, `rc_path`, `vehicle_status`, `vehicle_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 'Pulsar', 4, 'n150', 1, 'black', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-06 05:36:21', '2025-03-06 05:36:21'),
(2, 1, 1, 'FZ', 3, '34', 1, 'yellow', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-06 08:24:40', '2025-03-06 08:24:40'),
(3, 1, 3, 'fdr', 5, '5gt', 3, 'grry', 0, 0, NULL, NULL, 2, 0, 1, 1, '2025-03-06 08:56:58', '2025-03-07 16:48:42'),
(4, 1, 2, 'cddy', 2, 'gfrty', 3, 'vgff', 8, 85, NULL, NULL, 1, 0, 1, 1, '2025-03-06 09:10:41', '2025-03-07 15:37:09'),
(5, 1, 1, 'ddfh', 2, 'gfy6', 2, 'gfgb', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-06 09:27:31', '2025-03-06 09:27:31'),
(6, 1, 3, 'gyt', 4, 'gdr', 4, 'cfr', 0, 0, NULL, NULL, 2, 1, 1, 1, '2025-03-06 11:02:58', '2025-03-12 07:28:27'),
(7, 1, 3, 'wyhdd', 4, 'side', 3, 'song', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-06 11:19:16', '2025-03-06 11:19:16'),
(8, 1, 2, 'gwagon', 5, 'SUV', 4, 'red', 5, 300, NULL, NULL, 1, 0, 1, 1, '2025-03-06 15:48:09', '2025-03-06 15:48:09'),
(9, 1, 2, 'shsh', 4, 'sjjaai', 3, 'dhsjsj', 5, 648, NULL, NULL, 2, 0, 1, 1, '2025-03-06 16:16:14', '2025-03-07 16:48:45'),
(10, 1, 2, 'dhsus7', 4, 'dkdooc8', 4, 'shshsysu', 4, 464, NULL, NULL, 1, 0, 1, 1, '2025-03-06 16:35:36', '2025-03-06 16:35:36'),
(11, 1, 1, 'chhg', 4, 'gtij', 4, 'dhi', 0, 0, NULL, NULL, 2, 1, 1, 1, '2025-03-06 16:41:03', '2025-03-07 16:41:01'),
(12, 1, 1, 'xgt', 4, 'rgg', 3, 'wfg', 0, 0, NULL, NULL, 2, 1, 1, 1, '2025-03-06 16:51:53', '2025-03-07 16:40:37'),
(13, 1, 1, 'fguh', 5, 'rgb', 4, 'ftuj', 0, 0, NULL, NULL, 2, 1, 1, 1, '2025-03-06 16:54:16', '2025-03-12 08:25:48'),
(14, 1, 1, 'fguh', 2, 'guerilla', 1, 'ftuj', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-10 06:11:40', '2025-03-10 06:11:40'),
(41, 1, 3, 'சைக்கிள்', 49, 'gdr', 4, 'cfr', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-12 07:28:27', '2025-03-12 07:28:27'),
(42, 1, 1, 'fguh', 16, 'rgb', 4, 'ftuj', 0, 0, NULL, NULL, 1, 0, 1, 1, '2025-03-12 08:25:48', '2025-03-12 08:25:48'),
(44, 1, 2, 'Seltos', 39, 'X-001', 1, 'Red', 5, 300, 'PY 05 D 1234', 'VEHICLE_RC - certificate_1741847858786-67d27d99012f63.58846286-20250313120921.pdf', 2, 1, 1, 1, '2025-03-13 06:39:21', '2025-03-13 08:50:03'),
(53, 1, 2, 'Seltos', 38, 'X-001', 1, 'Red', 5, 300, 'PY 05 D 1234', 'VEHICLE_RC - certificate_1741847858786-67d27d99012f63.58846286-20250313120921.pdf', 1, 0, 1, 1, '2025-03-13 08:50:03', '2025-03-13 08:50:03'),
(55, 1, 1, 'Road King', 12, 'X40r', 1, 'red', 0, 0, 'Py 01 F 0987', 'VEHICLE_RC - certificate_1741862180766_67d2b6027e13c9_76883161_20250313161002.pdf', 1, 0, 1, 1, '2025-03-13 10:40:02', '2025-03-13 10:40:02'),
(56, 1, 3, 'Triumph', 52, 'A123', 2, 'Black', 0, 0, '1234', 'VEHICLE_RC - certificate_1741932969388_67d3c9aadb060460056775_20250314114610.pdf', 1, 0, 1, 1, '2025-03-14 06:16:10', '2025-03-14 06:16:10');

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

--
-- Dumping data for table `ra_tbl_vehicles_images`
--

INSERT INTO `ra_tbl_vehicles_images` (`vehicles_images_id`, `vehicle_id`, `vehicle_image_path`, `vehicle_image_order`, `vehicle_image_status`, `vehicle_image_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(41, 6, '6 - VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-06-03-2025 04 33 58 PM.jpg', 1, 1, 0, NULL, NULL, '2025-03-06 11:03:58', '2025-03-06 11:03:58'),
(42, 6, '6 - VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-06-03-2025 04 33 58 PM.jpg', 2, 1, 0, NULL, NULL, '2025-03-06 11:03:58', '2025-03-06 11:03:58'),
(43, 6, '6 - VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-06-03-2025 04 33 58 PM.jpg', 3, 1, 0, NULL, NULL, '2025-03-06 11:03:58', '2025-03-06 11:03:58'),
(44, 6, '6 - VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-06-03-2025 04 33 58 PM.jpg', 4, 1, 0, NULL, NULL, '2025-03-06 11:03:58', '2025-03-06 11:03:58'),
(45, 6, '6 - VEHICLE_IMAGE - Screenshot_20241009-084509_Instagram-06-03-2025 04 33 58 PM.jpg', 5, 1, 0, NULL, NULL, '2025-03-06 11:03:58', '2025-03-06 11:03:58'),
(46, 13, '13 - VEHICLE_IMAGE - Screenshot_20250228-103518_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 1, 1, 0, NULL, NULL, '2025-03-06 16:54:44', '2025-03-06 16:54:44'),
(47, 13, '13 - VEHICLE_IMAGE - Screenshot_20250228-103508_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 2, 1, 0, NULL, NULL, '2025-03-06 16:54:44', '2025-03-06 16:54:44'),
(48, 13, '13 - VEHICLE_IMAGE - Screenshot_20250228-193357_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 3, 1, 0, NULL, NULL, '2025-03-06 16:54:44', '2025-03-06 16:54:44'),
(49, 13, '13 - VEHICLE_IMAGE - Screenshot_20250228-103500_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 4, 1, 0, NULL, NULL, '2025-03-06 16:54:44', '2025-03-06 16:54:44'),
(50, 13, '13 - VEHICLE_IMAGE - Screenshot_20250228-103441_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 5, 1, 0, NULL, NULL, '2025-03-06 16:54:44', '2025-03-06 16:54:44'),
(56, 41, '6 - VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-06-03-2025 04 33 58 PM.jpg', 1, 1, 0, NULL, NULL, '2025-03-12 07:28:39', '2025-03-12 07:28:39'),
(57, 41, '6 - VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-06-03-2025 04 33 58 PM.jpg', 2, 1, 0, NULL, NULL, '2025-03-12 07:28:39', '2025-03-12 07:28:39'),
(58, 41, '6 - VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-06-03-2025 04 33 58 PM.jpg', 3, 1, 0, NULL, NULL, '2025-03-12 07:28:39', '2025-03-12 07:28:39'),
(59, 41, '6 - VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-06-03-2025 04 33 58 PM.jpg', 4, 1, 0, NULL, NULL, '2025-03-12 07:28:39', '2025-03-12 07:28:39'),
(60, 41, '6 - VEHICLE_IMAGE - Screenshot_20241009-084509_Instagram-06-03-2025 04 33 58 PM.jpg', 5, 1, 0, NULL, NULL, '2025-03-12 07:28:39', '2025-03-12 07:28:39'),
(61, 42, '13 - VEHICLE_IMAGE - Screenshot_20250228-103518_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 1, 1, 0, NULL, NULL, '2025-03-12 08:25:55', '2025-03-12 08:25:55'),
(62, 42, '13 - VEHICLE_IMAGE - Screenshot_20250228-103508_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 2, 1, 0, NULL, NULL, '2025-03-12 08:25:55', '2025-03-12 08:25:55'),
(63, 42, '13 - VEHICLE_IMAGE - Screenshot_20250228-193357_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 3, 1, 0, NULL, NULL, '2025-03-12 08:25:55', '2025-03-12 08:25:55'),
(64, 42, '13 - VEHICLE_IMAGE - Screenshot_20250228-103500_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 4, 1, 0, NULL, NULL, '2025-03-12 08:25:55', '2025-03-12 08:25:55'),
(65, 42, '13 - VEHICLE_IMAGE - Screenshot_20250228-103441_Vehicle Rental App-06-03-2025 10 24 44 PM.jpg', 5, 1, 0, NULL, NULL, '2025-03-12 08:25:55', '2025-03-12 08:25:55'),
(66, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-67d27dfbc87a76.74761283-20250313121059.jpg', 1, 2, 0, NULL, NULL, '2025-03-13 06:40:59', '2025-03-13 06:43:29'),
(67, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-67d27dfbc9fbe9.07868028-20250313121059.jpg', 2, 2, 0, NULL, NULL, '2025-03-13 06:40:59', '2025-03-13 06:43:29'),
(68, 44, 'VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-67d27dfbcb0147.65193123-20250313121059.jpg', 3, 2, 0, NULL, NULL, '2025-03-13 06:40:59', '2025-03-13 06:43:29'),
(69, 44, 'VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-67d27dfbcc3728.09512364-20250313121059.jpg', 4, 2, 0, NULL, NULL, '2025-03-13 06:40:59', '2025-03-13 06:43:29'),
(70, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-67d27e709426f7.78168443-20250313121256.jpg', 1, 2, 0, NULL, NULL, '2025-03-13 06:42:56', '2025-03-13 06:43:29'),
(71, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-67d27e709586c1.82946383-20250313121256.jpg', 2, 2, 0, NULL, NULL, '2025-03-13 06:42:56', '2025-03-13 06:43:29'),
(72, 44, 'VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-67d27e7096bc48.45710625-20250313121256.jpg', 3, 2, 0, NULL, NULL, '2025-03-13 06:42:56', '2025-03-13 06:43:29'),
(73, 44, 'VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-67d27e709809b4.95257969-20250313121256.jpg', 4, 2, 0, NULL, NULL, '2025-03-13 06:42:56', '2025-03-13 06:43:29'),
(74, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-67d27e911.fa72746616500-20250313121329.jpg', 1, 1, 0, NULL, NULL, '2025-03-13 06:43:29', '2025-03-13 07:09:29'),
(75, 44, 'VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-67d27e9120d927.89680948-20250313121329.jpg', 2, 1, 0, NULL, NULL, '2025-03-13 06:43:29', '2025-03-13 06:43:29'),
(76, 44, 'VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-67d27e9121f9f0.58486790-20250313121329.jpg', 3, 1, 0, NULL, NULL, '2025-03-13 06:43:29', '2025-03-13 06:43:29'),
(77, 44, 'VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-67d27e91233452.25864528-20250313121329.jpg', 4, 1, 0, NULL, NULL, '2025-03-13 06:43:29', '2025-03-13 06:43:29'),
(94, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-67d27e9121f9f0.58486790-20250313121329_67d2a91dd3c823_74747935_20250313151501.jpg', 1, 2, 1, NULL, NULL, '2025-03-13 09:45:01', '2025-03-13 10:34:46'),
(95, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-67d27e911.fa72746616500-20250313121329_67d2a91dd56858_87584883_20250313151501.jpg', 2, 2, 1, NULL, NULL, '2025-03-13 09:45:01', '2025-03-13 10:34:46'),
(96, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-67d27e9120d927.89680948-20250313121329_67d2a91dd653e6_10762247_20250313151501.jpg', 3, 2, 1, NULL, NULL, '2025-03-13 09:45:01', '2025-03-13 10:34:46'),
(97, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-67d27e91233452.25864528-20250313121329_67d2a91dd778e6_21452535_20250313151501.jpg', 4, 2, 1, NULL, NULL, '2025-03-13 09:45:01', '2025-03-13 10:34:46'),
(98, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram-67d27e9121f9f0.58486790-20250313121329_67d2b4c639aa75_48230923_20250313160446.jpg', 1, 1, 0, NULL, NULL, '2025-03-13 10:34:46', '2025-03-13 10:34:46'),
(99, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram-67d27e911.fa72746616500-20250313121329_67d2b4c63b6d48_61458310_20250313160446.jpg', 2, 1, 0, NULL, NULL, '2025-03-13 10:34:46', '2025-03-13 10:34:46'),
(100, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram-67d27e9120d927.89680948-20250313121329_67d2b4c63c9b73_15901508_20250313160446.jpg', 3, 1, 0, NULL, NULL, '2025-03-13 10:34:46', '2025-03-13 10:34:46'),
(101, 53, 'VEHICLE_IMAGE - VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram-67d27e91233452.25864528-20250313121329_67d2b4c63dad47_36652975_20250313160446.jpg', 4, 1, 0, NULL, NULL, '2025-03-13 10:34:46', '2025-03-13 10:34:46'),
(107, 55, 'VEHICLE_IMAGE - _Chillin_ like a true mercenary ? _Unstoppable _Deadpool _MarvelVibes__._._._._ ⸻✧⸻_→ Follow _pixel_muse__ ⸻✧⸻_._._._Hashtags___Deadpool _MarvelComics _MercWithAMouth _AntiHero _C(_67d2b7d803c9c6_90368990_20250313161752.jpg', 1, 1, 0, NULL, NULL, '2025-03-13 10:47:52', '2025-03-13 10:47:52'),
(108, 55, 'VEHICLE_IMAGE - _Chillin_ like a true mercenary ? _Unstoppable _Deadpool _MarvelVibes__._._._._ ⸻✧⸻_→ Follow _pixel_muse__ ⸻✧⸻_._._._Hashtags___Deadpool _MarvelComics _MercWithAMouth _AntiHero __1_67d2b7d804ef11_89952421_20250313161752.jpg', 2, 1, 0, NULL, NULL, '2025-03-13 10:47:52', '2025-03-13 10:47:52'),
(109, 55, 'VEHICLE_IMAGE - Forget the rules of physics_ magic has its own laws ✨_0(JPG)_67d2b7d805cfa7_42782698_20250313161752.jpg', 3, 1, 0, NULL, NULL, '2025-03-13 10:47:52', '2025-03-13 10:47:52'),
(110, 55, 'VEHICLE_IMAGE - A hero forged in steel. ??_0(JPG)_67d2b7d806ca32_60096856_20250313161752.jpg', 4, 1, 0, NULL, NULL, '2025-03-13 10:47:52', '2025-03-13 10:47:52'),
(111, 55, 'VEHICLE_IMAGE - A god_ a symbiote_ a sorcerer_ and a merc... what could go wrong_ ?✨⚔️_._._HD WALLPAPER ? https___linktr.ee_animenature_._Remember to like_ share_ and comment if you enjoyed this piece(_67d2b7d807b156_34194227_20250313161752.jpg', 5, 1, 0, NULL, NULL, '2025-03-13 10:47:52', '2025-03-13 10:47:52'),
(112, 56, 'VEHICLE_IMAGE - Screenshot_20241222-012005_Instagram_67d3c9c934d92282919738_20250314114641.jpg', 1, 1, 0, NULL, NULL, '2025-03-14 06:16:41', '2025-03-14 06:16:41'),
(113, 56, 'VEHICLE_IMAGE - Screenshot_20241222-011416_Instagram_67d3c9c937b58816877653_20250314114641.jpg', 2, 1, 0, NULL, NULL, '2025-03-14 06:16:41', '2025-03-14 06:16:41'),
(114, 56, 'VEHICLE_IMAGE - Screenshot_20241222-005452_Instagram_67d3c9c938dd0815903395_20250314114641.jpg', 3, 1, 0, NULL, NULL, '2025-03-14 06:16:41', '2025-03-14 06:16:41'),
(115, 56, 'VEHICLE_IMAGE - Screenshot_20241222-005321_Instagram_67d3c9c93a301504167894_20250314114641.jpg', 4, 1, 0, NULL, NULL, '2025-03-14 06:16:41', '2025-03-14 06:16:41'),
(116, 56, 'VEHICLE_IMAGE - Screenshot_20241009-084509_Instagram_67d3c9c93bdad677445135_20250314114641.jpg', 5, 1, 0, NULL, NULL, '2025-03-14 06:16:41', '2025-03-14 06:16:41');

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

--
-- Dumping data for table `ra_tbl_vehicles_slots`
--

INSERT INTO `ra_tbl_vehicles_slots` (`vehicle_slots_id`, `vehicle_id`, `vehicle_slot_duration`, `vehicle_slot_duration_type`, `vehicle_slot_price`, `vehicle_slot_status`, `vehicle_slot_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(4, 6, '1.5', 2, 123.00, 1, 0, NULL, NULL, '2025-03-06 11:07:33', '2025-03-06 11:07:33'),
(5, 6, '30.0', 1, 645.00, 1, 0, NULL, NULL, '2025-03-06 11:07:33', '2025-03-06 11:07:33'),
(6, 6, '5.0', 2, 4587.00, 1, 0, NULL, NULL, '2025-03-06 11:07:33', '2025-03-06 11:07:33'),
(7, 13, '1.5', 2, 580.00, 1, 0, NULL, NULL, '2025-03-06 17:02:07', '2025-03-06 17:02:07'),
(8, 13, '30.0', 1, 300.00, 1, 0, NULL, NULL, '2025-03-06 17:02:07', '2025-03-06 17:02:07'),
(9, 13, '5.0', 2, 450.00, 1, 0, NULL, NULL, '2025-03-06 17:02:07', '2025-03-06 17:02:07'),
(13, 41, '1.5', 2, 123.00, 1, 0, NULL, NULL, '2025-03-12 07:28:57', '2025-03-12 07:28:57'),
(14, 41, '30.0', 1, 645.00, 1, 0, NULL, NULL, '2025-03-12 07:28:57', '2025-03-12 07:28:57'),
(15, 41, '5.0', 2, 4587.00, 1, 0, NULL, NULL, '2025-03-12 07:28:57', '2025-03-12 07:28:57'),
(16, 41, '2.6', 2, 855.00, 1, 0, NULL, NULL, '2025-03-12 07:28:57', '2025-03-12 07:28:57'),
(17, 42, '1.5', 2, 580.00, 1, 0, NULL, NULL, '2025-03-12 08:25:58', '2025-03-12 08:25:58'),
(18, 42, '30.0', 1, 300.00, 1, 0, NULL, NULL, '2025-03-12 08:25:58', '2025-03-12 08:25:58'),
(19, 42, '5.0', 2, 450.00, 1, 0, NULL, NULL, '2025-03-12 08:25:58', '2025-03-12 08:25:58'),
(20, 44, '45.0', 1, 800.00, 1, 0, NULL, NULL, '2025-03-13 06:44:39', '2025-03-13 06:44:39'),
(21, 44, '2.5', 2, 5000.00, 1, 0, NULL, NULL, '2025-03-13 06:44:39', '2025-03-13 06:44:39'),
(24, 53, '45.0', 1, 800.00, 1, 0, NULL, NULL, '2025-03-13 08:51:02', '2025-03-13 08:51:02'),
(25, 53, '2.5', 2, 5000.00, 1, 0, NULL, NULL, '2025-03-13 08:51:02', '2025-03-13 08:51:02'),
(26, 55, '90.0', 1, 600.00, 1, 0, NULL, NULL, '2025-03-13 10:48:04', '2025-03-13 10:48:04'),
(27, 56, '50.0', 1, 600.00, 1, 0, NULL, NULL, '2025-03-14 06:16:58', '2025-03-14 06:16:58'),
(28, 56, '2.0', 2, 3000.00, 1, 0, NULL, NULL, '2025-03-14 06:16:58', '2025-03-14 06:16:58');

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
  ADD KEY `user_id` (`account_id`);

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
  ADD KEY `vehicle_customer_id` (`vehicle_customer_id`),
  ADD KEY `fk_vehicle_brand` (`vehicle_brand`);

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
  MODIFY `account_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `ra_tbl_bookings`
--
ALTER TABLE `ra_tbl_bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_customers`
--
ALTER TABLE `ra_tbl_customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `ra_tbl_error_logs`
--
ALTER TABLE `ra_tbl_error_logs`
  MODIFY `error_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ra_tbl_general`
--
ALTER TABLE `ra_tbl_general`
  MODIFY `general_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=64;

--
-- AUTO_INCREMENT for table `ra_tbl_guests`
--
ALTER TABLE `ra_tbl_guests`
  MODIFY `guest_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

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
  MODIFY `vehicle_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT for table `ra_tbl_vehicles_images`
--
ALTER TABLE `ra_tbl_vehicles_images`
  MODIFY `vehicles_images_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=117;

--
-- AUTO_INCREMENT for table `ra_tbl_vehicles_slots`
--
ALTER TABLE `ra_tbl_vehicles_slots`
  MODIFY `vehicle_slots_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

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
  ADD CONSTRAINT `ra_tbl_login_logs_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `ra_tbl_accounts` (`account_id`);

--
-- Constraints for table `ra_tbl_user_activity_logs`
--
ALTER TABLE `ra_tbl_user_activity_logs`
  ADD CONSTRAINT `ra_tbl_user_activity_logs_ibfk_1` FOREIGN KEY (`login_id`) REFERENCES `ra_tbl_login_logs` (`log_id`);

--
-- Constraints for table `ra_tbl_vehicles`
--
ALTER TABLE `ra_tbl_vehicles`
  ADD CONSTRAINT `fk_vehicle_brand` FOREIGN KEY (`vehicle_brand`) REFERENCES `ra_tbl_general` (`general_id`) ON DELETE CASCADE ON UPDATE CASCADE,
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
