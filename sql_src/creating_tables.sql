use walter_db;


drop table if exists Professor;
drop table if exists Student;
drop table if exists Created_Waitlist; 
drop table if exists On_Waitlist;
drop table if exists Person;
drop table if exists Course;
drop table if exists Waitlist;
-- drop table if exists Corresponding_Lab; 

-- define the user table
create table Person(
	bid char(9) not null primary key,
	name varchar(30) not null,
	email varchar(30) not null,
	usertype enum('p','s') not null,
	unique (email), 
	pass varchar(20) not null
	)
	ENGINE = InnoDB;
	
create table Student( 
	bid char(9) not null primary key, 
	foreign key (bid) 
		references Person (bid)  
		on delete cascade on update cascade,
	class_year int(4) unsigned not null,
	major_minor varchar(30) not null
	)
	ENGINE = InnoDB;

-- define the professor table
create table Professor( 
	bid char(9) primary key,
	foreign key (bid)
		references Person (bid)
		on delete cascade on update cascade,
	department varchar(25) not null
	)
	ENGINE = InnoDB;

-- define the course table
create table Course(
	crn int(5) not null primary key,
	course_name varchar(45) not null,
	course_num  varchar(10) not null,
	department varchar(20) not null,
	course_limit int unsigned not null,
	kind varchar(10) not null  
	)
	ENGINE = InnoDB;

/**
create table Class(
	crn int(5) primary key,
	foreign key (crn)
		references Course (crn)
		on delete cascade on update cascade
	)
	ENGINE = InnoDB;

-- define the lab table
create table Lab(
	crn int(5) primary key,
	foreign key (crn)
		references Course (crn)
		on delete cascade on update cascade
	)
	ENGINE = InnoDB;

-- define the course-to-lab relationship (one to many)  	
create table Corresponding_Lab( 
	class_crn int not null references class(crn),
	lab_crn int not null references lab(crn),
	primary key (class_crn, lab_crn)
	)
	ENGINE = InnoDB;
**/

-- define the waitlists table
create table Waitlist(
	waitlist_id int unsigned not null references Course(crn),
	student_bid char(9) not null 
		references Student (bid)
                on delete cascade on update cascade,
	student_name varchar(20) not null
		references Student (name)
		on delete cascade on update cascade,
	major_minor varchar(20) not null 
		references Student (major_minor)
		on delete cascade on update cascade,
	student_class int(4) not null 
		references Student (class_year)
		on delete cascade on update cascade,
	submitted_on timestamp default current_timestamp not null,
	rank int unsigned, 
	explanation varchar(250) default ' '
	)
	ENGINE = InnoDB;
/**
-- define the “student has waitlists” table
create table On_Waitlist(
	bid char(9) not null references Student (bid)
		on delete cascade on update cascade, 
	waitlist_id int not null references Waitlist(waitlist_id) 
		on delete cascade on update cascade,
	primary key (bid, waitlist_id) 
	)
	ENGINE = InnoDB;
*/
-- define the “professor manages course” table
create table Created_Waitlist(
	bid char(9),
	crn int(5),
	primary key (bid, crn),
	foreign key (bid)
		references Person (bid)
		on delete cascade on update cascade,
	foreign key (crn)
		references Course (crn)
		on delete cascade on update cascade
	)
	ENGINE = InnoDB;
	
