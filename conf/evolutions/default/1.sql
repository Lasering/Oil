# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "userrow__test" ("id" INTEGER NOT NULL PRIMARY KEY,"gender" VARCHAR NOT NULL,"street_address" VARCHAR NOT NULL,"city" VARCHAR NOT NULL,"zip_code" VARCHAR NOT NULL,"country" VARCHAR NOT NULL,"email" VARCHAR NOT NULL,"telephone_number" VARCHAR NOT NULL);

# --- !Downs

drop table "userrow__test";

