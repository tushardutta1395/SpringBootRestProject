package org.studyeasy.SpringRestDemo.payload.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountViewDTO {
    private Long id;
    private String email;
    private String authorities;
}
