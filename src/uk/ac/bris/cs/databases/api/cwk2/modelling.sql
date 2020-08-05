DROP TABLE IF EXISTS Purchase_list;
DROP TABLE IF EXISTS Commodity;
DROP TABLE IF EXISTS Bank_card;
DROP TABLE IF EXISTS User;

CREATE TABLE User (
   id                INTEGER        PRIMARY KEY   AUTO_INCREMENT,
   user_name         VARCHAR(50)    NOT NULL      UNIQUE,
   phone_number      CHAR(11)       NOT NULL,
   address           VARCHAR(500)   NULL
);

CREATE TABLE Bank_card (
   card_number       CHAR(16)       NOT NULL      PRIMARY KEY,
   bank              VARCHAR(50)    NOT NULL,
   owner_id          INTEGER        NOT NULL,
   FOREIGN KEY (owner_id) REFERENCES User(id)
);

CREATE TABLE Commodity (
   id                INTEGER        PRIMARY KEY   AUTO_INCREMENT,
   name              VARCHAR(50)    NOT NULL,
   price             INTEGER        NOT NULL,
   photo             BLOB           NULL,
   description       VARCHAR(1000)  NULL,
   classification    VARCHAR(50)    NOT NULL
);

CREATE TABLE Purchase_list (
   user_id           INTEGER        NOT NULL,
   commodity_id      INTEGER        NOT NULL,
   quantity          INTEGER        NOT NULL,
   FOREIGN KEY (user_id) REFERENCES User(id),
   FOREIGN KEY (commodity_id) REFERENCES Commodity(id),
   PRIMARY KEY (user_id, commodity_id),
   CHECK (quantity > 0)
);
