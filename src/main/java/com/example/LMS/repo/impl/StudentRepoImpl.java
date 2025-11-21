package com.example.LMS.repo.impl;

import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.enums.Status;
import com.example.LMS.repo.extend.StudentRepoExtend;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentRepoImpl implements StudentRepoExtend {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StudentDTO> search(StudentQuery studentQuery, Pageable pageable) {
        Map<String, Object> values = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select new com.example.LMS.dto.dtoProjection.StudentDTO(e.id,e.email, e.name, e.status) from Student e");
        if (studentQuery.getCourseId() != null) {
            sql.append(" join Enrollment er on e.id = er.student.id ");
        }
        sql.append(createWhereQuery(studentQuery.getKeyword(), studentQuery.getStatus(), values));
        sql.append(createOrderQuery(studentQuery.getSortBy()));
        TypedQuery<StudentDTO> query = entityManager.createQuery(sql.toString(), StudentDTO.class);
        values.forEach(query::setParameter);

        if (pageable != null) {
            query.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(StudentQuery studentQuery) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(e) from Student e "
                + createWhereQuery(studentQuery.getKeyword(), studentQuery.getStatus(), values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createOrderQuery(String sortBy) {
        StringBuilder sql = new StringBuilder();
        if (StringUtils.hasLength(sortBy)) {
            sql.append(" order by e.").append(sortBy.replace(".", " "));
        }
        return sql.toString();
    }

    private String createWhereQuery(String keyword, Status status, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        sql.append(" where 1 = 1");
        if (StringUtils.hasText(keyword)) {
            sql.append(" and ( lower(e.name) like :keyword" +
                    " or lower(e.email) like :keyword )");
            values.put("keyword", encodeKeyword(keyword));
        }
        
        if (status != null) {
            sql.append(" and e.status = :status");
            values.put("status", status);
        }
        
        return sql.toString();
    }

    public String encodeKeyword(String keyword) {
        if (keyword == null) {
            return "%";
        }

        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
