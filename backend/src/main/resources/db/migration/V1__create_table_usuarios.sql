-- =====================================================
-- Migration: V1 - Função de trigger e tabela de usuários
-- Descrição: Cria função utilitária para atualização automática de timestamps e tabela de usuários
-- =====================================================

-- Função para atualização automática do campo updated_at
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Tabela de usuários
-- Armazena as credenciais e informações básicas dos usuários do sistema
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_usuario_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuarios(email);

-- Trigger para atualização automática do campo updated_at
CREATE TRIGGER trg_usuarios_updated_at 
    BEFORE UPDATE ON usuarios 
    FOR EACH ROW 
    EXECUTE FUNCTION update_timestamp_column();

-- Comentários de documentação
COMMENT ON TABLE usuarios IS 'Credenciais e perfis de acesso dos usuários';
COMMENT ON COLUMN usuarios.id IS 'Chave primária autoincrementada.';
COMMENT ON COLUMN usuarios.username IS 'Nome de usuário para login (único).';
COMMENT ON COLUMN usuarios.password IS 'Hash da senha (BCrypt).';
COMMENT ON COLUMN usuarios.email IS 'Endereço email único.';
COMMENT ON COLUMN usuarios.ativo IS 'Flag para desativar conta.';
COMMENT ON COLUMN usuarios.last_login IS 'Último acesso.';
COMMENT ON COLUMN usuarios.created_at IS 'Data de registro do usuário no sistema.';
COMMENT ON COLUMN usuarios.updated_at IS 'Data da última alteração no perfil do usuário.';
