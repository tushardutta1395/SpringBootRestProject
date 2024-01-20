package org.studyeasy.SpringRestDemo.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDTO {

    @Size(min = 6, max = 20)
    @Schema(description = "Password", example = "Password", requiredMode = RequiredMode.REQUIRED, minLength = 6, maxLength = 20)
    private String password;
}
