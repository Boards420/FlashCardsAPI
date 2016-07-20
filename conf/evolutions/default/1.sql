# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table answer (
  answerId                  bigint auto_increment not null,
  answer_text               varchar(255),
  hint_text                 varchar(255),
  media_uri                 varchar(255),
  author_id                 bigint,
  parent_card_id            bigint,
  rating                    integer,
  created                   datetime(6) not null,
  last_updated              datetime(6) not null,
  constraint pk_answer primary key (answerId))
;

create table flash_card (
  flashcardId               bigint auto_increment not null,
  rating                    integer,
  question_id               bigint,
  author_id                 bigint,
  multiple_choice           tinyint(1) default 0,
  created                   datetime(6) not null,
  last_updated              datetime(6) not null,
  constraint uq_flash_card_question_id unique (question_id),
  constraint pk_flash_card primary key (flashcardId))
;

create table question (
  questionId                bigint auto_increment not null,
  question_text             varchar(255),
  media_uri                 varchar(255),
  author_id                 bigint,
  constraint pk_question primary key (questionId))
;

create table tag (
  tagId                     bigint auto_increment not null,
  name                      varchar(255),
  constraint uq_tag_name unique (name),
  constraint pk_tag primary key (tagId))
;

create table user (
  userId                    bigint auto_increment not null,
  name                      varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  rating                    integer,
  groupId                   bigint,
  created                   datetime(6) not null,
  constraint uq_user_email unique (email),
  constraint pk_user primary key (userId))
;

create table user_group (
  groupId                   bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_user_group primary key (groupId))
;


create table card_tag (
  card_id                        bigint not null,
  tag_id                         bigint not null,
  constraint pk_card_tag primary key (card_id, tag_id))
;
alter table answer add constraint fk_answer_author_1 foreign key (author_id) references user (userId) on delete restrict on update restrict;
create index ix_answer_author_1 on answer (author_id);
alter table answer add constraint fk_answer_card_2 foreign key (parent_card_id) references flash_card (flashcardId) on delete restrict on update restrict;
create index ix_answer_card_2 on answer (parent_card_id);
alter table flash_card add constraint fk_flash_card_question_3 foreign key (question_id) references question (questionId) on delete restrict on update restrict;
create index ix_flash_card_question_3 on flash_card (question_id);
alter table flash_card add constraint fk_flash_card_author_4 foreign key (author_id) references user (userId) on delete restrict on update restrict;
create index ix_flash_card_author_4 on flash_card (author_id);
alter table question add constraint fk_question_author_5 foreign key (author_id) references user (userId) on delete restrict on update restrict;
create index ix_question_author_5 on question (author_id);
alter table user add constraint fk_user_group_6 foreign key (groupId) references user_group (groupId) on delete restrict on update restrict;
create index ix_user_group_6 on user (groupId);



alter table card_tag add constraint fk_card_tag_flash_card_01 foreign key (card_id) references flash_card (flashcardId) on delete restrict on update restrict;

alter table card_tag add constraint fk_card_tag_tag_02 foreign key (tag_id) references tag (tagId) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table answer;

drop table flash_card;

drop table card_tag;

drop table question;

drop table tag;

drop table user;

drop table user_group;

SET FOREIGN_KEY_CHECKS=1;

