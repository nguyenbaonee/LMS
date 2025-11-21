package com.example.LMS.entity;

import com.example.LMS.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(
        name = "lessons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_course_lesson_order",
                        columnNames = {"course_id", "lessonOrder"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    Course course;

    @Column(nullable = false, length = 200, unique = true)
    String title;

    @OneToMany
//    @JoinColumn(name = "objectId", referencedColumnName = "id", insertable = false, updatable = false)
    @Where(clause = "object_type='LESSON' AND type='VIDEO' ")
    List<Image> videoUrl;

    @OneToMany
//    @JoinColumn(name = "objectId", referencedColumnName = "id", insertable = false, updatable = false)
    @Where(clause = "object_type='LESSON' AND type='THUMBNAIL' ")
    List<Image> thumbnail;

    @Column(nullable = false)
    Integer lessonOrder;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status = Status.ACTIVE;

}
