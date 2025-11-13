-- V1: начальная схема

-- ========================
-- Таблица users (учебная)
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    age         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT users_age_non_negative CHECK (age >= 0)
);

-- Индексы по необходимости тут же (пример):
-- CREATE INDEX IF NOT EXISTS idx_users_name ON users (name);

-- =========================
-- Таблица projects (основная)
-- =========================
CREATE TABLE IF NOT EXISTS projects (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    description  VARCHAR(1000),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Индекс по дате создания (если часто сортируем/ищем по дате):
-- CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects (created_at);