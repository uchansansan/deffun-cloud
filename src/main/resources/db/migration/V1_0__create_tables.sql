CREATE TABLE IF NOT EXISTS `users` (
     id SERIAL,
     username VARCHAR (255) NOT NULL,
     email VARCHAR (255) NOT NULL,
     ssh_public_key VARCHAR (255),
     PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `projects` (
     id SERIAL,
     PRIMARY KEY (id)
);
