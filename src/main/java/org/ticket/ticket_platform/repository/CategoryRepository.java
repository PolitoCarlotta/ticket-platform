package org.ticket.ticket_platform.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ticket.ticket_platform.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    public Category findByCategory(String category);

}
