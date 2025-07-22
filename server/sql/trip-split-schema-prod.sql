drop database if exists trip_split;
create database trip_split;
use trip_split;

-- create tables and relationships
create table `role` (
	role_id int primary key not null,
    `name` varchar(20) not null -- (admin, user)  
);

create table `user` (
	user_id int primary key auto_increment,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(254) not null,
	username varchar(100) not null,
    password_hash varchar(128) not null,
    role_id int not null,
	constraint fk_user_role_id
		foreign key (role_id)
		references `role`(role_id)
);

create table `group` (
	group_id int primary key auto_increment,
    `name` varchar(100) not null,
    `description` text null,
    created_by int not null,
    constraint fk_group_created_by
		foreign key (created_by)
		references `user`(user_id)
        on delete cascade
);

create table user_group (
	user_id int not null,
    group_id int not null,
    is_admin boolean not null default false,
	constraint pk_user_group
        primary key (user_id, group_id),
    constraint fk_user_group_user_id
        foreign key (user_id)
        references `user`(user_id)
        on delete cascade,
    constraint fk_user_group_group_id
        foreign key (group_id)
        references `group`(group_id)
        on delete cascade
);

create table expense (
	expense_id int primary key auto_increment,
    `name` varchar(100) not null,
    total_cost decimal(10, 2) not null,
    category varchar(50) not null,
    `description` text null,
    created_at date not null,
    group_id int not null,
    created_by int not null,
    constraint fk_expense_group_id
		foreign key (group_id)
        references `group`(group_id)
        on delete cascade,
	constraint fk_expense_created_by
		foreign key (created_by)
        references `user`(user_id)
        on delete cascade
);

create table user_expense (
	user_id int not null,
    expense_id int not null,
    amount_owned decimal(10, 2) not null,
    amount_paid decimal(10, 2) not null,
	constraint pk_user_expense 
		primary key (user_id, expense_id),
    constraint fk_user_expense_user 
		foreign key (user_id) 
        references `user`(user_id)
        on delete cascade,
    constraint fk_user_expense_expense 
		foreign key (expense_id) 
        references expense(expense_id)
        on delete cascade
);

create table receipt (
	receipt_id int primary key not null,
    image_url varchar(2083) not null,
    uploaded_at date not null,
    expense_id int not null,
    constraint fk_receipt_expense_id
		foreign key (expense_id)
		references expense(expense_id)
        on delete cascade
);

create table `comment` (
	comment_id int primary key not null,
    `timestamp` timestamp not null,
    content text not null,
    expense_id int not null,
    created_by int not null,
	constraint fk_comment_expense_id
		foreign key (expense_id)
		references expense(expense_id)
        on delete cascade,
	constraint fk_comment_created_by
		foreign key (created_by)
		references `user`(user_id)
        on delete cascade
);

-- data
insert into `role`(role_id, `name`) values
	(1, 'Admin'),
	(2, 'User');

insert into `user` (first_name, last_name, email, username, password_hash, role_id) values
('Alice', 'Johnson', 'alice.johnson@example.com', 'alicej', 'hash_1_example', 1),
('Bob', 'Smith', 'bob.smith@example.com', 'bobsmith', 'hash_2_example', 2),
('Carol', 'Davis', 'carol.davis@example.com', 'carold', 'hash_3_example', 2),
('David', 'Lee', 'david.lee@example.com', 'davidl', 'hash_4_example', 2),
('Eve', 'Martinez', 'eve.martinez@example.com', 'evem', 'hash_5_example', 2);

insert into `group` (`name`, `description`, created_by) values
('Japan Spring Trip', 'A cherry blossom tour across Tokyo and Kyoto.', 1),
('NYC Business Conference', 'Travel group for attending a tech conference in NYC.', 2),
('Iceland Road Adventure', 'Self-drive ring road trip around Iceland.', 3),
('Vegas Bachelor Party', 'Weekend celebration with the crew.', 4),
('Thailand Escape', 'Group trip to explore Bangkok and the islands.', 5);

insert into user_group (user_id, group_id, is_admin) values
(1, 1, true),   -- Alice (admin) in her Japan trip
(2, 1, false),
(3, 1, false),

(2, 2, true),   -- Bob (admin) on his business trip
(4, 2, false),
(5, 2, false),

(3, 3, true),   -- Carol (admin) on Iceland adventure
(1, 3, false),

(4, 4, true),   -- David (admin) in Vegas trip
(2, 4, false),
(5, 4, false),

(5, 5, true),   -- Eve (admin) in Thailand trip
(3, 5, false),
(4, 5, false);

