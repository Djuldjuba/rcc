DELETE FROM museum;

INSERT INTO museum (id, title, description, city, photo, country_id)
VALUES (X'11111111111111111111111111111111', 'Эрмитаж', 'Крупнейший музей России', 'Санкт-Петербург', NULL, X'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa');

INSERT INTO museum (id, title, description, city, photo, country_id)
VALUES (X'22222222222222222222222222222222', 'Лувр', 'Знаменитый музей Франции', 'Париж', NULL, X'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb');

INSERT INTO museum (id, title, description, city, photo, country_id)
VALUES (X'33333333333333333333333333333333', 'Прадо', 'Главный музей Испании', 'Мадрид', NULL, X'cccccccccccccccccccccccccccccccc');