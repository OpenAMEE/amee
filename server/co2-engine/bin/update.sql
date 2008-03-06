update amee.TARGET set TYPE = 0;
update amee.TARGET set TYPE = 1 where DIRECTORY_TARGET = 1;
alter table amee.TARGET drop column DIRECTORY_TARGET;