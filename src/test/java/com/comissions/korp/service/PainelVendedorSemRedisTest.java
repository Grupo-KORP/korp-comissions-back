package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Com o cache apontando para um Redis que não existe, o painel continua respondendo:
 * o CacheErrorHandler do CacheConfig registra o erro e a consulta vai direto ao banco.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:korp-cache-sem-redis-it;MODE=MySQL",
        "spring.cache.type=redis",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6399",
        "spring.data.redis.timeout=500ms",
        "spring.data.redis.connect-timeout=500ms"
})
@ActiveProfiles("test")
class PainelVendedorSemRedisTest {

    @Autowired private HomeVendedorService homeVendedorService;
    @Autowired private CacheManager cacheManager;

    @Test
    void painelFuncionaMesmoComORedisForaDoAr() {
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);

        HomeVendedorPainelDTO painel = homeVendedorService.buscarPainel(
                999, 2026, 11, null, FiltroStatusVenda.TODAS, PageRequest.of(0, 5));

        assertThat(painel.ano()).isEqualTo(2026);
        assertThat(painel.mes()).isEqualTo(11);
        assertThat(painel.linhas().content()).isEmpty();
    }
}
