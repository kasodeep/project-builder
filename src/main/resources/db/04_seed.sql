INSERT INTO project (id,
                     name,
                     team_id,
                     owner,
                     progress,
                     start_date,
                     end_date)
VALUES ('proj_12',
        'Analytics Demo Project',
        (SELECT team_id FROM "user" WHERE username = 'deep'),
        'deep',
        35,
        CURRENT_DATE - 15,
        CURRENT_DATE + 30);

INSERT INTO task
(id, project_id, feature_id, name, priority, status,
 start_date, end_date,
 started_at, completed_at, updated_by)
VALUES

-- Root
('task_1', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Architecture Setup', 1, 'COMPLETED',
 current_date - 20, current_date - 18,
 now() - interval '20 days', now() - interval '18 days', 'deep'),

-- Core
('task_2', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Core Module A', 2, 'COMPLETED',
 current_date - 18, current_date - 12,
 now() - interval '18 days', now() - interval '12 days', 'ayush'),

('task_3', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Core Module B', 2, 'ACTIVE',
 current_date - 10, current_date + 5,
 now() - interval '7 days', NULL, 'aniket'),

('task_4', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Security Base Layer', 2, 'ACTIVE',
 current_date - 9, current_date + 6,
 now() - interval '6 days', NULL, 'krish'),

-- Services
('task_5', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Service A1', 3, 'ACTIVE',
 current_date - 6, current_date + 7,
 now() - interval '5 days', NULL, 'deep'),

('task_6', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Service A2', 3, 'ACTIVE',
 current_date - 6, current_date + 7,
 now() - interval '5 days', NULL, 'ayush'),

('task_7', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Service B1', 3, 'ACTIVE',
 current_date - 5, current_date + 8,
 now() - interval '4 days', NULL, 'aniket'),

('task_8', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Auth Hardening', 3, 'ACTIVE',
 current_date - 5, current_date + 8,
 now() - interval '4 days', NULL, 'krish'),

-- Testing (planned future work)
('task_9', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Unit Test A1', 3, 'PENDING',
 current_date + 6, current_date + 12,
 NULL, NULL, 'deep'),

('task_10', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Unit Test A2', 3, 'PENDING',
 current_date + 6, current_date + 12,
 NULL, NULL, 'ayush'),

('task_11', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Unit Test B1', 3, 'PENDING',
 current_date + 7, current_date + 13,
 NULL, NULL, 'aniket'),

('task_12', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Security Testing', 3, 'PENDING',
 current_date + 7, current_date + 13,
 NULL, NULL, 'krish'),

-- Integration
('task_13', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Integration Layer', 4, 'PENDING',
 current_date + 14, current_date + 20,
 NULL, NULL, 'deep'),

-- Pre Release
('task_14', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'API Stabilization', 4, 'PENDING',
 current_date + 21, current_date + 25,
 NULL, NULL, 'aniket'),

('task_15', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Security Audit', 4, 'PENDING',
 current_date + 21, current_date + 25,
 NULL, NULL, 'krish'),

('task_16', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Regression Testing', 4, 'PENDING',
 current_date + 26, current_date + 30,
 NULL, NULL, 'ayush'),

('task_17', 'proj_12', '8c3afd25-8d12-4f9a-a1ff-c3ffc73e9487',
 'Load Testing', 4, 'PENDING',
 current_date + 26, current_date + 30,
 NULL, NULL, 'aniket'),

-- Final Sprint
('task_18', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Bug Fix Sprint', 4, 'PENDING',
 current_date + 31, current_date + 35,
 NULL, NULL, 'deep'),

('task_19', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Final Deployment', 5, 'PENDING',
 current_date + 36, current_date + 37,
 NULL, NULL, 'deep'),

-- Security chain
('task_20', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Threat Modeling', 4, 'PENDING',
 current_date + 10, current_date + 15,
 NULL, NULL, 'krish'),

('task_21', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Pen Testing', 4, 'PENDING',
 current_date + 16, current_date + 22,
 NULL, NULL, 'krish'),

('task_22', 'proj_12', 'fd2686c6-3ce7-4cc4-9f5f-de01f570eaaa',
 'Patch Cycle', 4, 'PENDING',
 current_date + 23, current_date + 28,
 NULL, NULL, 'krish'),

-- Release
('task_23', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Release Candidate', 5, 'PENDING',
 current_date + 38, current_date + 40,
 NULL, NULL, 'deep'),

('task_24', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Production Release', 5, 'PENDING',
 current_date + 41, current_date + 42,
 NULL, NULL, 'deep'),

('task_25', 'proj_12', '652fda76-37b0-43f1-bb31-ec667333440a',
 'Post Release Monitoring', 5, 'PENDING',
 current_date + 43, current_date + 50,
 NULL, NULL, 'deep');

INSERT INTO task_assignee (task_id, user_id)
VALUES ('task_1', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_2', '007e2edb-b739-40fc-a59e-7c048d9d563a'),
       ('task_3', '69dddc3c-2c2f-4ede-b28b-bef398ba262f'),
       ('task_4', 'e7f49e17-5171-464f-b163-7e70743219e8'),

       ('task_5', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_6', '007e2edb-b739-40fc-a59e-7c048d9d563a'),
       ('task_7', '69dddc3c-2c2f-4ede-b28b-bef398ba262f'),
       ('task_8', 'e7f49e17-5171-464f-b163-7e70743219e8'),

       ('task_9', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_10', '007e2edb-b739-40fc-a59e-7c048d9d563a'),
       ('task_11', '69dddc3c-2c2f-4ede-b28b-bef398ba262f'),
       ('task_12', 'e7f49e17-5171-464f-b163-7e70743219e8'),

       ('task_13', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_14', '69dddc3c-2c2f-4ede-b28b-bef398ba262f'),
       ('task_15', 'e7f49e17-5171-464f-b163-7e70743219e8'),
       ('task_16', '007e2edb-b739-40fc-a59e-7c048d9d563a'),
       ('task_17', '69dddc3c-2c2f-4ede-b28b-bef398ba262f'),

       ('task_18', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_19', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),

       ('task_20', 'e7f49e17-5171-464f-b163-7e70743219e8'),
       ('task_21', 'e7f49e17-5171-464f-b163-7e70743219e8'),
       ('task_22', 'e7f49e17-5171-464f-b163-7e70743219e8'),

       ('task_23', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_24', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb'),
       ('task_25', '6fffd5b6-7027-4cb6-b69a-32ec2f69cefb');

INSERT INTO task_dependency (task_id, depends_on_task_id)
VALUES
-- Root dependency
('task_2', 'task_1'),
('task_3', 'task_1'),
('task_4', 'task_1'),

-- Service layer
('task_5', 'task_2'),
('task_6', 'task_2'),
('task_7', 'task_3'),
('task_8', 'task_4'),

-- Testing layer
('task_9', 'task_5'),
('task_10', 'task_6'),
('task_11', 'task_7'),
('task_12', 'task_8'),

-- Integration depends on all tests
('task_13', 'task_9'),
('task_13', 'task_10'),
('task_13', 'task_11'),
('task_13', 'task_12'),

-- Pre release
('task_14', 'task_13'),
('task_15', 'task_13'),
('task_16', 'task_14'),
('task_17', 'task_14'),

-- Security chain
('task_20', 'task_4'),
('task_21', 'task_20'),
('task_22', 'task_21'),

-- Final Sprint
('task_18', 'task_16'),
('task_18', 'task_17'),
('task_18', 'task_15'),

('task_19', 'task_18'),

-- Release chain
('task_23', 'task_19'),
('task_24', 'task_23'),
('task_25', 'task_24');
