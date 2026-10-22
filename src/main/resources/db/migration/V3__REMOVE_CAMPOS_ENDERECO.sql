-- O DROP só roda se a coluna existir; o ambiente pode ter removido tipo_endereco manualmente.
SET @tem_tipo_endereco = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'endereco' AND COLUMN_NAME = 'tipo_endereco'
);
SET @sql_drop = IF(@tem_tipo_endereco > 0, 'ALTER TABLE endereco DROP COLUMN tipo_endereco', 'SELECT 1');
PREPARE stmt_drop FROM @sql_drop;
EXECUTE stmt_drop;
DEALLOCATE PREPARE stmt_drop;