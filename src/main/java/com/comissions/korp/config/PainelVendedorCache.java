package com.comissions.korp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Invalidação do cache do painel do vendedor (o lado "escrita" do cache-aside).
 *
 * <p>Os métodos que mudam pedidos, itens ou comissões chamam {@link #invalidarAposCommit()}: quando a
 * transação faz commit, todos os painéis guardados são apagados na hora e a próxima leitura recalcula.
 * Se a transação for desfeita (rollback), nada é apagado.</p>
 *
 * <p>Por que não {@code @CacheEvict(allEntries = true)}: ele usa {@code Cache.clear()}, que no Redis
 * é assíncrono. No teste com o Redis real a chave ainda existia logo depois em 16 de 20 vezes, e o
 * painel recarregado logo após finalizar um pedido podia vir antigo. {@code Cache.invalidate()} apaga
 * na hora (0 de 20).</p>
 */
@Component
public class PainelVendedorCache {

    private static final Logger log = LoggerFactory.getLogger(PainelVendedorCache.class);

    private final CacheManager cacheManager;

    public PainelVendedorCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Agenda a invalidação para depois do commit da transação atual (ou faz na hora, se não houver transação). */
    public void invalidarAposCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidarAgora();
                }
            });
        } else {
            invalidarAgora();
        }
    }

    private void invalidarAgora() {
        Cache cache = cacheManager.getCache(CacheConfig.PAINEL_VENDEDOR);
        if (cache == null) {
            return;
        }
        try {
            cache.invalidate();
        } catch (RuntimeException erro) {
            // Redis fora do ar: a gravação já foi feita; o painel antigo expira pelo TTL
            log.warn("Não foi possível apagar o cache {}; os painéis antigos expiram pelo TTL. Causa: {}",
                    CacheConfig.PAINEL_VENDEDOR, erro.getMessage());
        }
    }
}
