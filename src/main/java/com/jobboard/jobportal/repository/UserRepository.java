// src/main/java/com/jobboard/jobportal/repository/UserRepository.java
package com.jobboard.jobportal.repository;

import com.jobboard.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}