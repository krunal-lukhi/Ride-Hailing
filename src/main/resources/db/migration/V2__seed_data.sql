INSERT INTO users (name, email, role) VALUES ('Alice Rider', 'alice@test.com', 'RIDER') ON CONFLICT DO NOTHING;
INSERT INTO users (name, email, role) VALUES ('Bob Driver', 'bob@test.com', 'DRIVER') ON CONFLICT DO NOTHING;
