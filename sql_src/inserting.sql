INSERT INTO Person (bid, name, email, username, pass)
VALUES ("B12345678","Wendy Wellesley","wwellesley@wellesley.edu","wendy","WendyPass");

INSERT INTO Student (bid, class_year, major_minor)
VALUES ("B12345678", 2014, "Computer Science");

-- create professor account
INSERT INTO Person (bid, name, email, username, pass)
VALUES ("B8765432","Scott Anderson","sanderso@wellesley.edu","scott","ScottPass");
INSERT INTO Professor (bid, department)
VALUES ("B8765432","Computer Science");

-- create waitlist 
INSERT INTO Course( crn, course_num,  department, course_limit , kind, course_name)
VALUES ("12345","CS304", "Computer Science",30,"Lecture", "Databases with Web Interfaces");
-- Insert Into Class (crn) values ("12345");
Insert Into Created_Waitlist (bid,crn) values ("B8765432","12345"); 

INSERT INTO Course( crn, course_num,  department, course_limit,kind, course_name )
VALUES ("54321","CS304", "Computer Science",30, "Lab","Datbases with Web Interface Lab");
-- Insert Into Lab(crn) values ("54321");
Insert Into Created_Waitlist (bid,crn) values ("B8765432","54321");

-- connecting lab and class
-- Insert Into Corresponding_Lab (class_crn,lab_crn) 
-- Values ("12345","54321");

-- add to waitlist 
Insert Into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation) 
Values ("12345", "B12345678", "Wendy Wellesley", "Computer Science",  2014, 1, "I need this class to complete my major"); 
Insert into On_Waitlist (bid,waitlist_id) Values ("B12345678", "12345");

-- same student, second entry on waitlist
Insert Into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation)
Values ("12345", "B12345678", "Wendy Wellesley", "Computer Science",  2014, 2, "This is my second entry; I'd like to be in the class.");

-- select lectures and lab for a course
Select * from Course where course_num like "CS304%";
-- select entries on waitlist by time
Select * from Waitlist order by submitted_on ASC;

-- waitslits that scott created
select * from Created_Waitlist where bid = "B8765432";

/* this = SELECT class_year, major_minor FROM Student WHERE bid = "B12345678") and (select name from Person where bid = "B12345678");

--Insert Into Waitlist (waitlist_id, student_bid, student_name, student_class, major_minor, rank, explanation)
--Values ("12345", "B12345678", "Wendy Wellesley", (SELECT class_year, major_minor as student_class,major_minor FROM Student WHERE bid = "B12345678") , 1, "I need this class to complete my major");


--Insert Into Waitlist (waitlist_id, student_bid, student_name, major_minor, student_class, rank, explanation)
--Values ("12345", "B12345678", "Wendy Wellesley", "Computer Science",  2014, 1, "I need this class to complete my major");

*/