insert into expense (expense_id, `name`, total_cost, category, `description`, created_at, group_id, created_by) values
(1, 'Flight Tickets', 1200.00, 'Travel', 'Round trip flights to Tokyo', '2025-03-10', 1, 1),
(2, 'Hotel Accommodation', 800.50, 'Lodging', '5 nights stay at Tokyo hotel', '2025-03-11', 1, 2),
(3, 'Conference Fee', 350.00, 'Registration', 'Tech conference registration', '2025-04-01', 2, 2),
(4, 'Taxi Fare', 45.75, 'Transport', 'Taxi from airport to hotel', '2025-03-10', 1, 3),
(5, 'Dinner at Local Eatery', 120.20, 'Food', 'Group dinner in downtown Tokyo', '2025-03-12', 1, 1),
(6, 'Car Rental', 300.00, 'Transport', 'Rental car for Iceland road trip', '2025-06-15', 3, 3),
(7, 'Camping Equipment', 150.00, 'Supplies', 'Camping gear rental', '2025-06-16', 3, 1),
(8, 'Vegas Show Tickets', 200.00, 'Entertainment', 'Tickets for Vegas show', '2025-07-05', 4, 4),
(9, 'Flight to Bangkok', 950.00, 'Travel', 'Flight tickets for Thailand trip', '2025-08-20', 5, 5),
(10,'Island Tour', 180.00, 'Activity', 'Boat tour around islands', '2025-08-22', 5, 5);

insert into user_expense (user_id, expense_id, amount_owned, amount_paid) values
-- Expense 1: Flight Tickets ($1200) shared by users 1, 2, 3
(1, 1, 400.00, 1200.00),  -- Alice owes $400, paid full $1200
(2, 1, 400.00, 0.00),     -- Bob owes $400, paid nothing yet
(3, 1, 400.00, 0.00),     -- Carol owes $400, paid nothing yet

-- Expense 2: Hotel ($800.50) shared by users 1, 2
(1, 2, 400.25, 0.00),     -- Alice owes half
(2, 2, 400.25, 800.50),   -- Bob paid full amount

-- Expense 3: Conference Fee ($350) by users 2, 4, 5
(2, 3, 116.67, 350.00),   -- Bob paid full
(4, 3, 116.67, 0.00),
(5, 3, 116.66, 0.00),

-- Expense 4: Taxi Fare ($45.75) by users 1, 3
(1, 4, 22.88, 0.00),
(3, 4, 22.87, 45.75),

-- Expense 5: Dinner ($120.20) by users 1, 2, 3
(1, 5, 40.07, 120.20),    -- Alice paid full
(2, 5, 40.07, 0.00),
(3, 5, 40.06, 0.00),

-- Expense 6: Car Rental ($300) by users 3, 1
(3, 6, 150.00, 300.00),   -- Carol paid full
(1, 6, 150.00, 0.00),

-- Expense 7: Camping Gear ($150) by users 3, 1
(3, 7, 75.00, 0.00),
(1, 7, 75.00, 150.00),    -- Alice paid full

-- Expense 8: Vegas Show Tickets ($200) by users 4, 2, 5
(4, 8, 66.67, 200.00),   -- David paid full
(2, 8, 66.66, 0.00),
(5, 8, 66.67, 0.00),

-- Expense 9: Flight to Bangkok ($950) by users 5, 3, 4
(5, 9, 316.67, 950.00),  -- Eve paid full
(3, 9, 316.67, 0.00),
(4, 9, 316.66, 0.00),

-- Expense 10: Island Tour ($180) by users 5 only
(5, 10, 180.00, 180.00); -- Eve paid full

insert into receipt (receipt_id, image_url, uploaded_at, expense_id) values
(1, 'https://example.com/receipts/flight-ticket.jpg', '2025-07-01', 1),
(2, 'https://example.com/receipts/hotel-booking.png', '2025-07-02', 2),
(3, 'https://example.com/receipts/conference-fee.pdf', '2025-07-03', 3),
(4, 'https://example.com/receipts/taxi-fare.jpeg', '2025-07-03', 4),
(5, 'https://example.com/receipts/dinner-bill.jpg', '2025-07-04', 5);

insert into `comment` (comment_id, `timestamp`, content, expense_id, created_by) values
(1, '2025-07-01 10:23:45', 'I’ve uploaded the flight ticket receipt. Please confirm.', 1, 1),
(2, '2025-07-01 11:00:12', 'Looks good to me!', 1, 2),
(3, '2025-07-02 09:15:30', 'Did we split the hotel evenly?', 2, 3),
(4, '2025-07-02 10:45:00', 'Yes, it was $400.25 each.', 2, 2),
(5, '2025-07-03 14:02:10', 'Conference fee is reimbursable, right?', 3, 4),
(6, '2025-07-03 15:11:05', 'Yes, I’ll submit it later today.', 3, 2),
(7, '2025-07-04 08:00:00', 'Why is the taxi fare split like this?', 4, 3),
(8, '2025-07-04 08:12:34', 'Because I had to take a detour.', 4, 1),
(9, '2025-07-05 19:23:17', 'Dinner receipt is uploaded.', 5, 1),
(10, '2025-07-06 12:44:00', 'Thanks for covering the car rental!', 6, 4),
(11, '2025-07-07 17:18:09', 'Next time, let’s rent camping gear together.', 7, 2),
(12, '2025-07-08 10:00:00', 'Show tickets were amazing!', 8, 5),
(13, '2025-07-08 10:15:42', 'Totally worth it!', 8, 4),
(15, '2025-07-10 13:00:00', 'Let’s settle this one soon.', 10, 3);
