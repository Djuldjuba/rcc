DELETE FROM painting;

INSERT INTO painting (id, title, description, content, artist_id, museum_id)
VALUES (
    CAST('11111111-1111-1111-1111-111111111111' AS UUID),
    'Над вечным покоем',
    'Пейзаж Левитана',
    NULL,
    CAST('22222222-2222-2222-2222-222222222222' AS UUID),
    CAST('33333333-3333-3333-3333-333333333333' AS UUID)
);