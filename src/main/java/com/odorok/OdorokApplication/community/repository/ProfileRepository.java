package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.draftDomain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile,Long>, ProfileRepositoryCustom {
    Optional<Profile> findByUserId(Long userId);
}
