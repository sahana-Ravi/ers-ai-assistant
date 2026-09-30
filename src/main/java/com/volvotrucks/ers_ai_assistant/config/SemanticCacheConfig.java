package com.volvotrucks.ers_ai_assistant.config;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;


@Configuration
public class SemanticCacheConfig {

    @Bean
    public RedisClient redisClient(@Value("${spring.data.redis.host:localhost}") String host, @Value("${spring.data.redis.port:6379}") int port) {
        return RedisClient.builder().hostAndPort(host, port).build();
    }
    @Bean
    SemanticCache semanticCache(
            RedisClient redisClient,
            EmbeddingModel embeddingModel) {

        return DefaultSemanticCache.builder()
                .jedisClient(redisClient)
                .embeddingModel(embeddingModel)
                .indexName("semantic-cache-index")
                .prefix("semantic-cache:")
                .similarityThreshold(0.95)
                .build();
    }

    @Bean
    public SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache semanticCache) {
        return SemanticCacheAdvisor.builder().cache(semanticCache).build();
    }
}
