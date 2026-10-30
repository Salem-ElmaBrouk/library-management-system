-- V1_init.sql

CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          first_name VARCHAR(100),
                          last_name VARCHAR(100),
                          role VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- PostgreSQL unique index for case-insensitive email searches
CREATE UNIQUE INDEX idx_app_user_email_lower ON app_user (LOWER(email));

CREATE TABLE book (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      author VARCHAR(255) NOT NULL,
                      isbn VARCHAR(50),
                      category VARCHAR(100),
                      available_copies INTEGER NOT NULL DEFAULT 0,
                      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan (
                      id BIGSERIAL PRIMARY KEY,
                      book_id BIGINT NOT NULL,
                      user_id BIGINT NOT NULL,
                      borrowed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      due_date TIMESTAMP WITH TIME ZONE NOT NULL,
                      returned_at TIMESTAMP WITH TIME ZONE,
                      CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book (id),
                      CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- Index for performance (finding loans by user or book quickly)
CREATE INDEX idx_loan_user_id ON loan (user_id);
CREATE INDEX idx_loan_book_id ON loan (book_id);