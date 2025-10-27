package org.ticket.ticket_platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.model.TicketStatus;


public interface TicketRepository extends JpaRepository<Ticket, Integer>{

    public Optional<Ticket> findByTitle(String title);

    public List<Ticket> findByTitleContainingIgnoreCase(String title);

    public List<Ticket> findByStatus(TicketStatus status);

    public List<Ticket> findByOperatorEmail(String email);

    public List<Ticket> findByOperatorEmailAndTitleContainingIgnoreCase(String email, String keyword);


}
