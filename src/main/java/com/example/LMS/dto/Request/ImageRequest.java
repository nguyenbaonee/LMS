package com.example.LMS.dto.Request;

import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest {
    ObjectType objectType;

    Long objectId;

    String url;

    ImageType type;
}
