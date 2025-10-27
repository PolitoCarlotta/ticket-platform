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
import org.ticket.ticket_platform.model.Category;
import org.ticket.ticket_platform.model.Note;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.model.User;
import org.ticket.ticket_platform.repository.CategoryRepository;
import org.ticket.ticket_platform.repository.NoteRepository;
import org.ticket.ticket_platform.repository.TicketRepository;
import org.ticket.ticket_platform.repository.UserRepository;

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

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String index(Authentication auth, Model model,@RequestParam(name="keyword", required=false) String keyword) {
        List<Ticket> result;
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                          .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (isAdmin) {
        result = (keyword == null || keyword.isBlank()) ?
                ticketRepository.findAll() :
                ticketRepository.findByTitleContainingIgnoreCase(keyword);
    } else {
        result = (keyword == null || keyword.isBlank()) ?
                ticketRepository.findByOperatorEmail(email) :
                ticketRepository.findByOperatorEmailAndTitleContainingIgnoreCase(email, keyword);
    }

    model.addAttribute("list", result);
    model.addAttribute("username", email);
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
        model.addAttribute("categoryList", categoryRepository.findAll());
        List<User> operatorList = userRepository.findByRoles_NameAndFlag("OPERATOR", true);
        model.addAttribute("operatorList", operatorList);
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("editMode", false);
        return "/tickets/create";
    }
    
    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        Optional<Ticket> optTicket = ticketRepository.findByTitle(formTicket.getTitle());
        if(optTicket.isPresent()) {
            bindingResult.addError(new ObjectError("title", "The title already exist"));
        }

        if (formTicket.getCategories() != null) {
        List<Category> categories = categoryRepository.findAllById(
            formTicket.getCategories().stream().map(Category::getId).toList()
        );
        formTicket.setCategories(categories);
    }


        if(bindingResult.hasErrors()) {
            model.addAttribute("categoryList", categoryRepository.findAll());
            return "/tickets/create";
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
            return "/tickets/create";
        }
        
        ticketRepository.save(formTicket);

        return "redirect:/tickets";
    }

    @PostMapping("/delete/{id}")
    public String delete (@PathVariable("id") Integer id) {
        Optional<Ticket> optTicket = ticketRepository.findById(id);

        if(optTicket.isPresent()){
            Ticket ticket = optTicket.get();

            for(Note noteToDelete : ticket.getNotes()) {
            noteRepository.delete(noteToDelete);
            }

        ticketRepository.deleteById(id);
        }


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
