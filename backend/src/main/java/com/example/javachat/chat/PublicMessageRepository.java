package com.example.javachat.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicMessageRepository extends JpaRepository<PublicMessage, Long> {

    Page<PublicMessage> findAllByOrderByCreatedAtAsc(Pageable pageable);
}
