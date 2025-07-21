drop database if exists trip_split;
create database trip_split;
use trip_split;

create table `role` (
	role_id int primary key not null,
    `name` varchar(20) not null # (admin, user)  
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
	expense_id int primary key not null,
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