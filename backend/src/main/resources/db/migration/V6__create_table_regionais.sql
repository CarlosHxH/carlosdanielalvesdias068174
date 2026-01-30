-- =====================================================
-- Migration: V6 - Criação da tabela de regionais
-- Descrição: Tabela para armazenar informações das regionais do sistema
-- =====================================================

-- Tabela de regionais
-- Armazena informações das regionais/regiões do sistema
CREATE TABLE IF NOT EXISTS regionais (
    id BIGSERIAL PRIMARY KEY,                                    -- Identificador único da regional
    nome VARCHAR(200) NOT NULL,                                  -- Nome da regional
    ativo BOOLEAN NOT NULL DEFAULT TRUE,                         -- Status de ativação da regional
    data_sincronizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP       -- Data da última sincronização
);

-- Índice para otimizar buscas por status ativo
CREATE INDEX IF NOT EXISTS idx_regionais_ativo ON regionais(ativo);

-- Comentários nas colunas para documentação
COMMENT ON TABLE regionais IS 'Tabela de regionais do sistema';
COMMENT ON COLUMN regionais.id IS 'Identificador único da regional';
COMMENT ON COLUMN regionais.nome IS 'Nome da regional';
COMMENT ON COLUMN regionais.ativo IS 'Indica se a regional está ativa';
COMMENT ON COLUMN regionais.data_sincronizacao IS 'Data e hora da última sincronização';

