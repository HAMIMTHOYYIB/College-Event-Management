package com.example.basicweb.controller;

import com.example.basicweb.entity.CarouselSlide;
import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.EventRegistration;
import com.example.basicweb.entity.User;
import com.example.basicweb.repository.CarouselSlideRepository;
import com.example.basicweb.repository.EventCardRepository;
import com.example.basicweb.repository.EventRegistrationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
     * Admin Dashboard: Displays carousel slides, event cards, and registered student data.
     */
    @GetMapping
    public String adminDashboard(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<CarouselSlide> slides = carouselRepository.findAllByOrderByDisplayOrderAsc();
        List<EventCard> events = eventRepository.findAllByOrderByIdDesc();
        List<EventRegistration> registrations = registrationRepository.findAllByOrderByRegisteredAtDesc();

        // Count registrations per event
        Map<Long, Long> eventRegCounts = new HashMap<>();
        for (EventCard ev : events) {
            eventRegCounts.put(ev.getId(), registrationRepository.countByEventId(ev.getId()));
        }

        model.addAttribute("adminUser", currentUser);
        model.addAttribute("slides", slides);
        model.addAttribute("events", events);
        model.addAttribute("registrations", registrations);
        model.addAttribute("totalSlides", slides.size());
        model.addAttribute("totalEvents", events.size());
        model.addAttribute("totalRegistrations", registrations.size());
        model.addAttribute("eventRegCounts", eventRegCounts);

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
            @RequestParam(required = false, defaultValue = "1") Integer displayOrder,
            RedirectAttributes redirectAttributes) {

        CarouselSlide slide = new CarouselSlide(
                tag.trim(),
                title.trim(),
                description.trim(),
                badgeText.trim(),
                colorTheme.trim(),
                displayOrder
        );
        carouselRepository.save(slide);
        redirectAttributes.addFlashAttribute("successMessage", "Carousel slide created successfully!");
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
            @RequestParam(required = false, defaultValue = "1") Integer displayOrder,
            RedirectAttributes redirectAttributes) {

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
            redirectAttributes.addFlashAttribute("successMessage", "Carousel slide updated successfully!");
        }
        return "redirect:/admin";
    }

    @PostMapping("/carousel/delete/{id}")
    public String deleteSlide(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carouselRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Carousel slide deleted.");
        return "redirect:/admin";
    }

    // --- EVENT CARDS MANAGEMENT ---

    @PostMapping("/events/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String shortDescription,
            @RequestParam String fullDescription,
            @RequestParam(value = "eventDateInput", required = false) String eventDateInput,
            @RequestParam(value = "eventDate", required = false) String legacyEventDate,
            @RequestParam(value = "eventStartTime", required = false) String eventStartTime,
            @RequestParam(value = "eventTime", required = false) String legacyEventTime,
            @RequestParam(value = "eventEndTime", required = false, defaultValue = "") String eventEndTime,
            @RequestParam String venue,
            @RequestParam(required = false, defaultValue = "Campus Event Team") String organizer,
            @RequestParam(required = false, defaultValue = "Free Entry") String badge,
            @RequestParam(required = false, defaultValue = "sky") String colorAccent,
            @RequestParam(value = "maxCapacity", required = false, defaultValue = "0") Integer maxCapacity,
            RedirectAttributes redirectAttributes) {

        ParsedSchedule schedule = parseSchedule(eventDateInput, legacyEventDate, eventStartTime, legacyEventTime, eventEndTime);

        EventCard event = new EventCard(
                title.trim(),
                category.trim(),
                shortDescription.trim(),
                fullDescription.trim(),
                schedule.formattedDate,
                schedule.formattedTime,
                venue.trim(),
                organizer.trim(),
                badge.trim(),
                colorAccent.trim()
        );
        event.setEventDateTime(schedule.eventDateTime);
        event.setMaxCapacity(maxCapacity != null ? maxCapacity : 0);
        eventRepository.save(event);
        redirectAttributes.addFlashAttribute("successMessage", "Event '" + title.trim() + "' created successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/events/edit")
    public String editEvent(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String shortDescription,
            @RequestParam String fullDescription,
            @RequestParam(value = "eventDateInput", required = false) String eventDateInput,
            @RequestParam(value = "eventDate", required = false) String legacyEventDate,
            @RequestParam(value = "eventStartTime", required = false) String eventStartTime,
            @RequestParam(value = "eventTime", required = false) String legacyEventTime,
            @RequestParam(value = "eventEndTime", required = false, defaultValue = "") String eventEndTime,
            @RequestParam String venue,
            @RequestParam(required = false, defaultValue = "Campus Event Team") String organizer,
            @RequestParam(required = false, defaultValue = "Free Entry") String badge,
            @RequestParam(required = false, defaultValue = "sky") String colorAccent,
            @RequestParam(value = "maxCapacity", required = false, defaultValue = "0") Integer maxCapacity,
            RedirectAttributes redirectAttributes) {

        Optional<EventCard> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            EventCard event = eventOpt.get();
            ParsedSchedule schedule = parseSchedule(eventDateInput, legacyEventDate, eventStartTime, legacyEventTime, eventEndTime);

            event.setTitle(title.trim());
            event.setCategory(category.trim());
            event.setShortDescription(shortDescription.trim());
            event.setFullDescription(fullDescription.trim());
            event.setEventDate(schedule.formattedDate);
            event.setEventTime(schedule.formattedTime);
            event.setEventDateTime(schedule.eventDateTime);
            event.setVenue(venue.trim());
            event.setOrganizer(organizer.trim());
            event.setBadge(badge.trim());
            event.setColorAccent(colorAccent.trim());
            event.setMaxCapacity(maxCapacity != null ? maxCapacity : 0);
            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        }
        return "redirect:/admin";
    }

    @Transactional
    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Remove registrations referencing this event first to maintain database integrity
        List<EventRegistration> registrations = registrationRepository.findByEventId(id);
        if (!registrations.isEmpty()) {
            registrationRepository.deleteAll(registrations);
        }
        eventRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Event and associated registrations deleted.");
        return "redirect:/admin";
    }

    // --- PROGRAM REGISTRATIONS MANAGEMENT ---

    @Transactional
    @PostMapping("/registrations/delete/{id}")
    public String deleteRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        registrationRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student registration removed successfully.");
        return "redirect:/admin";
    }

    // --- HELPER: Resilient Date/Time Schedule Parser ---

    private static class ParsedSchedule {
        final String formattedDate;
        final String formattedTime;
        final LocalDateTime eventDateTime;

        ParsedSchedule(String formattedDate, String formattedTime, LocalDateTime eventDateTime) {
            this.formattedDate = formattedDate;
            this.formattedTime = formattedTime;
            this.eventDateTime = eventDateTime;
        }
    }

    private ParsedSchedule parseSchedule(String dateInput, String legacyDate,
                                          String startTimeInput, String legacyTime,
                                          String endTimeInput) {
        // 1. Resolve LocalDate
        LocalDate date = null;
        String rawDate = (dateInput != null && !dateInput.isBlank()) ? dateInput.trim() :
                (legacyDate != null && !legacyDate.isBlank() ? legacyDate.trim() : null);

        if (rawDate != null) {
            // Try ISO format: yyyy-MM-dd
            try {
                date = LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {
                // Try "MMM dd, yyyy" e.g. "Nov 15, 2026"
                try {
                    date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));
                } catch (Exception ignored2) {
                    try {
                        date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    } catch (Exception ignored3) {}
                }
            }
        }
        if (date == null) {
            date = LocalDate.now().plusDays(7);
        }
        String formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));

        // 2. Resolve Start LocalTime
        LocalTime startTime = null;
        String rawStartTime = (startTimeInput != null && !startTimeInput.isBlank()) ? startTimeInput.trim() : null;
        if (rawStartTime == null && legacyTime != null && !legacyTime.isBlank()) {
            // Legacy might be "10:00 AM - 3:00 PM" -> extract first part
            String[] parts = legacyTime.split("-");
            rawStartTime = parts[0].trim();
        }

        if (rawStartTime != null) {
            // Try 24-hour HH:mm
            try {
                startTime = LocalTime.parse(rawStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                try {
                    startTime = LocalTime.parse(rawStartTime, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
                } catch (Exception ignored2) {
                    try {
                        startTime = LocalTime.parse(rawStartTime, DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
                    } catch (Exception ignored3) {}
                }
            }
        }
        if (startTime == null) {
            startTime = LocalTime.of(10, 0);
        }
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));

        // 3. Resolve Optional End LocalTime
        LocalTime endTime = null;
        if (endTimeInput != null && !endTimeInput.isBlank()) {
            try {
                endTime = LocalTime.parse(endTimeInput.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                try {
                    endTime = LocalTime.parse(endTimeInput.trim(), DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
                } catch (Exception ignored2) {}
            }
        } else if (legacyTime != null && legacyTime.contains("-")) {
            String[] parts = legacyTime.split("-");
            if (parts.length > 1) {
                try {
                    endTime = LocalTime.parse(parts[1].trim(), DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
                } catch (Exception ignored) {}
            }
        }

        if (endTime != null) {
            formattedTime += " - " + endTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        }

        LocalDateTime eventDateTime = LocalDateTime.of(date, startTime);
        return new ParsedSchedule(formattedDate, formattedTime, eventDateTime);
    }
}
