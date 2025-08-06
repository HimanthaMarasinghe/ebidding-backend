-- Add sample test data to your user_service_db

-- Insert test bidders
INSERT INTO user_profile (username, email, primary_phone, secondary_phone, first_name, last_name, date_of_birth, role) 
VALUES 
('john_bidder', 'john@bidder.com', '+1234567890', '+0987654321', 'John', 'Smith', '1990-01-01', 'Bidder'),
('jane_bidder', 'jane@bidder.com', '+1234567891', '+0987654322', 'Jane', 'Doe', '1992-02-02', 'Bidder');

-- Insert bidder details (replace IDs with actual ones from above inserts)
-- You'll need to check the IDs after running the above insert
INSERT INTO bidder (id, user_image_url, nic_image_url) 
VALUES 
((SELECT id FROM user_profile WHERE username = 'john_bidder'), 'john_user_image.jpg', 'john_nic_image.jpg'),
((SELECT id FROM user_profile WHERE username = 'jane_bidder'), 'jane_user_image.jpg', 'jane_nic_image.jpg');

-- Insert test auction managers
INSERT INTO user_profile (username, email, primary_phone, secondary_phone, first_name, last_name, date_of_birth, role) 
VALUES 
('alice_auction', 'alice@auction.com', '+1111111111', '+2222222222', 'Alice', 'Manager', '1985-05-15', 'auction_manager'),
('bob_auction', 'bob@auction.com', '+1111111112', '+2222222223', 'Bob', 'Director', '1983-07-20', 'auction_manager');

-- Insert auction manager details
INSERT INTO auction_manager (id, auction_center, designation) 
VALUES 
((SELECT id FROM user_profile WHERE username = 'alice_auction'), 'Kandy', 'Senior Manager'),
((SELECT id FROM user_profile WHERE username = 'bob_auction'), 'Galle', 'Director');

-- Insert test yard managers
INSERT INTO user_profile (username, email, primary_phone, secondary_phone, first_name, last_name, date_of_birth, role) 
VALUES 
('charlie_yard', 'charlie@yard.com', '+3333333333', '+4444444444', 'Charlie', 'Supervisor', '1980-03-20', 'yard_manager'),
('david_yard', 'david@yard.com', '+3333333334', '+4444444445', 'David', 'Manager', '1978-11-10', 'yard_manager');

-- Insert yard manager details
INSERT INTO yard_manager (id, yard_name, license_number) 
VALUES 
((SELECT id FROM user_profile WHERE username = 'charlie_yard'), 'Colombo Yard', 'YM123456'),
((SELECT id FROM user_profile WHERE username = 'david_yard'), 'Kandy Yard', 'YM789012');

-- Check what we have now
SELECT 'Total Users' as info, COUNT(*) as count FROM user_profile
UNION ALL
SELECT 'Bidders', COUNT(*) FROM user_profile WHERE role = 'Bidder'
UNION ALL
SELECT 'Auction Managers', COUNT(*) FROM user_profile WHERE role = 'auction_manager'
UNION ALL
SELECT 'Yard Managers', COUNT(*) FROM user_profile WHERE role = 'yard_manager';
