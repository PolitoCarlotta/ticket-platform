package org.ticket.ticket_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ticket.ticket_platform.model.Note;
import org.ticket.ticket_platform.repository.NoteRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("note") Note note, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "notes/edit";
        }

        noteRepository.save(note);

        return "redirect:/tickets/show" + note.getTicket().getId();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Note note = noteRepository.findById(id).get();
        model.addAttribute("editMode", true);
        model.addAttribute("note", note);

        return"/notes/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@Valid @ModelAttribute("note") Note note, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("editMode", true);
            return "/notes/edit";
        }

        noteRepository.save(note);

        return"redirect:/tickets/show" + note.getTicket().getId();
    }
}
