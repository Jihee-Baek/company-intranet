-- 부서 초기 데이터 주입
INSERT INTO departments (id, name) VALUES 
(1, 'Web솔루션개발팀'),
(2, '응용개발팀'),
(3, 'GIS소프트팀'),
(4, '경영지원실'),
(5, '신사업개발팀')
ON CONFLICT (id) DO NOTHING;

-- 자동 증가 일련번호(Sequence) 동기화
SELECT setval(pg_get_serial_sequence('departments', 'id'), coalesce(max(id), 1)) FROM departments;