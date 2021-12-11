package it.polimi.middleware.api.services;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import it.polimi.middleware.api.dto.requests.CompressionRequest;
import it.polimi.middleware.api.dto.responses.TaskResponse;
import it.polimi.middleware.api.dto.responses.TaskResponse.TaskResponseBuilder;
import it.polimi.middleware.api.messages.AbstractTaskMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TaskService {
	
	private final KafkaTemplate<String, AbstractTaskMessage> kafkaTemplate;
	
	public TaskResponse compressImage(CompressionRequest request) {
		String taskId =  generateId();
		
		
		TaskResponseBuilder builder = TaskResponse.builder()
				.id(generateId());
		//TODO:kafka stuff
		return builder.build();
	}
	
	private String generateId() {
		return UUID.randomUUID().toString();
	}

}
