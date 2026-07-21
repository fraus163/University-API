ALTER TABLE score_sheets
ADD CONSTRAINT uk_student_group_subject UNIQUE (student_id, group_subject_id);