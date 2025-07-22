use trip_split_test;

select * from `group`;
select * from `user`;

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