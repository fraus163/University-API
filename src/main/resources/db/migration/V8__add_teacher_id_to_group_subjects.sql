alter table group_subjects
add column IF NOT EXISTS teacher_id bigint;

alter table group_subjects
add constraint fk_gs_teacher foreign key (teacher_id) references teachers(user_id);