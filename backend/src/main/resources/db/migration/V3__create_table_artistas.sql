-- =====================================================
-- Migration: V3 - Criação da tabela de artistas
-- Descrição: Tabela para armazenar informações dos artistas musicais
-- =====================================================

-- Tabela de artistas
-- Armazena informações sobre bandas ou cantores solo
CREATE TABLE IF NOT EXISTS artistas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    genero VARCHAR(50),
    biografia TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índice para otimizar buscas por nome de artista
CREATE INDEX IF NOT EXISTS idx_artista_nome ON artistas(nome);

-- Trigger para atualização automática do campo updated_at
CREATE TRIGGER trg_artistas_updated_at 
    BEFORE UPDATE ON artistas 
    FOR EACH ROW 
    EXECUTE FUNCTION update_timestamp_column();

-- Comentários de documentação
COMMENT ON TABLE artistas IS 'Informações sobre artistas.';
COMMENT ON COLUMN artistas.id IS 'Identificador único do artista.';
COMMENT ON COLUMN artistas.nome IS 'Nome do artista ou banda.';
COMMENT ON COLUMN artistas.genero IS 'Gênero musical do artista.';
COMMENT ON COLUMN artistas.biografia IS 'Biografia ou descrição do artista.';
COMMENT ON COLUMN artistas.created_at IS 'Data de criação do registro.';
COMMENT ON COLUMN artistas.updated_at IS 'Data da última atualização do registro.';
