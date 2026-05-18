package com.aem.tiretrack.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.aem.tiretrack.model.User;

public interface  UserRepository extends JpaRepository<User, Long> 
{
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
}
