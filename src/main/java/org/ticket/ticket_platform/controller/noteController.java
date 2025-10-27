package org.ticket.ticket_platform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ticket.ticket_platform.model.Note;
import org.ticket.ticket_platform.model.User;
import org.ticket.ticket_platform.repository.NoteRepository;
import org.ticket.ticket_platform.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired 
    private UserRepository userRepository;

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("note") Note note, BindingResult bindingResult, Authentication auth) {
        if(bindingResult.hasErrors()){
            return "notes/create";
        }

        User currentUser = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        note.setAuthor(currentUser);

        noteRepository.save(note);

        return "redirect:/tickets/show/" + note.getTicket().getId();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Note note = noteRepository.findById(id).get();
        model.addAttribute("editMode", true);
        model.addAttribute("note", note);

        return"/notes/edit";
    }

    @PostMapping("/edit")
    public String editNote(@RequestParam Integer id, @RequestParam String details) {
        Optional<Note> optionalNote = noteRepository.findById(id);
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            note.setDetails(details); 
            noteRepository.save(note);
            return "redirect:/tickets/show/" + note.getTicket().getId();
        }
        return "redirect:/tickets"; // se non trova la nota
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable("id") Integer id) {
        Optional<Note> optionalNote = noteRepository.findById(id);
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            Integer ticketId = note.getTicket().getId();
            noteRepository.delete(note);
            return "redirect:/tickets/show/" + ticketId;
        }
        return "redirect:/tickets";
    }
}

