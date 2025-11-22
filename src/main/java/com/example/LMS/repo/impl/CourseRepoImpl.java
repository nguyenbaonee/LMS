package com.example.LMS.repo.impl;

import com.example.LMS.dto.Request.CourseQuery;
import com.example.LMS.dto.dtoProjection.CourseDTO;
import com.example.LMS.enums.Status;
import com.example.LMS.repo.extend.CourseRepoExtend;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseRepoImpl implements CourseRepoExtend {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CourseDTO> search(CourseQuery courseQuery, Pageable pageable) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select new com.example.LMS.dto.dtoProjection.CourseDTO(e.id,e.name, e.code, e.description, e.status) from Course e "
                + createWhereQuery(courseQuery.getKeyword(), courseQuery.getStatus(), values);
        TypedQuery<CourseDTO> query = entityManager.createQuery(sql, CourseDTO.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    private String createWhereQuery(String keyword, Status status, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        sql.append(" where 1 = 1");

        if (StringUtils.hasText(keyword)) {
            sql.append(" and (lower(e.name) like :keyword" +
                    " or lower(e.code) like :keyword" +
                    " or lower(e.description) like :keyword)");
            values.put("keyword", encodeKeyword(keyword));
        }

        if (status != null) {
            sql.append(" and e.status = :status");
            values.put("status", status);
        }

        return sql.toString();
    }

    private String encodeKeyword(String keyword) {
        if (keyword == null) {
            return "%";
        }

        return "%" + keyword.trim().toLowerCase() + "%";
    }

    @Override
    public Long count(CourseQuery courseQuery) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(e) from Course e "
                + createWhereQuery(courseQuery.getKeyword(), courseQuery.getStatus(), values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }
}
