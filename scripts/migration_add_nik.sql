-- Migration: Add NIK field and remove parent_name
-- Date: 2025-12-23
-- Purpose: Support NIK-based child registration for posyandu system

USE balita_sehat;

-- Step 1: Add nik_anak column (allow NULL temporarily for existing data)
ALTER TABLE children 
ADD COLUMN nik_anak CHAR(16) UNIQUE DEFAULT NULL 
AFTER name;

-- Step 2: Update existing children with dummy NIK (modify manually later)
-- Format: 33YYYYMMDD + 6 digit random for existing data
UPDATE children 
SET nik_anak = CONCAT('3300000000000', LPAD(id, 3, '0'))
WHERE nik_anak IS NULL;

-- Step 3: Make nik_anak NOT NULL after data populated
ALTER TABLE children 
MODIFY COLUMN nik_anak CHAR(16) NOT NULL;

-- Step 4: Remove parent_name column (not needed for posyandu workflow)
ALTER TABLE children 
DROP COLUMN parent_name;

-- Step 5: Add index for faster NIK lookup
ALTER TABLE children 
ADD INDEX idx_nik_anak (nik_anak);

-- Verify changes
DESCRIBE children;
SELECT * FROM children;
