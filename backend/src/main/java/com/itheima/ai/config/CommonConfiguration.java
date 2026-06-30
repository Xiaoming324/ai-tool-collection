package com.itheima.ai.config;

import com.itheima.ai.contant.SystemConstants;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

@Configuration
public class CommonConfiguration {

    @Value("${spring.ai.vectorstore.redis.index}")
    private String redisIndexName;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String redisPrefix;

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean("pdfVectorStore")
    @Primary
    public VectorStore pdfVectorStore(JedisConnectionFactory jedisConnectionFactory, EmbeddingModel embeddingModel) {
        JedisPooled jedisPooled = buildJedisPooled(jedisConnectionFactory);
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(redisIndexName)
                .prefix(redisPrefix)
                .initializeSchema(true)
                .metadataFields(
                        RedisVectorStore.MetadataField.numeric("user_id"),
                        RedisVectorStore.MetadataField.numeric("session_id"),
                        RedisVectorStore.MetadataField.numeric("file_id"),
                        RedisVectorStore.MetadataField.tag("file_kind")
                )
                .build();
    }

    private JedisPooled buildJedisPooled(JedisConnectionFactory jedisConnectionFactory) {
        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .ssl(jedisConnectionFactory.isUseSsl())
                .clientName(jedisConnectionFactory.getClientName())
                .timeoutMillis(jedisConnectionFactory.getTimeout())
                .password(jedisConnectionFactory.getPassword())
                .build();

        return new JedisPooled(
                new HostAndPort(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort()),
                clientConfig
        );
    }


    @Bean
    public ChatClient chatClient(AnthropicChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.CHAT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Bean
    public ChatClient pdfChatClient(AnthropicChatModel model, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.PDF_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.5)
                                        .topK(2)
                                        .build())
                                .build()
                )
                .build();
    }
}
