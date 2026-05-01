INSERT INTO languages (code, name, active)
SELECT 'en', 'English', TRUE
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE code = 'en');

INSERT INTO languages (code, name, active)
SELECT 'vi', 'Vietnamese', TRUE
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE code = 'vi');

INSERT INTO languages (code, name, active)
SELECT 'ja', 'Japanese', TRUE
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE code = 'ja');

INSERT INTO languages (code, name, active)
SELECT 'ko', 'Korean', TRUE
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE code = 'ko');

INSERT INTO languages (code, name, active)
SELECT 'zh', 'Chinese', TRUE
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE code = 'zh');

INSERT INTO topics (name, description, active)
SELECT 'Daily Life', 'Common vocabulary for daily life', TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics WHERE name = 'Daily Life');

INSERT INTO topics (name, description, active)
SELECT 'Travel', 'Vocabulary for travel situations', TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics WHERE name = 'Travel');

INSERT INTO topics (name, description, active)
SELECT 'Business', 'Vocabulary for business communication', TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics WHERE name = 'Business');

INSERT INTO topics (name, description, active)
SELECT 'Grammar', 'Grammar-focused study content', TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics WHERE name = 'Grammar');

INSERT INTO topics (name, description, active)
SELECT 'Exam Preparation', 'Vocabulary for exams and certificates', TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics WHERE name = 'Exam Preparation');
