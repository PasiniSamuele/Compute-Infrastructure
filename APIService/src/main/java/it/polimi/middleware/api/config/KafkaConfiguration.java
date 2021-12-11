package it.polimi.middleware.api.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;

import it.polimi.middleware.api.messages.AbstractTaskMessage;

@Configuration
public class KafkaConfiguration {
	
	@Autowired
	private Environment env;
	
	@Bean
	public ProducerFactory<String, AbstractTaskMessage> producerFactory() {
		Map<String, Object> config = new HashMap<String, Object>();
		
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("kafka.address")+":"+ env.getProperty("kafka.port"));
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);

		return new DefaultKafkaProducerFactory<String, AbstractTaskMessage>(config);
	}
	
	@Bean
	public KafkaTemplate<String, AbstractTaskMessage> kafkaTemplate(){
		return new KafkaTemplate<String, AbstractTaskMessage>(producerFactory());
	}

}
