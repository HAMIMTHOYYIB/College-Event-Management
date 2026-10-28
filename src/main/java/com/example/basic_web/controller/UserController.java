package com.example.basicweb.controller;

import com.example.basicweb.entity.CarouselSlide;
import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.EventRegistration;
import com.example.basicweb.entity.User;
import com.example.basicweb.repository.CarouselSlideRepository;
import com.example.basicweb.repository.EventCardRepository;
import com.example.basicweb.repository.EventRegistrationRepository;
import com.example.basicweb.repository.UserRepository;
import com.example.basicweb.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final CarouselSlideRepository carouselRepository;
    private final EventCardRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(CarouselSlideRepository carouselRepository,
                          EventCardRepository eventRepository,
                          EventRegistrationRepository registrationRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.carouselRepository = carouselRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        // Find next event for live countdown hero
        LocalDateTime now = LocalDateTime.now();
        EventCard nextRegisteredEvent = null;
        boolean isRegisteredHero = false;

        // 1. First priority: Check user's registered events for upcoming schedule
        if (!userRegistrations.isEmpty()) {
            nextRegisteredEvent = userRegistrations.stream()
                    .map(EventRegistration::getEvent)
                    .filter(e -> e.getEventDateTime() != null && e.getEventDateTime().isAfter(now))
                    .min(Comparator.comparing(EventCard::getEventDateTime))
                    .orElse(null);

            if (nextRegisteredEvent != null) {
                isRegisteredHero = true;
            } else {
                // If registered events are in progress or completed, show the most recent registered one
                nextRegisteredEvent = userRegistrations.get(0).getEvent();
                isRegisteredHero = true;
            }
        }

        // 2. Second priority: If student hasn't registered yet, feature next upcoming campus program
        if (nextRegisteredEvent == null && !events.isEmpty()) {
            nextRegisteredEvent = events.stream()
                    .filter(e -> e.getEventDateTime() != null && e.getEventDateTime().isAfter(now))
                    .min(Comparator.comparing(EventCard::getEventDateTime))
                    .orElse(events.get(0));
            isRegisteredHero = false;
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
        model.addAttribute("nextRegisteredEvent", nextRegisteredEvent);
        model.addAttribute("isRegisteredHero", isRegisteredHero);

        return "views/home";
    }

    /**
     * Register the current user for an event (AJAX endpoint).
     * Enforces maximum capacity limit.
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

        EventCard event = eventOpt.get();

        if (registrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            response.put("success", false);
            response.put("message", "You are already registered for this event.");
            return response;
        }

        long currentCount = registrationRepository.countByEventId(eventId);
        if (event.isFull(currentCount)) {
            response.put("success", false);
            response.put("message", "Registration closed: This program has reached maximum capacity (" + event.getMaxCapacity() + " seats).");
            return response;
        }

        EventRegistration registration = new EventRegistration(user, event);
        registrationRepository.save(registration);

        long count = registrationRepository.countByEventId(eventId);
        response.put("success", true);
        response.put("message", "Successfully registered for " + event.getTitle() + "!");
        response.put("registrationCount", count);
        response.put("maxCapacity", event.getMaxCapacity());
        response.put("eventId", event.getId());
        response.put("eventTitle", event.getTitle());
        response.put("eventDate", event.getEventDate());
        response.put("eventTime", event.getEventTime());
        response.put("eventDateTime", event.getEventDateTime() != null ? event.getEventDateTime().toString() : "");
        response.put("venue", event.getVenue());
        response.put("category", event.getCategory());
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

    /**
     * Download iCalendar (.ics) file for an event to add to Outlook, Apple Calendar, or mobile calendars.
     */
    @GetMapping(value = "/events/{eventId}/calendar.ics", produces = "text/calendar")
    public ResponseEntity<String> downloadCalendarFile(@PathVariable Long eventId) {
        Optional<EventCard> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        EventCard event = eventOpt.get();
        LocalDateTime dt = event.getEventDateTime() != null ? event.getEventDateTime() : LocalDateTime.now().plusDays(1);
        LocalDateTime dtEnd = dt.plusHours(2);

        DateTimeFormatter icsFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String dtStartStr = dt.format(icsFmt);
        String dtEndStr = dtEnd.format(icsFmt);
        String nowStr = LocalDateTime.now().format(icsFmt);

        String safeSummary = event.getTitle().replace("\n", " ").replace("\r", "");
        String safeDesc = (event.getShortDescription() != null ? event.getShortDescription() : "")
                .replace("\n", " ").replace("\r", "");
        String safeLocation = (event.getVenue() != null ? event.getVenue() : "Campus")
                .replace("\n", " ").replace("\r", "");

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//CampusPulse//EventPortal//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:event-").append(event.getId()).append("-").append(System.currentTimeMillis()).append("@campuspulse.edu\r\n");
        ics.append("DTSTAMP:").append(nowStr).append("Z\r\n");
        ics.append("DTSTART:").append(dtStartStr).append("\r\n");
        ics.append("DTEND:").append(dtEndStr).append("\r\n");
        ics.append("SUMMARY:").append(safeSummary).append("\r\n");
        ics.append("DESCRIPTION:").append(safeDesc).append("\r\n");
        ics.append("LOCATION:").append(safeLocation).append("\r\n");
        ics.append("STATUS:CONFIRMED\r\n");
        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        String safeFilename = event.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".ics";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                .body(ics.toString());
    }

    /**
     * Student Profile & Password Update (AJAX endpoint).
     */
    @PostMapping("/profile/update")
    @ResponseBody
    @Transactional
    public Map<String, Object> updateProfile(
            @RequestParam String username,
            @RequestParam String collegeName,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            HttpServletRequest request,
            HttpServletResponse response) {

        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "User session expired. Please log in again.");
            return result;
        }

        // Fetch fresh managed entity from database
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User account not found.");
            return result;
        }
        User user = userOpt.get();

        // Validate username and college
        if (username == null || username.isBlank()) {
            result.put("success", false);
            result.put("message", "Full name cannot be blank.");
            return result;
        }
        if (collegeName == null || collegeName.isBlank()) {
            result.put("success", false);
            result.put("message", "College name cannot be blank.");
            return result;
        }

        // Validate 10-digit Indian phone number (starts with 6-9, no +91 prefix)
        String trimmedPhone = phoneNumber != null ? phoneNumber.trim().replace(" ", "").replace("-", "") : "";
        if (trimmedPhone.contains("+91") || trimmedPhone.startsWith("+")) {
            result.put("success", false);
            result.put("message", "Please enter only the 10-digit phone number without +91.");
            return result;
        }
        if (!trimmedPhone.matches("^[6-9]\\d{9}$")) {
            result.put("success", false);
            result.put("message", "Phone number must be a valid 10-digit Indian mobile number (e.g. 9876543210).");
            return result;
        }

        user.setUsername(username.trim());
        user.setCollegeName(collegeName.trim());
        user.setPhoneNumber(trimmedPhone);

        boolean passwordChanged = false;
        String curPass = (currentPassword != null) ? currentPassword.trim() : "";
        String newPass = (newPassword != null) ? newPassword.trim() : "";
        String confPass = (confirmPassword != null) ? confirmPassword.trim() : "";

        // If user provided a new password, verify identity and change password
        if (!newPass.isEmpty() || !confPass.isEmpty()) {
            if (curPass.isEmpty()) {
                result.put("success", false);
                result.put("message", "Please enter your current password to authorize this password change.");
                return result;
            }

            if (!passwordEncoder.matches(curPass, user.getPassword())) {
                result.put("success", false);
                result.put("message", "Current password does not match your existing password.");
                return result;
            }

            if (newPass.length() < 6) {
                result.put("success", false);
                result.put("message", "New password must be at least 6 characters long.");
                return result;
            }

            if (!confPass.isEmpty() && !newPass.equals(confPass)) {
                result.put("success", false);
                result.put("message", "New password and confirmation password do not match.");
                return result;
            }

            // Successfully change password
            user.setPassword(passwordEncoder.encode(newPass));
            passwordChanged = true;
        } else if (!curPass.isEmpty()) {
            // User provided current password without a new password: verify correctness
            if (!passwordEncoder.matches(curPass, user.getPassword())) {
                result.put("success", false);
                result.put("message", "Current password does not match your existing password.");
                return result;
            }
        }

        userRepository.saveAndFlush(user);

        // Re-issue JWT cookie so session stays valid and in sync with updated name
        String token = jwtService.generateToken(user.getEmail(), user.getUsername());
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        result.put("success", true);
        if (passwordChanged) {
            result.put("message", "Password and profile updated successfully!");
        } else {
            result.put("message", "Profile details updated successfully!");
        }
        result.put("passwordChanged", passwordChanged);
        result.put("username", user.getUsername());
        result.put("collegeName", user.getCollegeName());
        result.put("phoneNumber", user.getPhoneNumber());
        return result;
    }
}
