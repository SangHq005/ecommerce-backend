-- V0111: Robust migration for review replies and likes
-- Uses a procedure to safely add columns if they don't already exist

DELIMITER //

CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN colName VARCHAR(64),
    IN colDef VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = tableName
        AND column_name = colName
    ) THEN
        SET @sqlstmt = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
        PREPARE stmt FROM @sqlstmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE AddIndexIfNotExists(
    IN tableName VARCHAR(64),
    IN indexName VARCHAR(64),
    IN indexDef VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = tableName
        AND index_name = indexName
    ) THEN
        SET @sqlstmt = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, indexDef);
        PREPARE stmt FROM @sqlstmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- Safely add columns
CALL AddColumnIfNotExists('review', 'parent_id', 'BIGINT NULL');
CALL AddColumnIfNotExists('review', 'helpful_count', 'INT NOT NULL DEFAULT 0');

-- Drop procedures
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;

-- Create review_likes table safely
CREATE TABLE IF NOT EXISTS review_likes (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES review(id) ON DELETE CASCADE
);

-- Safely add index
-- We can't use CALL here because we dropped it, but CREATE INDEX doesn't have IF NOT EXISTS in all MySQL versions
-- However, we can use a separate script or just try-catch if possible. 
-- Actually, let's just use the index creation outside or keep the procedure longer.

-- Redefine AddIndexIfNotExists for the next call
DELIMITER //
CREATE PROCEDURE AddIndexIfNotExists(
    IN tableName VARCHAR(64),
    IN indexName VARCHAR(64),
    IN indexDef VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = tableName
        AND index_name = indexName
    ) THEN
        SET @sqlstmt = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, indexDef);
        PREPARE stmt FROM @sqlstmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddIndexIfNotExists('review', 'idx_review_parent', '(parent_id)');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
