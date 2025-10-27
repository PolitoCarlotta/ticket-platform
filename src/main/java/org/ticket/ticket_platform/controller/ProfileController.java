package org.ticket.ticket_platform.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.ticket.ticket_platform.model.Role;
import org.ticket.ticket_platform.model.TicketStatus;
import org.ticket.ticket_platform.model.User;
import org.ticket.ticket_platform.repository.RoleRepository;
import org.ticket.ticket_platform.repository.UserRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired RoleRepository roleRepository;

    @GetMapping
    public String profile(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            boolean isOperator = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("OPERATOR"));
            model.addAttribute("isOperator", isOperator);

            return "profile/index";
        } else {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            boolean isOperator = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("OPERATOR"));
            model.addAttribute("isOperator", isOperator);

            if (isOperator) {
                boolean canSetInactive = user.getTickets().stream()
                    .noneMatch(t -> t.getStatus() == TicketStatus.TO_DO || t.getStatus() == TicketStatus.IN_PROGRESS);
                model.addAttribute("canSetInactive", canSetInactive);
            }

            return "profile/edit"; 
        } else {
            return "redirect:/login?error";
        }
    }

    @PostMapping("/edit")
    public String saveProfile(@ModelAttribute("user") User userForm, RedirectAttributes redirectAttributes, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();

            currentUser.setName(userForm.getName());

            boolean isOperator = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("OPERATOR"));

        if (isOperator) {
            boolean canSetInactive = currentUser.getTickets().stream()
                .noneMatch(t -> t.getStatus() == TicketStatus.TO_DO || t.getStatus() == TicketStatus.IN_PROGRESS);

            if (userForm.isFlag() == false && !canSetInactive) {
            } else {
                currentUser.setFlag(userForm.isFlag());
            }
        } else {
            currentUser.setFlag(true);
        }

            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profilo aggiornato correttamente!");
            return "redirect:/profile";
        }

        redirectAttributes.addFlashAttribute("error", "Errore aggiornamento profilo");
        return "redirect:/profile";
    }

    @GetMapping("/create-operator")
    public String showCreateOperatorForm(Model model) {
        model.addAttribute("user", new User());
        return "profile/create-operator"; 
    }

@PostMapping("/create-operator")
public String createOperator(@ModelAttribute("user") User userForm,
                             RedirectAttributes redirectAttributes) {

    if (userRepository.findByEmail(userForm.getEmail()).isPresent()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Email già in uso!");
        return "redirect:/profile/create-operator";
    }

    userForm.setFlag(true);

    userForm.setPassword("{noop}" + userForm.getPassword());

    Role operatorRole = roleRepository.findByName("OPERATOR")
            .orElseThrow(() -> new RuntimeException("Ruolo OPERATOR non trovato"));
    userForm.setRoles(List.of(operatorRole));

    userRepository.save(userForm);

    redirectAttributes.addFlashAttribute("successMessage", "Operatore creato correttamente!");

    return "redirect:/profile";
}
}

