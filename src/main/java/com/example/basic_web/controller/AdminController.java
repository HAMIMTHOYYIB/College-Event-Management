package com.example.basicweb.controller;

import com.example.basicweb.entity.CarouselSlide;
import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.User;
import com.example.basicweb.repository.CarouselSlideRepository;
import com.example.basicweb.repository.EventCardRepository;
import com.example.basicweb.repository.EventRegistrationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarouselSlideRepository carouselRepository;
    private final EventCardRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public AdminController(CarouselSlideRepository carouselRepository,
                           EventCardRepository eventRepository,
                           EventRegistrationRepository registrationRepository) {
        this.carouselRepository = carouselRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    /**
     * Admin Dashboard: Displays carousel slide list and event cards for management.
     */
    @GetMapping
    public String adminDashboard(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<CarouselSlide> slides = carouselRepository.findAllByOrderByDisplayOrderAsc();
        List<EventCard> events = eventRepository.findAllByOrderByIdDesc();

        model.addAttribute("adminUser", currentUser);
        model.addAttribute("slides", slides);
        model.addAttribute("events", events);
        model.addAttribute("totalSlides", slides.size());
        model.addAttribute("totalEvents", events.size());

        return "views/admin";
    }

    // --- CAROUSEL SLIDE MANAGEMENT ---

    @PostMapping("/carousel/create")
    public String createSlide(
            @RequestParam String tag,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "") String badgeText,
            @RequestParam(required = false, defaultValue = "sky") String colorTheme,
            @RequestParam(required = false, defaultValue = "1") Integer displayOrder) {

        CarouselSlide slide = new CarouselSlide(
                tag.trim(),
                title.trim(),
                description.trim(),
                badgeText.trim(),
                colorTheme.trim(),
                displayOrder
        );
        carouselRepository.save(slide);
        return "redirect:/admin";
    }

    @PostMapping("/carousel/edit")
    public String editSlide(
            @RequestParam Long id,
            @RequestParam String tag,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "") String badgeText,
            @RequestParam(required = false, defaultValue = "sky") String colorTheme,
            @RequestParam(required = false, defaultValue = "1") Integer displayOrder) {

        Optional<CarouselSlide> slideOpt = carouselRepository.findById(id);
        if (slideOpt.isPresent()) {
            CarouselSlide slide = slideOpt.get();
            slide.setTag(tag.trim());
            slide.setTitle(title.trim());
            slide.setDescription(description.trim());
            slide.setBadgeText(badgeText.trim());
            slide.setColorTheme(colorTheme.trim());
            slide.setDisplayOrder(displayOrder);
            carouselRepository.save(slide);
        }
        return "redirect:/admin";
    }

    @PostMapping("/carousel/delete/{id}")
    public String deleteSlide(@PathVariable Long id) {
        carouselRepository.deleteById(id);
        return "redirect:/admin";
    }

    // --- EVENT CARDS MANAGEMENT ---

    @PostMapping("/events/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String shortDescription,
            @RequestParam String fullDescription,
            @RequestParam String eventDateInput,
            @RequestParam String eventStartTime,
            @RequestParam(required = false, defaultValue = "") String eventEndTime,
            @RequestParam String venue,
            @RequestParam(required = false, defaultValue = "Campus Event Team") String organizer,
            @RequestParam(required = false, defaultValue = "Free Entry") String badge,
            @RequestParam(required = false, defaultValue = "sky") String colorAccent) {

        // Parse date from calendar input (yyyy-MM-dd)
        LocalDate date = LocalDate.parse(eventDateInput.trim());
        String formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));

        // Parse time (HH:mm) and format to 12-hour
        LocalTime startTime = LocalTime.parse(eventStartTime.trim());
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        if (eventEndTime != null && !eventEndTime.isBlank()) {
            LocalTime endTime = LocalTime.parse(eventEndTime.trim());
            formattedTime += " - " + endTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        }

        // Build LocalDateTime for countdown timer
        LocalDateTime eventDateTime = LocalDateTime.of(date, startTime);

        EventCard event = new EventCard(
                title.trim(),
                category.trim(),
                shortDescription.trim(),
                fullDescription.trim(),
                formattedDate,
                formattedTime,
                venue.trim(),
                organizer.trim(),
                badge.trim(),
                colorAccent.trim()
        );
        event.setEventDateTime(eventDateTime);
        eventRepository.save(event);
        return "redirect:/admin";
    }

    @PostMapping("/events/edit")
    public String editEvent(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String shortDescription,
            @RequestParam String fullDescription,
            @RequestParam String eventDateInput,
            @RequestParam String eventStartTime,
            @RequestParam(required = false, defaultValue = "") String eventEndTime,
            @RequestParam String venue,
            @RequestParam(required = false, defaultValue = "Campus Event Team") String organizer,
            @RequestParam(required = false, defaultValue = "Free Entry") String badge,
            @RequestParam(required = false, defaultValue = "sky") String colorAccent) {

        Optional<EventCard> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            EventCard event = eventOpt.get();

            // Parse date from calendar input (yyyy-MM-dd)
            LocalDate date = LocalDate.parse(eventDateInput.trim());
            String formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));

            LocalTime startTime = LocalTime.parse(eventStartTime.trim());
            String formattedTime = startTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
            if (eventEndTime != null && !eventEndTime.isBlank()) {
                LocalTime endTime = LocalTime.parse(eventEndTime.trim());
                formattedTime += " - " + endTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
            }

            LocalDateTime eventDateTime = LocalDateTime.of(date, startTime);

            event.setTitle(title.trim());
            event.setCategory(category.trim());
            event.setShortDescription(shortDescription.trim());
            event.setFullDescription(fullDescription.trim());
            event.setEventDate(formattedDate);
            event.setEventTime(formattedTime);
            event.setEventDateTime(eventDateTime);
            event.setVenue(venue.trim());
            event.setOrganizer(organizer.trim());
            event.setBadge(badge.trim());
            event.setColorAccent(colorAccent.trim());
            eventRepository.save(event);
        }
        return "redirect:/admin";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/admin";
    }
}
