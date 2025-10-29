package org.ticket.ticket_platform.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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
                        .noneMatch(t -> t.getStatus() == TicketStatus.TO_DO
                                || t.getStatus() == TicketStatus.IN_PROGRESS);
                model.addAttribute("canSetInactive", canSetInactive);
            }

            return "profile/edit";
        } else {
            return "redirect:/login?error";
        }
    }

    @PostMapping("/edit")
    public String saveProfile(@Valid @ModelAttribute User userForm, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes, Principal principal) {

    String email = principal.getName();
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
        return "redirect:/login?error";
    }

    User currentUser = userOpt.get();

        boolean isOperator = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("OPERATOR"));
    model.addAttribute("isOperator", isOperator);

        if (!currentUser.getEmail().equals(userForm.getEmail())) {
            bindingResult.addError(new FieldError("userForm", "email", "La mail non può essere modificata"));
        }

        if (isOperator) {
            boolean canSetInactive = currentUser.getTickets().stream()
                    .noneMatch(t -> t.getStatus() == TicketStatus.TO_DO || t.getStatus() == TicketStatus.IN_PROGRESS);

            if (!userForm.isFlag() && !canSetInactive) {
                bindingResult.addError(
                        new FieldError("userForm", "flag", "Devi prima completare i ticket per passare inattivo"));
            }
        }

            if (bindingResult.hasErrors()) {
                model.addAttribute("isOperator", isOperator);

                    if(isOperator){
                        model.addAttribute("user", userForm);
                        boolean canSetInactive= currentUser.getTickets().stream().noneMatch(t -> t.getStatus() == TicketStatus.TO_DO || t.getStatus() == TicketStatus.IN_PROGRESS);
                        model.addAttribute("canSetInactive", canSetInactive);
                        model.addAttribute("isOperator", isOperator);
                    }
                       System.out.println("❌ Errori nel form:");
    bindingResult.getAllErrors().forEach(e -> System.out.println(" - " + e));
                return "profile/edit";
            }

            currentUser.setName(userForm.getName());

            if(isOperator) {
                currentUser.setFlag(userForm.isFlag());
            }

            userRepository.save(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato correttamente!");

            return "redirect:/profile";
        }

    @GetMapping("/create-operator")
    public String showCreateOperatorForm(Model model) {
        model.addAttribute("user", new User());
        return "profile/create-operator";
    }

    @PostMapping("/create-operator")
    public String createOperator(@Valid @ModelAttribute("user") User userForm, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model) {

        if (userRepository.findByEmail(userForm.getEmail()).isPresent()) {
            bindingResult.addError(new FieldError("user", "email", "Email già in uso"));
        }

        if (bindingResult.hasErrors()) {
            userForm.setPassword("");
            model.addAttribute("user", userForm);
            return "profile/create-operator";
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
