-- V2_notifications.sql

CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              message TEXT NOT NULL,
                              type VARCHAR(50) NOT NULL,
                              is_read BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- Add flags to the loan table so the LoanReminderJob doesn't send duplicate emails
ALTER TABLE loan ADD COLUMN due_soon_notified_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE loan ADD COLUMN overdue_notified_at TIMESTAMP WITH TIME ZONE;