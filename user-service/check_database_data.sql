-- Check what data exists in your user_service_db

-- 1. Check all users in user_profile table
SELECT 'user_profile' as table_name, count(*) as record_count FROM user_profile
UNION ALL
SELECT 'bidder' as table_name, count(*) as record_count FROM bidder
UNION ALL
SELECT 'auction_manager' as table_name, count(*) as record_count FROM auction_manager
UNION ALL
SELECT 'yard_manager' as table_name, count(*) as record_count FROM yard_manager;

-- 2. Show all users with their details
SELECT id, username, email, first_name, last_name, role, primary_phone 
FROM user_profile 
ORDER BY id;

-- 3. Show bidders with their additional info
SELECT up.id, up.username, up.first_name, up.last_name, up.role, 
       b.user_image_url, b.nic_image_url
FROM user_profile up
JOIN bidder b ON up.id = b.id;

-- 4. Show auction managers
SELECT up.id, up.username, up.first_name, up.last_name, up.role,
       am.auction_center, am.designation
FROM user_profile up
JOIN auction_manager am ON up.id = am.id;

-- 5. Show yard managers
SELECT up.id, up.username, up.first_name, up.last_name, up.role,
       ym.yard_name, ym.license_number
FROM user_profile up
JOIN yard_manager ym ON up.id = ym.id;
