package it.polimi.middleware.api.dto.requests;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompressionRequest {
	
	@NotNull
	private Float compressionRatio;
	
	@NotNull
	private MultipartFile image;
	
	@NotEmpty
	@Pattern(regexp="(\\([a-zA-Z0-9]+))+.(jpg|png")
	private String directory;

}
