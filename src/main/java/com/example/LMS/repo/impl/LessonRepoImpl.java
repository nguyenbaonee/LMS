package com.example.LMS.repo.impl;

import com.example.LMS.dto.Request.LessonQuery;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import com.example.LMS.enums.Status;
import com.example.LMS.repo.extend.LessonRepoExtend;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonRepoImpl implements LessonRepoExtend {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LessonDTO> search(LessonQuery lessonQuery, Pageable pageable) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select new com.example.LMS.dto.dtoProjection.LessonDTO(e.id,e.title, e.lessonOrder, e.status) from Lesson e "
                + createWhereQuery(lessonQuery.getKeyword(), lessonQuery.getCourseId(), lessonQuery.getStatus(), values);
        TypedQuery<LessonDTO> query = entityManager.createQuery(sql, LessonDTO.class);
        values.forEach(query::setParameter);
        query.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    @Override
    public Long count(LessonQuery lessonQuery) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(e) from Lesson e "
                + createWhereQuery(lessonQuery.getKeyword(), lessonQuery.getCourseId(), lessonQuery.getStatus(), values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(String keyword, Long courseId, Status status, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        sql.append(" where 1 = 1");
        if (StringUtils.hasText(keyword)) {
            sql.append(" and (lower(e.title) like :keyword)");
            values.put("keyword", encodeKeyword(keyword));
        }

        if (status != null) {
            sql.append(" and e.status = :status");
            values.put("status", status);
        }

        if (courseId != null) {
            sql.append(" and e.course.id = :courseId");
            values.put("courseId", courseId);
        }

        return sql.toString();
    }

    private String encodeKeyword(String keyword) {
        if (keyword == null) {
            return "%";
        }

        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
