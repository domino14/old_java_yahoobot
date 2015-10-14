alter table questions drop column id;
alter table questions add id int unsigned not null auto_increment, add primary key(id);