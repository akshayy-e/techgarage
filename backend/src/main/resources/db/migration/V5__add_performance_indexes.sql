-- Performance indexes added by QA hardening.
CREATE INDEX idx_users_role_enabled ON users(role, enabled);
CREATE INDEX idx_problems_status_created ON problems(status, created_at);
CREATE INDEX idx_problems_client_created ON problems(client_id, created_at);
CREATE INDEX idx_proposals_problem_created ON proposals(problem_id, created_at);
CREATE INDEX idx_jobs_created ON jobs(created_at);
CREATE INDEX idx_notifications_user_read_created ON notifications(user_id, is_read, created_at);
