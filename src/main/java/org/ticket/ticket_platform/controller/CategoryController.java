package org.ticket.ticket_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ticket.ticket_platform.model.Category;
import org.ticket.ticket_platform.model.Ticket;
import org.ticket.ticket_platform.repository.CategoryRepository;

import jakarta.validation.Valid;

@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping()
    public String index(Model model) {
        model.addAttribute("list", categoryRepository.findAll());
        model.addAttribute("categoryObj", new Category());

        return "categories/index";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("categoryObj")Category category, BindingResult bindingResult, Model model) {
        Category cate = categoryRepository.findByCategory(category.getCategory());

        if(cate==null) {

        } else {
            bindingResult.addError(new ObjectError("category", "Category already present"));
        }

        categoryRepository.save(category);

        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model) {
        Category cate = categoryRepository.findById(id).get();

        for(Ticket ticket : cate.getTickets()) {
            ticket.getCategories().remove(cate);
        }

        categoryRepository.deleteById(id);

        return "redirect:/categories";
    }

}
