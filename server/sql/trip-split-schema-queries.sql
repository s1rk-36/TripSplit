use trip_split_test;
-- set sql_safe_updates = 0;
-- call set_known_good_state();

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

select ug.user_id, ug.group_id, ug.is_admin,
	   u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id,
       r.`name` as role_name,
       g.group_id, g.`name` as group_name, g.`description` as group_description
from user_group as ug
inner join user as u on ug.user_id = u.user_id
inner join role as r on u.role_id = r.role_id
inner join `group` as g on ug.group_id = g.group_id
where ug.group_id = 1;

SELECT
  -- From user_group
  ug.user_id,
  ug.group_id,
  ug.is_admin,

  -- Full columns for the user in the group (u)
  u.user_id AS u_user_id,
  u.first_name AS u_first_name,
  u.last_name AS u_last_name,
  u.email AS u_email,
  u.username AS u_username,
  u.password_hash AS u_password_hash,
  u.role_id AS u_role_id,

  -- Group columns
  g.group_id AS g_group_id,
  g.name AS group_name,
  g.description AS group_description,

  -- Full columns for the user who created the group (cu)
  cu.user_id AS cu_user_id,
  cu.first_name AS cu_first_name,
  cu.last_name AS cu_last_name,
  cu.email AS cu_email,
  cu.username AS cu_username,
  cu.password_hash AS cu_password_hash,
  cu.role_id AS cu_role_id

FROM user_group ug
JOIN user u ON ug.user_id = u.user_id
JOIN `group` g ON ug.group_id = g.group_id
JOIN user cu ON g.created_by = cu.user_id;
       