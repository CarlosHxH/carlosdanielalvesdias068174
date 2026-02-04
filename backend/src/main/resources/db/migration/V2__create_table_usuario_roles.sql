-- =====================================================
-- Migration: V2 - Criação da tabela de roles de usuário
-- Descrição: Tabela de relacionamento muitos-para-muitos entre usuários e roles
-- =====================================================

-- Tabela de roles de usuário
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    -- Chave composta garante que o usuário não tenha a mesma role duplicada
    PRIMARY KEY (usuario_id, role),
    -- Deleção em cascata: se o usuário for excluído, as roles também são
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    -- CONSTRAINT CRUCIAL: Impede a inserção de strings sem o prefixo ROLE_
    CONSTRAINT ck_role_nome CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

-- Índices para performance em buscas de permissão
CREATE INDEX IF NOT EXISTS idx_usuario_roles_usuario_id ON usuario_roles(usuario_id);

-- Comentários de documentação
COMMENT ON TABLE usuario_roles IS 'Permissões granulares para Spring Security. Exige prefixo ROLE_.';
COMMENT ON COLUMN usuario_roles.usuario_id IS 'Referência ao ID do usuário.';
COMMENT ON COLUMN usuario_roles.role IS 'Nome da role (deve ser ROLE_USER ou ROLE_ADMIN).';