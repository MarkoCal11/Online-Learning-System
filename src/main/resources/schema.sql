
DROP TABLE IF EXISTS QUIZ_SUBMISSION_ANSWER;
DROP TABLE IF EXISTS QUIZ_SUBMISSION;
DROP TABLE IF EXISTS QUESTION_OPTION;
DROP TABLE IF EXISTS QUESTION;
DROP TABLE IF EXISTS QUIZ;
DROP TABLE IF EXISTS ENROLMENT;
DROP TABLE IF EXISTS LESSON;
DROP TABLE IF EXISTS COURSE;
DROP TABLE IF EXISTS PASSWORD_RESET_REQUESTS;
DROP TABLE IF EXISTS STUDENT;
DROP TABLE IF EXISTS TEACHER;

CREATE TABLE TEACHER (
                         ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                         USERNAME VARCHAR(100) NOT NULL UNIQUE,
                         ROLE VARCHAR(20) NOT NULL,
                         FIRST_NAME VARCHAR(100) NOT NULL,
                         LAST_NAME VARCHAR(100) NOT NULL,
                         EMAIL VARCHAR(200) NOT NULL,
                         DATE_ADDED DATE NOT NULL
);

CREATE TABLE STUDENT (
                         ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                         USERNAME VARCHAR(100) NOT NULL UNIQUE,
                         ROLE VARCHAR(20) NOT NULL,
                         FIRST_NAME VARCHAR(100) NOT NULL,
                         LAST_NAME VARCHAR(100) NOT NULL,
                         EMAIL VARCHAR(200) NOT NULL,
                         DATE_ADDED DATE NOT NULL,
                         JMBAG VARCHAR(50) NOT NULL
);

CREATE TABLE COURSE (
                        ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                        TITLE VARCHAR(200) NOT NULL UNIQUE,
                        DESCRIPTION CLOB,
                        ECTS INT NOT NULL,
                        TEACHER_ID BIGINT,
                        CONSTRAINT FK_COURSE_TEACHER FOREIGN KEY (TEACHER_ID) REFERENCES TEACHER(ID)
);

CREATE TABLE LESSON (
                        ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                        TITLE VARCHAR(200) NOT NULL,
                        CONTENT CLOB,
                        COURSE_ID BIGINT NOT NULL,
                        CONSTRAINT FK_LESSON_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSE(ID)
);

CREATE TABLE ENROLMENT (
                           ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                           STUDENT_ID BIGINT NOT NULL,
                           COURSE_ID BIGINT NOT NULL,
                           ENROLMENT_DATE DATE NOT NULL,
                           STATUS VARCHAR(20) NOT NULL,
                           FINAL_GRADE DOUBLE,
                           CONSTRAINT FK_ENROLMENT_STUDENT FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT(ID),
                           CONSTRAINT FK_ENROLMENT_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSE(ID)
);

CREATE TABLE QUIZ (
                      ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                      TITLE VARCHAR(200) NOT NULL,
                      DATE DATE,
                      COURSE_ID BIGINT NOT NULL,
                      READY_TO_TAKE BOOLEAN NOT NULL DEFAULT FALSE,
                      CONSTRAINT FK_QUIZ_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSE(ID)
);

CREATE TABLE QUESTION (
                          ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                          TEXT CLOB NOT NULL,
                          QUIZ_ID BIGINT NOT NULL,
                          CORRECT_ANSWER_INDEX INT,
                          CONSTRAINT FK_QUESTION_QUIZ FOREIGN KEY (QUIZ_ID) REFERENCES QUIZ(ID)
);

CREATE TABLE QUESTION_OPTION (
                                 ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 QUESTION_ID BIGINT NOT NULL,
                                 OPTION_INDEX INT NOT NULL,
                                 TEXT CLOB NOT NULL,
                                 CONSTRAINT FK_QOPT_QUESTION FOREIGN KEY (QUESTION_ID) REFERENCES QUESTION(ID)
);

CREATE TABLE QUIZ_SUBMISSION (
                                 ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 QUIZ_ID BIGINT NOT NULL,
                                 ENROLMENT_ID BIGINT NOT NULL,
                                 SUBMITTED_AT TIMESTAMP,
                                 GRADE INT,
                                 PERCENTAGE DOUBLE,
                                 CONSTRAINT FK_QS_QUIZ FOREIGN KEY (QUIZ_ID) REFERENCES QUIZ(ID),
                                 CONSTRAINT FK_QS_ENROLMENT FOREIGN KEY (ENROLMENT_ID) REFERENCES ENROLMENT(ID)
);

CREATE TABLE QUIZ_SUBMISSION_ANSWER (
                                        ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        QUIZ_SUBMISSION_ID BIGINT NOT NULL,
                                        QUESTION_ID BIGINT NOT NULL,
                                        SELECTED_OPTION_INDEX INT,
                                        CONSTRAINT FK_QSA_SUBMISSION FOREIGN KEY (QUIZ_SUBMISSION_ID) REFERENCES QUIZ_SUBMISSION(ID),
                                        CONSTRAINT FK_QSA_QUESTION FOREIGN KEY (QUESTION_ID) REFERENCES QUESTION(ID)
);

CREATE TABLE PASSWORD_RESET_REQUESTS (
                                         ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         STUDENT_USERNAME VARCHAR(100),
                                         TEACHER_USERNAME VARCHAR(100),
                                         STATUS VARCHAR(20) NOT NULL
);
