package com.example.basicweb.repository;

import com.example.basicweb.entity.CarouselSlide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselSlideRepository extends JpaRepository<CarouselSlide, Long> {

    List<CarouselSlide> findAllByOrderByDisplayOrderAsc();
}
