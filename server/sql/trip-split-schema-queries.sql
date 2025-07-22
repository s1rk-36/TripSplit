use trip_split_test;
-- set sql_safe_updates = 0;
call set_known_good_state();

-- This is just for testing purposes
select * from `user`;
select * from `group`;
select * from expense;
select * from receipt;
select * from `comment`;
select * from user_group;
select * from user_expense;

-- delete from `user` where user_id = 1;

select g.group_id, g.`name` as group_name, g.`description` as group_description, 
	   u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id
from `group` as g
inner join user as u on g.created_by = u.user_id
limit 1000;

select g.group_id, g.`name` as group_name, g.`description` as group_description, 
	   u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id
from `group` as g
inner join user as u on g.created_by = u.user_id
where g.group_id = 1;

insert into `group` (`name`, `description`, created_by) values
('Mexico Food Tour', 'Exploring street food and markets from Mexico City to Oaxaca.', 2);

update `group` set
`name` = "1",
`description` = "2",
created_by = 1
where group_id = 1;

delete from `group` where group_id = 1;

select count(*) 
from `group`
where lower(`name`) = lower('Japan Spring Trip') and not group_id = 5;

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
  u.role_id as u_role_id,

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
  gcb.role_id as gcb_role_id

from user_group ug
inner join `user` u on ug.user_id = u.user_id
inner join `group` g on ug.group_id = g.group_id
inner join `user` gcb on g.created_by = gcb.user_id
where ug.group_id = 1;
       