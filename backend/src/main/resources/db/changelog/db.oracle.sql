-- liquibase formatted sql

-- changeset anika:1
CREATE TABLE SCHOOL
(
    ID          NUMBER(38) PRIMARY KEY NOT NULL,
    SCHOOL_NAME VARCHAR2(255) not null,
    CITY        VARCHAR2(255) not null,
    ADDRESS     VARCHAR2(255) not null
);
CREATE TABLE SCHOOL_PERIOD
(
    ID         NUMBER(38) PRIMARY KEY NOT NULL,
    START_YEAR NUMBER(10) not null,
    END_YEAR   NUMBER(10) not null,
    SEMESTER   NUMBER(1) not null
);
CREATE TABLE "USER"
(
    ID              NUMBER(38) PRIMARY KEY NOT NULL,
    PERSONAL_NUMBER VARCHAR2(50) not null,
    EMAIL           VARCHAR(60)      not null,
    PHONE_NUMBER    VARCHAR(60),
    FIRST_NAME      VARCHAR2(60),
    MIDDLE_NAME     VARCHAR2(60),
    LAST_NAME       VARCHAR2(60),
    USERNAME        VARCHAR2(60),
    ADDRESS         VARCHAR(255),
    ROLE            VARCHAR2(60) not null,
    DELETED         CHAR default 'N' not null,
    DETAILS         VARCHAR2(2000),
    SCHOOL_ID       NUMBER(38) not null
        constraint FK_USER_SCHOOL
        references SCHOOL
);
CREATE TABLE SUBJECT
(
    ID               NUMBER(38) PRIMARY KEY NOT NULL,
    NAME             VARCHAR(255) not null,
    TEACHER_ID       NUMBER(38) not null
        constraint FK_SUBJECT_TEACHER
        references "USER",
    SCHOOL_ID        NUMBER(38) not null
        constraint FK_SUBJECT_SCHOOL
        references SCHOOL,
    SCHOOL_PERIOD_ID NUMBER(38) not null
        constraint FK_SUBJECT_SCHOOL_PERIOD
        references SCHOOL_PERIOD

);
CREATE TABLE SCHOOL_CLASS
(
    ID               NUMBER(38) PRIMARY KEY NOT NULL,
    NAME             VARCHAR(3) NOT NULL,
    MAIN_TEACHER     NUMBER(38) not null
        constraint FK_SCHOOL_CLASS_TEACHER
        references "USER",
    SCHOOL_ID        NUMBER(38) not null
        constraint FK_SCHOOL_CLASS_SCHOOL
        references SCHOOL,
    SCHOOL_PERIOD_ID NUMBER(38) not null
        constraint FK_SCHOOL_CLASS_SCHOOL_PERIOD
        references SCHOOL_PERIOD
);
CREATE TABLE SCHOOL_LESSON
(
    ID                   NUMBER(38) PRIMARY KEY NOT NULL,
    START_TIME_OF_LESSON DATE             not null,
    END_TIME_OF_LESSON   DATE             not null,
    SUBJECT_ID           NUMBER(38) not null
        constraint FK_LESSON_SUBJECT
        references SUBJECT,
    SCHOOL_CLASS_ID      NUMBER(38) not null
        constraint FK_LESSON_SCHOOL_CLASS
        references SCHOOL_CLASS,
    LESSON_TOPIC         VARCHAR2(255),
    ROOM                 VARCHAR(50)      not null,
    TAKEN                CHAR default 'N' not null,
    SCHOOL_ID            NUMBER(38) not null
        constraint FK_SCHOOL_LESSON_SCHOOL
        references SCHOOL,
    SCHOOL_PERIOD_ID     NUMBER(38) not null
        constraint FK_SCHOOL_LESSON_SCHOOL_PERIOD
        references SCHOOL_PERIOD

);
CREATE TABLE EVALUATION
(
    ID               NUMBER(38) PRIMARY KEY NOT NULL,
    STUDENT_ID       NUMBER(38) not null
        constraint FK_EVALUATION_STUDENT
        references "USER",
    SUBJECT_ID       NUMBER(38) not null
        constraint FK_EVALUATION_SUBJECT
        references SUBJECT,
    SCHOOL_LESSON_ID NUMBER(38) not null
        constraint FK_EVALUATION_SCHOOL_LESSON
        references SUBJECT,
    EVALUATION_DATE  DATE,
    EVALUATION_TYPE  VARCHAR(50),
    EVALUATION_VALUE VARCHAR2(255),
    SCHOOL_ID        NUMBER(38) not null
        constraint FK_EVALUATION_SCHOOL
        references SCHOOL,
    SCHOOL_PERIOD_ID NUMBER(38) not null
        constraint FK_EVALUATION_SCHOOL_PERIOD
        references SCHOOL_PERIOD
);
create sequence SCHOOL_SEQ increment by 1 nocache;
create sequence SCHOOL_PERIOD_SEQ increment by 1 nocache;
create sequence USER_SEQ increment by 1 nocache;
create sequence SUBJECT_SEQ increment by 1 nocache;
create sequence SCHOOL_CLASS_SEQ increment by 1 nocache;
create sequence SCHOOL_LESSON_SEQ increment by 1 nocache;
create sequence EVALUATION_SEQ increment by 1 nocache;
-- changeset anika:2
CREATE TABLE SCHOOL_CLASS_SUBJECT
(
    SCHOOL_CLASS_ID NUMBER(38) REFERENCES SCHOOL_CLASS(ID),
    SUBJECT_ID      NUMBER(38) REFERENCES SUBJECT(ID),
    PRIMARY KEY (SCHOOL_CLASS_ID, SUBJECT_ID)
);
-- changeset anika:3
CREATE TABLE PARENT_STUDENT
(
    STUDENT_ID NUMBER(38) REFERENCES "USER"(ID),
    PARENT_ID  NUMBER(38) REFERENCES "USER"(ID),
    PRIMARY KEY (STUDENT_ID, PARENT_ID)
);
CREATE TABLE STUDENT_SCHOOL_CLASS
(
    STUDENT_ID      NUMBER(38) REFERENCES "USER"(ID),
    SCHOOL_CLASS_ID NUMBER(38) REFERENCES SCHOOL_CLASS(ID),
    PERIOD_ID       NUMBER(38) REFERENCES SCHOOL_PERIOD(ID),
    NUMBER_IN_CLASS NUMBER(10),
    CONSTRAINT CK_SCHOOL_CLASS_PERIOD_NUMBER
        UNIQUE (SCHOOL_CLASS_ID, PERIOD_ID, NUMBER_IN_CLASS),
    PRIMARY KEY (STUDENT_ID, SCHOOL_CLASS_ID)
);
-- changeset anika:4
ALTER TABLE "USER"
    ADD PASSWORD VARCHAR2(50);
