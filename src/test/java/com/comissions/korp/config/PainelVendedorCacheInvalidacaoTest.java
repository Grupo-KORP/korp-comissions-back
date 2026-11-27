package com.comissions.korp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

/** O cache do painel só é apagado depois do commit; no rollback continua como estava. */
class PainelVendedorCacheInvalidacaoTest {

    private Cache cache;
    private PainelVendedorCache painelVendedorCache;

    @BeforeEach
    void setUp() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(CacheConfig.PAINEL_VENDEDOR);
        cache = cacheManager.getCache(CacheConfig.PAINEL_VENDEDOR);
        cache.put("99:2026-11:mes:TODAS:p0:s5", "painel guardado");
        painelVendedorCache = new PainelVendedorCache(cacheManager);
    }

    @AfterEach
    void limparTransacao() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void dentroDeTransacao_apagaSoDepoisDoCommit() {
        TransactionSynchronizationManager.initSynchronization();

        painelVendedorCache.invalidarAposCommit();
        assertThat(cache.get("99:2026-11:mes:TODAS:p0:s5")).isNotNull();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(cache.get("99:2026-11:mes:TODAS:p0:s5")).isNull();
    }

    @Test
    void dentroDeTransacao_naoApagaNoRollback() {
        TransactionSynchronizationManager.initSynchronization();

        painelVendedorCache.invalidarAposCommit();
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sincronizacao -> sincronizacao.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(cache.get("99:2026-11:mes:TODAS:p0:s5")).isNotNull();
    }

    @Test
    void semTransacao_apagaNaHora() {
        painelVendedorCache.invalidarAposCommit();

        assertThat(cache.get("99:2026-11:mes:TODAS:p0:s5")).isNull();
    }
}
