-- Make rating column nullable in review table to support replies without ratings
ALTER TABLE review MODIFY rating INT NULL;
