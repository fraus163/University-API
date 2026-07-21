create table schedule_groups(
    schedule_id bigint,
    group_id int,

    constraint fk_sg_schedule
        foreign key (schedule_id)
        references schedule(id),
    constraint fk_sg_group
        foreign key (group_id)
        references groups(id)
);