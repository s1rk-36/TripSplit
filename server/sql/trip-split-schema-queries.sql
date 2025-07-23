use trip_split_test;
-- set sql_safe_updates = 0;
call set_known_good_state();
SELECT @@global.time_zone, @@session.time_zone;
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

-- This is just for testing purposes
select * from `role`;
select * from `user`;
select * from user_role;
select * from `group`;
select * from user_group;
select * from expense;
select * from user_expense;
select * from receipt;
select * from `comment`;

-- Group CRUD
-- findAll()
select g.group_id, g.`name` as group_name, g.`description` as group_description, 
	   u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id
from `group` as g
inner join user as u on g.created_by = u.user_id
limit 1000;

-- findById()
select g.group_id, g.`name` as group_name, g.`description` as group_description, 
	   u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id
from `group` as g
inner join user as u on g.created_by = u.user_id
where g.group_id = 1;

-- add()
insert into `group` (`name`, `description`, created_by) values
('Mexico Food Tour', 'Exploring street food and markets from Mexico City to Oaxaca.', 2);

-- update()
update `group` set
`name` = "1",
`description` = "2",
created_by = 1
where group_id = 1;

-- deleteById()
delete from `group` where group_id = 1;

-- nameExists()
select count(*) 
from `group`
where lower(`name`) = lower('Japan Spring Trip') and not group_id = 5;

-- addUsers()
select
  ug.user_id, 
  ug.group_id, 
  ug.is_admin, 

  -- Full columns for the user in the group (u)
  u.user_id as u_user_id, 
  u.first_name as u_first_name, 
  u.last_name as u_last_name, 
  u.email as u_email, 
  u.username as u_username, 
  u.password_hash as u_password_hash, 
  u.disabled as u_disabled, 

  -- Group columns
  g.group_id, 
  g.`name` as group_name, 
  g.`description` as group_description, 

  -- Full columns for the user who created the group (gcb)
  gcb.user_id as gcb_user_id, 
  gcb.first_name as gcb_first_name, 
  gcb.last_name as gcb_last_name, 
  gcb.email as gcb_email, 
  gcb.username as gcb_username, 
  gcb.password_hash as gcb_password_hash, 
  gcb.disabled as gcb_disabled 

from user_group ug
inner join `user` u on ug.user_id = u.user_id
inner join `group` g on ug.group_id = g.group_id
inner join `user` gcb on g.created_by = gcb.user_id
where ug.group_id = 1;





-- Expense CRUD
-- findAll()
select
	e.expense_id, 
	e.`name` as expense_name, 
	e.total_cost, 
	e.category, 
	e.`description` as expense_description, 
	e.created_at, 

	g.group_id, 
	g.`name` as group_name, 
	g.`description` as group_description, 

	gcb.user_id as gcb_user_id, 
	gcb.first_name as gcb_first_name, 
	gcb.last_name as gcb_last_name, 
	gcb.email as gcb_email, 
	gcb.username as gcb_username, 
	gcb.password_hash as gcb_password_hash, 
	gcb.disabled as gcb_disabled, 

	ecb.user_id as ecb_user_id, 
    ecb.first_name as ecb_first_name, 
    ecb.last_name as ecb_last_name, 
	ecb.email as ecb_email, 
    ecb.username as ecb_username, 
    ecb.password_hash as ecb_password_hash, 
    ecb.disabled as ecb_disabled 

from expense e 
inner join `group` as g on e.group_id = g.group_id 
inner join `user` as gcb on g.created_by = gcb.user_id 
inner join `user` as ecb on e.created_by = ecb.user_id 
limit 1000;

-- findById()
select
	e.expense_id, 
	e.`name` as expense_name, 
	e.total_cost, 
	e.category, 
	e.`description` as expense_description, 
	e.created_at, 

	g.group_id, 
	g.`name` as group_name, 
	g.`description` as group_description, 

	gcb.user_id as gcb_user_id, 
	gcb.first_name as gcb_first_name, 
	gcb.last_name as gcb_last_name, 
	gcb.email as gcb_email, 
	gcb.username as gcb_username, 
	gcb.password_hash as gcb_password_hash, 
	gcb.disabled as gcb_disabled, 

	ecb.user_id as ecb_user_id, 
    ecb.first_name as ecb_first_name, 
    ecb.last_name as ecb_last_name, 
	ecb.email as ecb_email, 
    ecb.username as ecb_username, 
    ecb.password_hash as ecb_password_hash, 
    ecb.disabled as ecb_disabled 

from expense e 
inner join `group` as g on e.group_id = g.group_id 
inner join `user` as gcb on g.created_by = gcb.user_id 
inner join `user` as ecb on e.created_by = ecb.user_id  
where e.expense_id = 1; 

-- add()
insert into expense (`name`, total_cost, category, `description`, created_at, group_id, created_by) values
('Hotel Stay', 850.00, 'LODGING', '3-night stay at Grand Hotel', '2025-03-11', 1, 2);

-- update()
update expense set
`name` = "1",
total_cost = 1,
category = "LODGING",
`description` = "1"
where expense_id = 1;


-- UserExpense get users
select
	ue.user_id as ue_user_id, 
    ue.expense_id as ue_expense_id, 
    ue.amount_owned as ue_amount_owned, 
    ue.amount_paid as ue_amount_paid, 
    
    -- Full columns for the user in the expense (u)
	u.user_id as u_user_id, 
	u.first_name as u_first_name, 
	u.last_name as u_last_name, 
	u.email as u_email, 
	u.username as u_username, 
	u.password_hash as u_password_hash,
    u.disabled as u_disabled, 
    
    -- Expense columns
    e.expense_id, 
    e.`name` as expense_name, 
    e.total_cost, 
    e.category, 
    e.`description` as expense_description, 
    e.created_at, 
	
    -- Full columns for the group in the expense (eg)
    eg.group_id as eg_group_id, 
    eg.`name` as eg_group_name, 
    eg.`description` as eg_group_description, 
    
    -- Full columns for the user that created the group in the expense (egcb)
    egcb.user_id as egcb_user_id, 
    egcb.first_name as egcb_first_name, 
	egcb.last_name as egcb_last_name, 
	egcb.email as egcb_email, 
	egcb.username as egcb_username, 
	egcb.password_hash as egcb_password_hash, 
    egcb.disabled as egcb_disabled, 
    
    -- Full columns for the user that created the expense (ecb)
    ecb.user_id as ecb_user_id, 
    ecb.first_name as ecb_first_name, 
	ecb.last_name as ecb_last_name, 
	ecb.email as ecb_email, 
	ecb.username as ecb_username, 
	ecb.password_hash as ecb_password_hash,
    ecb.disabled as ecb_disabled 
    
from user_expense as ue 
inner join `user` as u on ue.user_id = u.user_id 
inner join expense as e on ue.expense_id = e.expense_id 
inner join `group` as eg on e.group_id = eg.group_id 
inner join `user` as egcb on eg.created_by = egcb.user_id 
inner join `user` as ecb on e.created_by = ecb.user_id 
where ue.expense_id = 1;
       