create table users (
    id bigint primary key generated always as identity,
    email varchar(30) unique not null,
    password varchar(120) not null,
    role varchar(15) not null,
    last_name varchar(30) not null,
    first_name varchar(30) not null,
    patronymic varchar(30) not null,
    phone_number varchar(12) unique not null

);

create table faculties (
    id smallint primary key generated always as identity,
    number smallint unique not null,
    name varchar(60) unique not null
);

create table positions (
    id smallint primary key generated always as identity,
    name varchar(60) unique not null
);

create table departments (
    id smallint primary key generated always as identity,
    faculty_id smallint,
    name varchar(60) unique not null,

    constraint fk_department_faculty
        foreign key (faculty_id)
        references faculties(id)
);

create table department_positions (
    id int primary key generated always as identity,
    position_id smallint,
    department_id smallint,

    constraint fk_dp_position
        foreign key (position_id)
        references positions(id),
    constraint fk_dp_department
        foreign key (department_id)
        references departments(id)
);

create table teachers (
    user_id bigint,
    experience smallint,
    academic_rank varchar(20),
    academic_degree varchar(30),

    constraint fk_teacher_user
      foreign key (user_id)
          references users(id),

    primary key (user_id)
);

create table teachers_positions (
    teacher_id bigint,
    department_positions_id int,

    constraint fk_tp_teacher
        foreign key (teacher_id)
        references teachers(user_id),
    constraint fk_tp_position
        foreign key (department_positions_id)
        references department_positions(id),

    primary key (teacher_id, department_positions_id)
);

create table specialties (
    id smallint primary key generated always as identity,
    name varchar(30) not null unique,
    degree varchar(20) not null,
    faculty_id smallint,

    constraint fk_specialty_faculty
        foreign key (faculty_id)
        references faculties(id)
);

create table groups (
    id int primary key generated always as identity,
    name varchar(10) not null,
    course smallint not null,
    specialty_id smallint,

    constraint fk_group_specialty
        foreign key (specialty_id)
        references specialties(id)
);

create table subjects (
    id int primary key generated always as identity,
    name varchar(30) not null unique,
    description varchar(255) not null
);

create table group_subjects(
    id int primary key generated always as identity,
    group_id int,
    subject_id int,
    term smallint not null,
    hours smallint not null,
    type_of_control varchar(10) not null,

    constraint fk_gs_group
        foreign key (group_id)
        references groups(id),
    constraint fk_gs_subject
        foreign key (subject_id)
        references subjects(id)
);

create table students (
    user_id bigint,
    group_id int,

    constraint fk_student_user
        foreign key (user_id)
        references users(id),
    constraint fk_student_group
        foreign key (group_id)
        references groups(id),

    primary key (user_id)
);

create table score_sheets (
    id bigint primary key generated always as identity,
    student_id bigint,
    group_subject_id int,
    assessment varchar(5),

    constraint fk_ss_student
        foreign key (student_id)
        references students(user_id),
    constraint fk_ss_subject
        foreign key (group_subject_id)
        references group_subjects(id)
);

create table applicants (
    user_id bigint,
    scores smallint,
    specialty_id smallint,

    constraint fk_applicant_user
        foreign key (user_id)
        references users(id),
    constraint fk_applicant_specialty
        foreign key (specialty_id)
        references specialties(id),

    primary key (user_id)
);

create table schedule (
    id bigint primary key generated always as identity,
    teacher_id bigint,
    subject_id int,
    time_from time not null,
    time_to time not null,
    date date not null,
    audience varchar(10) not null,

    constraint fk_schedule_teacher
        foreign key (teacher_id)
        references teachers(user_id),
    constraint fk_schedule_subject
        foreign key (subject_id)
        references subjects(id)
);

