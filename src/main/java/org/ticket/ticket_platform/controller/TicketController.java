package org.ticket.ticket_platform.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.ticket.ticket_platform.model.Category;
import org.ticket.ticket_platform.model.Note;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.model.TicketStatus;
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
    public String index(Authentication auth, Model model,
                        @RequestParam(name = "keyword", required = false) String keyword) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        List<Ticket> result = (isAdmin)
                ? ((keyword == null || keyword.isBlank())
                    ? ticketRepository.findAll()
                    : ticketRepository.findByTitleContainingIgnoreCase(keyword))
                : ((keyword == null || keyword.isBlank())
                    ? ticketRepository.findByOperatorEmail(email)
                    : ticketRepository.findByOperatorEmailAndTitleContainingIgnoreCase(email, keyword));

        model.addAttribute("list", result);
        model.addAttribute("username", email);
        return "tickets/index";
    }


    @GetMapping("/show/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        Note note = new Note();
        note.setTicket(ticket);
        model.addAttribute("ticket", ticket);
        model.addAttribute("note", note);
        return "tickets/show";
    }

    @PostMapping("/edit/{id}/status")
    @PreAuthorize("hasAuthority('OPERATOR')")
    public String updateTicketStatus(@PathVariable Integer id,
                                    @RequestParam("status") String status) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        ticket.setStatus(TicketStatus.valueOf(status));
        ticketRepository.save(ticket);
        return "redirect:/tickets/show/" + id;
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("editMode", false);
        model.addAttribute("categoryList", categoryRepository.findAll());
        model.addAttribute("operatorList", userRepository.findByRoles_NameAndFlag("OPERATOR", true));
        return "tickets/create";
    }

    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("ticket") Ticket formTicket,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {


        if (ticketRepository.findByTitle(formTicket.getTitle()).isPresent()) {
            bindingResult.rejectValue("title", "error.ticket", "Esiste già un ticket con questo titolo");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);
            model.addAttribute("categoryList", categoryRepository.findAll());
            model.addAttribute("operatorList", userRepository.findByRoles_NameAndFlag("OPERATOR", true));
            return "tickets/create";
        }

        // Imposta operator e categorie
        User operator = userRepository.findById(formTicket.getOperator().getId())
                .orElseThrow(() -> new IllegalArgumentException("Operatore non trovato"));
        formTicket.setOperator(operator);

        if (formTicket.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(
                    formTicket.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
            formTicket.setCategories(categories);
        }

        ticketRepository.save(formTicket);
        redirectAttributes.addFlashAttribute("successMessage", "Ticket creato con successo!");
        return "redirect:/tickets";
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));

        model.addAttribute("ticket", ticket);
        model.addAttribute("editMode", true);
        model.addAttribute("categoryList", categoryRepository.findAll());
        model.addAttribute("operatorList", userRepository.findByRoles_NameAndFlag("OPERATOR", true));
        return "tickets/create";
    }


    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Valid @ModelAttribute("ticket") Ticket formTicket,
                         BindingResult bindingResult,
                         Model model) {

        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));

        // Il titolo non può essere modificato
        if (!existing.getTitle().equals(formTicket.getTitle())) {
            bindingResult.rejectValue("title", "error.ticket", "Il titolo non può essere modificato");
        }

        // Se ci sono errori, torna alla form
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("categoryList", categoryRepository.findAll());
            model.addAttribute("operatorList", userRepository.findByRoles_NameAndFlag("OPERATOR", true));
            return "tickets/create";
        }

        // Mantiene titolo e data creazione
        formTicket.setTitle(existing.getTitle());
        formTicket.setCreationDate(existing.getCreationDate());

        // Imposta operator e categorie
        User operator = userRepository.findById(formTicket.getOperator().getId())
                .orElseThrow(() -> new IllegalArgumentException("Operatore non trovato"));
        formTicket.setOperator(operator);

        if (formTicket.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(
                    formTicket.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
            formTicket.setCategories(categories);
        }

        ticketRepository.save(formTicket);
        return "redirect:/tickets";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        ticketRepository.findById(id).ifPresent(ticket -> {
            noteRepository.deleteAll(ticket.getNotes());
            ticketRepository.delete(ticket);
        });
        return "redirect:/tickets";
    }

    @GetMapping("/{id}/note")
    public String note(@PathVariable("id") Integer id, Model model) {
        Note note = new Note();
        note.setTicket(ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket non trovato")));
        model.addAttribute("note", note);
        model.addAttribute("editMode", false);
        return "/notes/edit";
    }
}
