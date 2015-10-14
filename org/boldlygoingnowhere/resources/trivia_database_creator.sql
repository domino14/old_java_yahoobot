drop database Trivia;
create database Trivia;
use Trivia;
create table questions (
	question text,
	answer text);

load data local infile '~/yahoobot/trivia.txt' into table questions FIELDS terminated by '*' LINES terminated by '\n';

delete from questions where length(question) > 255;
delete from questions where length(answer) > 50;

ALTER TABLE questions ADD id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (id);