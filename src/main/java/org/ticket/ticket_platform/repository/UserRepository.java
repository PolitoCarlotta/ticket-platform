package org.ticket.ticket_platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ticket.ticket_platform.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    public Optional<User> findByEmail(String email);

    public List<User> findByRoles_NameAndFlag(String roleName, boolean flag);

}
