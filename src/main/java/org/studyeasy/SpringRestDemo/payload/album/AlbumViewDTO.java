package org.studyeasy.SpringRestDemo.payload.album;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AlbumViewDTO {

    private Long id;

    @NotBlank
    @Schema(description = "Album name", example = "Travel", requiredMode = RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Description of the album", example = "Description", requiredMode = RequiredMode.REQUIRED)
    private String description;

    private List<PhotoDTO> photos;
}
