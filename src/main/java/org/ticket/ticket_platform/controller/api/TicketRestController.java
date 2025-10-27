package org.ticket.ticket_platform.controller.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ticket.ticket_platform.model.GenericPayload;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.model.TicketStatus;
import org.ticket.ticket_platform.repository.TicketRepository;

@RestController
@CrossOrigin
@RequestMapping("/api/tickets")
public class TicketRestController {

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<GenericPayload<List<Ticket>>> ticketList(
            @RequestParam(name="keyword", required=false) String keyword) {

        List<Ticket> tickets;
        if(keyword != null && keyword.isBlank()) {
            return ResponseEntity.badRequest().body(new GenericPayload<>(null, "Keyword vuota", "BAD_REQUEST"));
        } else if(keyword != null && !keyword.isBlank()) {
            tickets = ticketRepository.findByTitleContainingIgnoreCase(keyword);
        } else {
            tickets = ticketRepository.findAll();
        }

        return ResponseEntity.ok(new GenericPayload<>(tickets, "", "OK"));
    }

    @GetMapping("{id}")
    public ResponseEntity<GenericPayload<Ticket>> get(@PathVariable Integer id) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if(optTicket.isPresent()) {
            return ResponseEntity.ok(new GenericPayload<>(optTicket.get(), "", "OK"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenericPayload<>(null, "Ticket non trovato con id " + id, "NOT_FOUND"));
        }
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<GenericPayload<Ticket>> create(@RequestBody Ticket ticket) {
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericPayload<>(saved, "Ticket creato", "CREATED"));
    }

    @PutMapping("{id}")
    public ResponseEntity<GenericPayload<Ticket>> update(@PathVariable Integer id, @RequestBody Ticket ticket) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if(optTicket.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenericPayload<>(null, "Ticket non trovato", "NOT_FOUND"));
        }

        Ticket existing = optTicket.get();
        existing.setTitle(ticket.getTitle());
        existing.setDescription(ticket.getDescription());
        existing.setStatus(ticket.getStatus());
        existing.setOperator(ticket.getOperator());
        existing.setCategories(ticket.getCategories());

        Ticket saved = ticketRepository.save(existing);
        return ResponseEntity.ok(new GenericPayload<>(saved, "Ticket aggiornato", "OK"));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<GenericPayload<Void>> delete(@PathVariable Integer id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenericPayload<>(null, "Ticket non trovato", "NOT_FOUND"));
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.ok(new GenericPayload<>(null, "Ticket eliminato", "OK"));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<GenericPayload<List<Ticket>>> ticketsByCategory(@PathVariable Integer categoryId) {
        List<Ticket> tickets = ticketRepository.findByCategories_Id(categoryId);
        return ResponseEntity.ok(new GenericPayload<>(tickets, "", "OK"));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@RequestParam String status) {
        TicketStatus ticketStatus;
        try {
            ticketStatus = TicketStatus.valueOf(status); // converte la stringa in enum
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // status non valido
        }

        List<Ticket> tickets = ticketRepository.findByStatus(ticketStatus);
        return ResponseEntity.ok(tickets);
    }
}