--changeset anika:5
alter table "USER"
    modify PERSONAL_NUMBER null;
--changeset anika:6
alter table "USER"
    modify SCHOOL_ID null;
--changeset anika:7
alter table "USER"
    modify PASSWORD VARCHAR2(255);
--changeset anika:8
create table REGISTRATION_REQUEST
(
    ID                   NUMBER(38) not null,
    VERIFICATION_STATUS  VARCHAR2(20) default 'PENDING' not null,
    REQUESTED_BY_USER_ID NUMBER(38) not null
        constraint FK_VERIFICATION_REQ_USER
        references "USER",
    REQUEST_DATE         TIMESTAMP(6) not null,
    RESOLVED_BY_USER_ID  NUMBER(38)
        constraint FK_VERIFICATION_RESOLVE_USER
        references "USER",
    RESOLVED_DATE        TIMESTAMP(6)
);
--changeset anika:9
alter table "USER"
    add STATUS VARCHAR2(20);
--changeset anika:10
CREATE TABLE SCHOOL_USER_ROLE_DETAILS
(
    SCHOOL_ID NUMBER(38) REFERENCES SCHOOL(ID),
    USER_ID   NUMBER(38) REFERENCES "USER"(ID),
    ROLE            VARCHAR2(60) not null,
    DETAILS         VARCHAR2(2000)
);
--changeset anika:11
alter table "USER"
drop column DETAILS;
--changeset anika:12
alter table SCHOOL_USER_ROLE_DETAILS
drop column DETAILS;
ALTER TABLE PARENT_STUDENT
    ADD SCHOOL_ID NUMBER(38) DEFAULT 1 NOT NULL REFERENCES SCHOOL(ID);
--changeset anika:13
alter table "USER"
drop column SCHOOL_ID;