package com.example.javachat.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndEnabledTrue(Long id);

    boolean existsByUsername(String username);

    long countByEnabledTrue();

    @Query("""
            select user
            from User user
            where user.enabled = true
              and user.id <> :excludedUserId
              and (
                    lower(user.username) like lower(concat('%', :keyword, '%'))
                 or lower(user.nickname) like lower(concat('%', :keyword, '%'))
              )
            order by user.username asc
            """)
    Page<User> searchEnabledUsers(
            @Param("keyword") String keyword,
            @Param("excludedUserId") Long excludedUserId,
            Pageable pageable
    );
}
