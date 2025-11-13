-- 1) Если таблицы нет — создаём правильную
CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,
  email         TEXT,
  password_hash TEXT,
  role          VARCHAR(20),
  created_at    TIMESTAMPTZ DEFAULT now()
);

-- 2) Если таблица уже была (старая) — добавим недостающие столбцы
ALTER TABLE users ADD COLUMN IF NOT EXISTS email         TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role          VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at    TIMESTAMPTZ;

-- 3) Заполним разумные значения там, где NULL (на случай старых данных)
UPDATE users SET role = 'USER'      WHERE role IS NULL;
UPDATE users SET created_at = now() WHERE created_at IS NULL;

-- 4) Сделаем нужные NOT NULL (после того как всё заполнено)
ALTER TABLE users ALTER COLUMN email         SET NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE users ALTER COLUMN role          SET NOT NULL;
ALTER TABLE users ALTER COLUMN created_at    SET NOT NULL;

-- 5) Кейс-инсенситивная уникальность email (через индекс по lower(email))
-- Если нужен именно CONSTRAINT, можно: CREATE UNIQUE INDEX ... и оставить @Column(unique=true) в JPA.
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_ci ON users (lower(email));