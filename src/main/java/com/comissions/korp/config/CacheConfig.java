package com.comissions.korp.config;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Cache do painel do vendedor no Redis (padrão cache-aside, via anotações do Spring).
 *
 * <ul>
 *   <li>Leitura: {@code @Cacheable} em HomeVendedorService.buscarPainel. Hit devolve o JSON guardado;
 *   miss consulta o MySQL e guarda o resultado.</li>
 *   <li>Escrita: os métodos que mudam pedidos, itens ou comissões chamam PainelVendedorCache, que apaga
 *   o cache depois do commit; a próxima leitura recalcula.</li>
 *   <li>TTL: cada chave expira sozinha (padrão 10 min), uma rede de segurança para dados que o cache
 *   não acompanha, como o nome do cliente.</li>
 * </ul>
 * Chaves no Redis: {@code korp:painelVendedor::<vendedor>:<ano>-<mes>:<dia>:<filtro>:p<pagina>:s<tamanho>}.
 * Nos testes o cache é em memória ({@code spring.cache.type=simple}) e esta configuração do Redis não é usada.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String PAINEL_VENDEDOR = "painelVendedor";
    public static final String CHAVE_PAINEL_VENDEDOR = "chavePainelVendedor";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Ajusta o gerenciador de cache do Redis que o Spring Boot cria:
     * prefixo "korp:", valores em JSON, TTL do painel e limpeza com SCAN.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer configuracaoDoRedis(
            RedisConnectionFactory conexao,
            JsonMapper jsonMapper,
            @Value("${korp.cache.painel-vendedor.ttl:10m}") Duration ttlPainelVendedor,
            // Os testes usam outro prefixo para nunca misturar dados de teste com os da aplicação no mesmo Redis
            @Value("${korp.cache.prefixo:korp:}") String prefixo
    ) {
        RedisCacheConfiguration painelVendedor = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(prefixo)
                .entryTtl(ttlPainelVendedor)
                .disableCachingNullValues()
                // JSON legível no Redis, com o tipo do painel para voltar do JSON sem perder nada
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, HomeVendedorPainelDTO.class)));

        return builder -> builder
                // SCAN em vez de KEYS ao limpar o cache: não trava o Redis quando há muitas chaves
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(conexao, BatchStrategies.scan(100)))
                // Gravação e limpeza do cache só acontecem depois do commit da transação
                .transactionAware()
                .withCacheConfiguration(PAINEL_VENDEDOR, painelVendedor);
    }

    /**
     * Chave do painel: vendedor + período + dia + filtro + página. Sem ano/mês na URL, o painel é do mês
     * atual, então a chave usa o mês atual (e não "null"): ao virar o mês, a chave muda sozinha.
     * Parâmetros na ordem de HomeVendedorService.buscarPainel(idVendedor, ano, mes, dia, filtro, pageable).
     */
    @Bean(CHAVE_PAINEL_VENDEDOR)
    public KeyGenerator chavePainelVendedor() {
        return (alvo, metodo, parametros) -> {
            LocalDate hoje = LocalDate.now();
            Integer ano = parametros[1] != null ? (Integer) parametros[1] : hoje.getYear();
            Integer mes = parametros[2] != null ? (Integer) parametros[2] : hoje.getMonthValue();
            Integer dia = (Integer) parametros[3];
            FiltroStatusVenda filtro = (FiltroStatusVenda) parametros[4];
            Pageable pagina = (Pageable) parametros[5];
            return parametros[0] + ":" + ano + "-" + mes + ":" + (dia == null ? "mes" : "dia" + dia)
                    + ":" + filtro + ":p" + pagina.getPageNumber() + ":s" + pagina.getPageSize();
        };
    }

    /**
     * Se o Redis cair ou falhar, o painel continua funcionando: o erro vai para o log e a consulta segue
     * direto para o banco (como se fosse um cache miss), em vez de virar erro 500 para o vendedor.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException erro, Cache cache, Object chave) {
                log.warn("Cache indisponível ao ler {} [{}]; consultando o banco. Causa: {}", cache.getName(), chave, erro.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException erro, Cache cache, Object chave, Object valor) {
                log.warn("Cache indisponível ao gravar {} [{}]. Causa: {}", cache.getName(), chave, erro.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException erro, Cache cache, Object chave) {
                log.warn("Cache indisponível ao apagar {} [{}]; o dado antigo expira pelo TTL. Causa: {}", cache.getName(), chave, erro.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException erro, Cache cache) {
                log.warn("Cache indisponível ao limpar {}; os dados antigos expiram pelo TTL. Causa: {}", cache.getName(), erro.getMessage());
            }
        };
    }
}
