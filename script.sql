-- ==========================================
-- BASE DE DATOS
-- ==========================================

CREATE DATABASE IF NOT EXISTS inventario_db;

USE inventario_db;


-- ==========================================
-- TABLA: CATEGORIAS
-- ==========================================

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo_prefijo CHAR(3) NOT NULL,

    CONSTRAINT uk_categoria_nombre
        UNIQUE (nombre),

    CONSTRAINT uk_categoria_codigo_prefijo
        UNIQUE (codigo_prefijo),

    CONSTRAINT chk_categoria_codigo_prefijo
        CHECK (CHAR_LENGTH(codigo_prefijo) = 3)
);


-- ==========================================
-- TABLA: ACTIVOS
-- ==========================================

CREATE TABLE IF NOT EXISTS activos (
    identificador_tecnico CHAR(36) PRIMARY KEY,
    folio_inventario VARCHAR(20) NOT NULL,
    numero_serie VARCHAR(100) NOT NULL,
    marca_modelo VARCHAR(150) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    costo_adquisicion DECIMAL(19,2) NOT NULL,
    fecha_ingreso DATETIME(6) NOT NULL,
    categoria_id BIGINT NOT NULL,

    CONSTRAINT uk_activo_folio
        UNIQUE (folio_inventario),

    CONSTRAINT uk_activo_numero_serie
        UNIQUE (numero_serie),

    CONSTRAINT fk_activo_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id),

    CONSTRAINT chk_activo_estado
        CHECK (
            estado IN (
                'DISPONIBLE',
                'ASIGNADO',
                'EN_MANTENIMIENTO',
                'BAJA'
            )
        ),

    CONSTRAINT chk_activo_costo
        CHECK (costo_adquisicion >= 0)
);


-- ==========================================
-- TABLA: USUARIOS
-- ==========================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,

    CONSTRAINT uk_usuario_username
        UNIQUE (username),

    CONSTRAINT chk_usuario_rol
        CHECK (
            rol IN ('ADMIN', 'USER')
        )
);


-- ==========================================
-- ÍNDICES PARA CONSULTAS
-- ==========================================

CREATE INDEX idx_activo_categoria
    ON activos(categoria_id);

CREATE INDEX idx_activo_estado
    ON activos(estado);

CREATE INDEX idx_activo_marca_modelo
    ON activos(marca_modelo);

CREATE INDEX idx_activo_fecha_ingreso
    ON activos(fecha_ingreso);

CREATE INDEX idx_activo_costo
    ON activos(costo_adquisicion);


-- ==========================================
-- DATOS INICIALES DE CATEGORIAS
-- ==========================================

INSERT IGNORE INTO categorias (nombre, codigo_prefijo)
VALUES
    ('Laptop', 'LAP'),
    ('Monitor', 'MON'),
    ('Celular', 'CEL');


-- ==========================================
-- USUARIOS INICIALES
-- ==========================================
-- IMPORTANTE:
-- Estos passwords son temporales para desarrollo.
-- Posteriormente deben almacenarse utilizando BCrypt.

INSERT IGNORE INTO usuarios (username, password, rol)
VALUES
    ('admin', 'admin123', 'ADMIN'),
    ('user', 'user123', 'USER');