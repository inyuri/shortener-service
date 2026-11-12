package com.timofey.shortener_service.repository;

import com.timofey.shortener_service.model.Link;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> getLinkByShortCode(String shortCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Link> findByShortCode(String shortCode);
}
