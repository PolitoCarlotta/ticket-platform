package org.ticket.ticket_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ticket.ticket_platform.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer>{

    Optional<Role> findByName(String name);

}
