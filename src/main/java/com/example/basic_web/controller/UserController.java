package com.example.basicweb.controller;

import com.example.basicweb.entity.CarouselSlide;
import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.EventRegistration;
import com.example.basicweb.entity.User;
import com.example.basicweb.repository.CarouselSlideRepository;
import com.example.basicweb.repository.EventCardRepository;
import com.example.basicweb.repository.EventRegistrationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final CarouselSlideRepository carouselRepository;
    private final EventCardRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public UserController(CarouselSlideRepository carouselRepository,
                          EventCardRepository eventRepository,
                          EventRegistrationRepository registrationRepository) {
        this.carouselRepository = carouselRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("currentUser");
        String username = (user != null) ? user.getUsername() : "Student";

        List<CarouselSlide> slides = carouselRepository.findAllByOrderByDisplayOrderAsc();
        List<EventCard> events = eventRepository.findAllByOrderByIdDesc();

        // Extract distinct categories in order
        Set<String> categories = new LinkedHashSet<>();
        categories.add("All Events");
        for (EventCard e : events) {
            if (e.getCategory() != null && !e.getCategory().isBlank()) {
                categories.add(e.getCategory().trim());
            }
        }

        // Get user's registered event IDs and registration data
        Set<Long> registeredEventIds = new HashSet<>();
        List<EventRegistration> userRegistrations = new ArrayList<>();
        if (user != null) {
            userRegistrations = registrationRepository.findByUserId(user.getId());
            registeredEventIds = userRegistrations.stream()
                    .map(r -> r.getEvent().getId())
                    .collect(Collectors.toSet());
        }

        // Build registration count map
        Map<Long, Long> registrationCounts = new HashMap<>();
        for (EventCard event : events) {
            registrationCounts.put(event.getId(), registrationRepository.countByEventId(event.getId()));
        }

        model.addAttribute("name", username);
        model.addAttribute("user", user);
        model.addAttribute("slides", slides);
        model.addAttribute("events", events);
        model.addAttribute("categories", categories);
        model.addAttribute("isAdmin", user != null && "ADMIN".equalsIgnoreCase(user.getRole()));
        model.addAttribute("registeredEventIds", registeredEventIds);
        model.addAttribute("userRegistrations", userRegistrations);
        model.addAttribute("registrationCounts", registrationCounts);

        return "views/home";
    }

    /**
     * Register the current user for an event (AJAX endpoint).
     */
    @PostMapping("/events/{eventId}/register")
    @ResponseBody
    public Map<String, Object> registerForEvent(@PathVariable Long eventId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) request.getAttribute("currentUser");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Please log in to register for events.");
            return response;
        }

        Optional<EventCard> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Event not found.");
            return response;
        }

        if (registrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            response.put("success", false);
            response.put("message", "You are already registered for this event.");
            return response;
        }

        EventRegistration registration = new EventRegistration(user, eventOpt.get());
        registrationRepository.save(registration);

        long count = registrationRepository.countByEventId(eventId);
        response.put("success", true);
        response.put("message", "Successfully registered for " + eventOpt.get().getTitle() + "!");
        response.put("registrationCount", count);
        return response;
    }

    /**
     * Unregister the current user from an event (AJAX endpoint).
     */
    @PostMapping("/events/{eventId}/unregister")
    @ResponseBody
    @Transactional
    public Map<String, Object> unregisterFromEvent(@PathVariable Long eventId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) request.getAttribute("currentUser");

        if (user == null) {
            response.put("success", false);
            response.put("message", "Please log in first.");
            return response;
        }

        if (!registrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            response.put("success", false);
            response.put("message", "You are not registered for this event.");
            return response;
        }

        registrationRepository.deleteByUserIdAndEventId(user.getId(), eventId);

        long count = registrationRepository.countByEventId(eventId);
        response.put("success", true);
        response.put("message", "Successfully unregistered from the event.");
        response.put("registrationCount", count);
        return response;
    }
}