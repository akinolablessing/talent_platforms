package org.ayomide.repository;

import org.ayomide.model.TalentProfile;
import org.ayomide.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TalentProfileRepository extends JpaRepository<TalentProfile, Long> {
    Optional<TalentProfile> findByUser(User user);
}
