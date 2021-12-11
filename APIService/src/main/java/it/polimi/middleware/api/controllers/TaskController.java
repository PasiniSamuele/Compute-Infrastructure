package it.polimi.middleware.api.controllers;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import it.polimi.middleware.api.dto.requests.CompressionRequest;
import it.polimi.middleware.api.dto.responses.TaskResponse;
import it.polimi.middleware.api.services.TaskService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class TaskController {
	
	private final TaskService taskService;
	
	@PostMapping(value = "/task/compressImage", consumes = { "multipart/form-data" })
	public ResponseEntity<TaskResponse> compressImage(@Valid @ModelAttribute CompressionRequest compressionRequest){
		TaskResponse task = taskService.compressImage(compressionRequest);
		return ResponseEntity.ok()
				.body(task);
	}
}
