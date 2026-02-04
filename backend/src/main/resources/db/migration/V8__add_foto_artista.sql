-- =====================================================
-- Migration: V8 - Foto do artista
-- Descrição: Adiciona coluna para armazenar referência da foto no MinIO
-- =====================================================

ALTER TABLE artistas
ADD COLUMN IF NOT EXISTS foto_nome_arquivo VARCHAR(500) NULL;

COMMENT ON COLUMN artistas.foto_nome_arquivo IS 'Chave do objeto no MinIO (ex: artistas/1/uuid_foto.jpg)';
