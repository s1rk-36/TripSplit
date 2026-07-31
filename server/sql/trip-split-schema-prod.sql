drop database if exists trip_split;
create database trip_split;
use trip_split;

-- create tables and relationships
create table `role` (
	role_id int primary key auto_increment,
    `name` varchar(50) not null unique -- (admin, user)
);

create table `user` (
	user_id int primary key auto_increment,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(254) not null,
	username varchar(100) not null unique,
    password_hash varchar(2048) not null,
    disabled boolean not null default(0)
);

create table user_role (
    user_id int not null,
    role_id int not null,
    constraint pk_user_role
        primary key (user_id, role_id),
    constraint fk_user_role_user_id
        foreign key (user_id)
        references user(user_id)
        on delete cascade,
    constraint fk_user_role_role_id
        foreign key (role_id)
        references `role`(role_id)
        on delete cascade
);

create table `group` (
	group_id int primary key auto_increment,
    `name` varchar(100) not null,
    `description` text null,
    invite_code varchar(12) not null unique,
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
    created_at datetime not null,
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
	receipt_id int primary key auto_increment,
    image_url varchar(2083) not null,
    uploaded_at datetime not null,
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

create table settlement (
	settlement_id int primary key auto_increment,
    group_id int not null,
    payer_id int not null,
    payee_id int not null,
    amount decimal(10, 2) not null,
    created_at datetime not null,
	constraint fk_settlement_group_id
		foreign key (group_id)
        references `group`(group_id)
        on delete cascade,
	constraint fk_settlement_payer
		foreign key (payer_id)
        references `user`(user_id)
        on delete cascade,
	constraint fk_settlement_payee
		foreign key (payee_id)
        references `user`(user_id)
        on delete cascade
);

-- data
insert into `role`(role_id, `name`) values
(1, 'ADMIN'),
(2, 'USER');

-- No demo data: real users are created via registration (assigned ROLE_USER).
-- To grant admin, insert a user_role row mapping their user_id to role_id 1 (ADMIN).
