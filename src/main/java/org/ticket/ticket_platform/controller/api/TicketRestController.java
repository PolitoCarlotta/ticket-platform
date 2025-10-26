package org.ticket.ticket_platform.controller.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ticket.ticket_platform.model.GenericPayload;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.repository.TicketRepository;

@RestController
@CrossOrigin
@RequestMapping("/api/tickets")
public class TicketRestController {

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<List<Ticket>> ticketList(@RequestParam (name="keyword", required=false) String keyword) {
        List<Ticket> result = null;
        if(keyword != null && keyword.isBlank()) {
            return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
        } else if (keyword != null && !keyword.isBlank()) {
            result = ticketRepository.findByTitleContainingIgnoreCase(keyword);
        } else {
            result = ticketRepository.findAll();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}")
    public ResponseEntity<GenericPayload<Ticket>> get (@PathVariable("id") Integer id) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);

        if(optTicket.isPresent()) {
            GenericPayload<Ticket> result = new GenericPayload<Ticket>(optTicket.get(), "", HttpStatus.OK.getReasonPhrase());

            return new ResponseEntity<GenericPayload<Ticket>>(result, HttpStatus.OK);
        } else {
            GenericPayload<Ticket> result = new GenericPayload<Ticket>(null, "The ticket with " + id + "id doesn't exist", HttpStatus.BAD_REQUEST.getReasonPhrase());

            return new ResponseEntity<GenericPayload<Ticket>>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@RequestBody Ticket ticket) {
        return new ResponseEntity<>(ticketRepository.save(ticket), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<Ticket> put(@PathVariable("id") Integer id, @RequestBody Ticket ticket) {

        return new ResponseEntity<>(ticketRepository.save(ticket), HttpStatus.CREATED);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Ticket> delete(@PathVariable("id") Integer id) {
        ticketRepository.deleteById(id);
        return new ResponseEntity<>( HttpStatus.OK);
    }


}
