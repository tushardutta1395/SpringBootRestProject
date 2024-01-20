package org.studyeasy.SpringRestDemo.payload.album;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDTO {

    private Long id;
    private String name;
    private String description;
    private String fileName;
    private String download_link;
}
