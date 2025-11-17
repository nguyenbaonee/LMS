package com.example.LMS.mapper;

import com.example.LMS.dto.Request.ImageRequest;
import com.example.LMS.dto.Response.ImageResponse;
import com.example.LMS.entity.Image;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    Image toImage(ImageRequest imageRequest);
    ImageResponse toImageResponse(Image image);

    List<Image> toImages(List<ImageRequest> imageRequests);
    List<ImageResponse> toImageResponses(List<Image> images);
}
