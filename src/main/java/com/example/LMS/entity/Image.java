package com.example.LMS.entity;

import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type",nullable = false, length = 20)
    ObjectType objectType;

    @Column(nullable = false)
    Long objectId;

    @Column(nullable = false, length = 255)
    String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ImageType type = ImageType.IMAGE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status = Status.ACTIVE;

}

