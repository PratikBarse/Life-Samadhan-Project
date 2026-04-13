# Hourly Rate Feature Implementation

## Overview
Added hourly rate functionality to the service provider registration process and implemented cascade delete for user-service provider relationship.

## Changes Made

### Backend Changes
1. **ServiceProvider Model** - Added `hourlyRate` column (Double type)
2. **ServiceProvider Model** - Added cascade delete foreign key constraint
3. **CreateProviderSkillDTO** - Added `hourlyRate` field
4. **ProviderSkillController** - Updated to handle hourly rate during skill addition
5. **Database Migration** - SQL scripts for hourly_rate column and cascade delete

### Frontend Changes
1. **ProviderSetup Component** - Added hourly rate input field
2. **Validation** - Added validation for hourly rate (must be > 0)
3. **API Integration** - Updated skill creation to include hourly rate

## Database Schema
```sql
-- Add hourly rate column
ALTER TABLE service_providers 
ADD COLUMN hourly_rate DECIMAL(10,2) NULL;

-- Add cascade delete constraint
ALTER TABLE service_providers 
ADD CONSTRAINT fk_service_providers_user_id 
FORIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
```

## Features
1. **Hourly Rate**: Service providers input their hourly rate during setup
2. **Cascade Delete**: When a user is deleted, their service provider record is automatically deleted
3. **Validation**: Ensures positive hourly rate is entered

## Files Modified
- `ServiceProvider.java` (hourly rate + cascade delete)
- `CreateProviderSkillDTO.java` 
- `ProviderSkillController.java`
- `ProviderSetup.jsx`
- `db_migration_add_hourly_rate.sql` (new)
- `db_add_cascade_delete.sql` (new)