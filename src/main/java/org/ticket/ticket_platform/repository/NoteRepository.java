package org.ticket.ticket_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ticket.ticket_platform.model.Note;

public interface NoteRepository extends JpaRepository<Note, Integer>{

    

}
