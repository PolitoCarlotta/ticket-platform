package org.ticket.ticket_platform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ticket.ticket_platform.model.Category;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.repository.CategoryRepository;
import org.ticket.ticket_platform.repository.TicketRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

 
    @GetMapping
    public String index(Model model) {
        model.addAttribute("list", categoryRepository.findAll());
        model.addAttribute("categoryObj", new Category());
        return "categories/index";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("categoryObj") Category category,
                         BindingResult bindingResult, Model model) {

        Category existing = categoryRepository.findByCategory(category.getCategory());

        if (existing != null) {
            bindingResult.addError(new FieldError("categoryObj","category", "Categoria già esistente"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("list", categoryRepository.findAll());
            model.addAttribute("categoryObj", category);
            return "categories/index";
        }

        categoryRepository.save(category);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model) {

        Optional<Category> optCategory = categoryRepository.findById(id);

        if (optCategory.isPresent()) {
            Category cate = optCategory.get();

            for (Ticket ticket : cate.getTickets()) {
                ticket.getCategories().remove(cate);
                ticketRepository.save(ticket); 
            }

            categoryRepository.delete(cate);
        }

        return "redirect:/categories";
    }
}
