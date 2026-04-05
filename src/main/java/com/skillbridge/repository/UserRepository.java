// src/main/java/com/skillbridge/repository/UserRepository.java
package com.skillbridge.repository;

import com.skillbridge.model.User;
import com.skillbridge.model.enums.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}