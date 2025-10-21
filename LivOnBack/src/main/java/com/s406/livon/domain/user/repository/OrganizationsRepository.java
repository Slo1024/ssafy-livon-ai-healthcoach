package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationsRepository extends JpaRepository<Organizations, Long> {

}