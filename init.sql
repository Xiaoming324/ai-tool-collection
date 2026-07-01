CREATE DATABASE ai_tool_collection CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ai_tool_collection;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`   VARCHAR(50)  NOT NULL UNIQUE,
    `password`   VARCHAR(100) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_session`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `type`       VARCHAR(20) NOT NULL,
    `chat_id`    VARCHAR(64) NOT NULL,
    `title`      VARCHAR(255)         DEFAULT NULL,
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_type_chat` (`user_id`, `type`, `chat_id`),
    KEY `idx_user_type_updated` (`user_id`, `type`, `updated_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_message`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `session_id`   BIGINT      NOT NULL,
    `user_id`      BIGINT      NOT NULL,
    `role`         VARCHAR(20) NOT NULL,
    `sequence_no`  INT         NOT NULL,
    `text_content` MEDIUMTEXT           DEFAULT NULL,
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_session_sequence` (`session_id`, `sequence_no`),
    KEY `idx_user_session` (`user_id`, `session_id`),
    KEY `idx_session_created` (`session_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `stored_file`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `session_id`        BIGINT       NOT NULL,
    `file_kind`         VARCHAR(20)  NOT NULL,
    `original_filename` VARCHAR(255) NOT NULL,
    `content_type`      VARCHAR(100) NOT NULL,
    `size_bytes`        BIGINT       NOT NULL,
    `s3_bucket`         VARCHAR(100) NOT NULL,
    `s3_key`            VARCHAR(512) NOT NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_bucket_key` (`s3_bucket`, `s3_key`),
    KEY `idx_user_session` (`user_id`, `session_id`),
    KEY `idx_session_kind` (`session_id`, `file_kind`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_message_attachment`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `message_id` BIGINT   NOT NULL,
    `file_id`    BIGINT   NOT NULL,
    `sort_order` INT      NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_message_file` (`message_id`, `file_id`),
    KEY `idx_message_sort` (`message_id`, `sort_order`),
    KEY `idx_file_id` (`file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `travel_itinerary`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `session_id`        BIGINT       NOT NULL,
    `chat_id`           VARCHAR(64)  NOT NULL,
    `title`             VARCHAR(255) NOT NULL,
    `destination`       VARCHAR(255) NOT NULL,
    `start_date`        DATE                  DEFAULT NULL,
    `end_date`          DATE                  DEFAULT NULL,
    `itinerary_content` LONGTEXT     NOT NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_session_created` (`session_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
