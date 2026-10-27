package com.example.basicweb.repository;

import com.example.basicweb.entity.EventCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCardRepository extends JpaRepository<EventCard, Long> {

    List<EventCard> findAllByOrderByIdDesc();

    List<EventCard> findByCategoryOrderByIdDesc(String category);
}
