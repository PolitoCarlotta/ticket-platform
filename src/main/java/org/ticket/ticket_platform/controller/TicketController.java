package org.ticket.ticket_platform.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.ticket.ticket_platform.model.Note;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.repository.CategoryRepository;
import org.ticket.ticket_platform.repository.NoteRepository;
import org.ticket.ticket_platform.repository.TicketRepository;

import jakarta.validation.Valid;



@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired 
    private TicketRepository ticketRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String index(Authentication auth, Model model,@RequestParam(name="keyword", required=false) String keyword) {
        List<Ticket> result= null;
        if(keyword == null || keyword.isBlank()) {
            result = ticketRepository.findAll();
        } else {
            result = ticketRepository.findByTitleContainingIgnoreCase(keyword);
        }

        model.addAttribute("list", result);
        model.addAttribute("username", auth.getName());

        return "tickets/index";
    }

    @GetMapping("/show/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if(optTicket.isPresent()){
            model.addAttribute("ticket", optTicket.get());
            model.addAttribute("empty", false);
        } else {
            model.addAttribute("empty", true);
        }
        return "tickets/show";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("category<list", categoryRepository.findAll());
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("editMode", false);
        return "/tickets/create";
    }
    
    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("Ticket") Ticket formTicket, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        Optional<Ticket> optTicket = ticketRepository.findByTitle(formTicket.getTitle());
        if(optTicket.isPresent()) {
            bindingResult.addError(new ObjectError("title", "The title already exist"));
        }

        if(bindingResult.hasErrors()) {
            model.addAttribute("categoryList", categoryRepository.findAll());
            return "redirect:/tickets";
        }

        ticketRepository.save(formTicket);
        redirectAttributes.addFlashAttribute("successMessage", "Ticket created");

        return "redirect:/tickets";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        Ticket ticket = optTicket.get();
        model.addAttribute("editMode", true);
        model.addAttribute("categoryList", categoryRepository.findAll());
        model.addAttribute("ticket", ticket);
        return "/tickets/create";
    }

    @PostMapping("/edit/{id}")
    public String update(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, Model model) {
        Ticket oldTicket = ticketRepository.findById(formTicket.getId()).get();

        if(!oldTicket.getTitle().equals(formTicket.getTitle())) {
            bindingResult.addError(new ObjectError("title", "Cannot change the title"));
        }

        if(!oldTicket.getCreationDate().equals(formTicket.getCreationDate())) {
            bindingResult.addError(new ObjectError("creationDate", "The date of creation cannot change"));
        }

        if(bindingResult.hasErrors()) {
            model.addAttribute("categoryList", categoryRepository.findAll());
            return "/ticket/create";
        }
        
        ticketRepository.save(formTicket);

        return "redirect:/tickets";
    }

    @PostMapping("/delete/{id}")
    public String delete (@PathVariable("id") Integer id) {
        Ticket ticket = ticketRepository.findById(id).get();
        for(Note noteToDelete : ticket.getNotes()) {
            noteRepository.delete(noteToDelete);
        }

        ticketRepository.deleteById(id);

        return "redirect:/tickets";
    }

    @GetMapping("/{id}/note")
    public String note (@PathVariable("id") Integer id, Model model) {
        Note note = new Note();
        note.setTicket(ticketRepository.findById(id).get());

        model.addAttribute("note", note);
        model.addAttribute("editMode", false);

        return"/notes/edit";
    }
    
    
    
    
}
