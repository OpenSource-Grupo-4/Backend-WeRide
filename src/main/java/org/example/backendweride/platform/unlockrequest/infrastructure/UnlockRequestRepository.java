package org.example.backendweride.platform.unlockrequest.infrastructure;

import org.example.backendweride.platform.unlockrequest.domain.model.UnlockRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnlockRequestRepository extends JpaRepository<UnlockRequest, Long> {
    Optional<UnlockRequest> findByPublicId(String publicId);
    List<UnlockRequest> findAllByUserId(String userId);
    List<UnlockRequest> findAllByUserIdAndBookingId(String userId, Long bookingId);
}
