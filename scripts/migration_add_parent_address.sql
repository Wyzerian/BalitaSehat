-- Migration: Add parent_name and address fields
-- Date: 2025-12-26
-- Purpose: Add parent/guardian info and address for posyandu records

USE balita_sehat;

-- Add parent_name column
ALTER TABLE children 
ADD COLUMN parent_name VARCHAR(100) DEFAULT NULL
AFTER nik_anak;

-- Add address column
ALTER TABLE children 
ADD COLUMN address TEXT DEFAULT NULL
AFTER parent_name;

-- Verify changes
DESCRIBE children;

-- Show sample data
SELECT id, name, nik_anak, parent_name, address, gender, birth_date 
FROM children 
LIMIT 5;
