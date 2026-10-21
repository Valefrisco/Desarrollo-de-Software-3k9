-- Lo ejecuta Spring Boot al arrancar (spring.sql.init.mode=always), antes de que
-- Hibernate cree las tablas. Hibernate crea las tablas pero no el esquema.
CREATE SCHEMA IF NOT EXISTS modeloventas;
