package org.ticket.ticket_platform.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.ticket.ticket_platform.model.User;
import org.ticket.ticket_platform.repository.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isPresent()){
            return new DatabaseUserDetails(userOpt.get());
        } else{
            throw new UsernameNotFoundException("username not found");
        }
    }


}
