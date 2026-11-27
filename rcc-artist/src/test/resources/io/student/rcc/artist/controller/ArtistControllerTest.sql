DELETE FROM artist;

INSERT INTO artist (id, name, biography, photo)
VALUES (
    CAST('11111111-1111-1111-1111-111111111111' AS UUID),
    'Иван Айвазовский',
    'Русский художник-маринист',
    NULL
);

INSERT INTO artist (id, name, biography, photo)
VALUES (
    CAST('22222222-2222-2222-2222-222222222222' AS UUID),
    'Исаак Левитан',
    'Русский художник-пейзажист',
    NULL
);