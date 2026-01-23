package com.notFoundTomAndJerry.notFoundJerry.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;
  @Value("${spring.data.redis.port}")
  private int port;

  // 1. Redis Connection Factory 설정 (Lettuce 사용)
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  // 2. RedisTemplate 설정 (직렬화 방식 변경)
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Key Serializer: Key는 String으로 저장 (예: "chat:room:1")
    template.setKeySerializer(new StringRedisSerializer());

    // Value Serializer: Value는 JSON으로 저장 (DTO 객체 등)
    // GenericJackson2JsonRedisSerializer를 사용하면 Class Type 정보(@class)도 함께 저장되어
    // 나중에 데이터를 꺼낼 때 DTO로 매핑하기 쉽습니다.
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

    // Hash Key/Value Serializer (필요 시 설정)
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    return template;
  }
}
