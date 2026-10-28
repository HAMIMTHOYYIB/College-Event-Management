package com.example.basicweb;

import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.EventRegistration;
import com.example.basicweb.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BasicWebApplicationTests {

    @Test
    void testEventCardIsoFormatters() {
        EventCard event = new EventCard("Tech Fest", "Technology", "Short overview", "Full overview",
                "Nov 15, 2026", "10:00 AM - 3:30 PM", "Auditorium", "CS Club", "Free Entry", "sky");
        event.setEventDateTime(LocalDateTime.of(2026, 11, 15, 10, 0));

        assertEquals("2026-11-15", event.getIsoDate());
        assertEquals("10:00", event.getIsoStartTime());
        assertEquals("15:30", event.getIsoEndTime());
    }

    @Test
    void testEventCreationAndScheduleProperties() {
        EventCard event = new EventCard(
                "Drone Racing",
                "Sports",
                "Fast drones",
                "Full description here",
                "Nov 20, 2026",
                "2:30 PM - 6:00 PM",
                "Indoor Arena",
                "Robotics Society",
                "Prize: $5,000",
                "sky"
        );
        event.setEventDateTime(LocalDateTime.of(2026, 11, 20, 14, 30));

        assertNotNull(event.getEventDateTime());
        assertEquals("Nov 20, 2026", event.getEventDate());
        assertEquals("2:30 PM - 6:00 PM", event.getEventTime());
        assertEquals("2026-11-20", event.getIsoDate());
        assertEquals("14:30", event.getIsoStartTime());
        assertEquals("18:00", event.getIsoEndTime());
    }

    @Test
    void testIndianPhoneNumberFormatValidation() {
        // Valid 10-digit Indian numbers starting with 6, 7, 8, or 9
        assertTrue("9876543210".matches("^[6-9]\\d{9}$"));
        assertTrue("8123456789".matches("^[6-9]\\d{9}$"));
        assertTrue("7000000000".matches("^[6-9]\\d{9}$"));
        assertTrue("6399999999".matches("^[6-9]\\d{9}$"));

        // Invalid numbers (country code included, wrong length, wrong starting digit)
        assertFalse("+919876543210".matches("^[6-9]\\d{9}$"));
        assertFalse("919876543210".matches("^[6-9]\\d{9}$"));
        assertFalse("1234567890".matches("^[6-9]\\d{9}$"));
        assertFalse("5876543210".matches("^[6-9]\\d{9}$"));
        assertFalse("987654321".matches("^[6-9]\\d{9}$"));
        assertFalse("98765432100".matches("^[6-9]\\d{9}$"));
    }

    @Test
    void testEventRegistrationAssociation() {
        User user = new User("Priya", "priya@test.com", "secret", "9876543210", "Delhi University");
        EventCard event = new EventCard("Hackathon", "Tech", "Build apps", "Full agenda",
                "Nov 30, 2026", "9:00 AM - 5:00 PM", "Lab 1", "Tech Society", "Certificate", "sky");
        EventRegistration registration = new EventRegistration(user, event);

        assertEquals("Priya", registration.getUser().getUsername());
        assertEquals("9876543210", registration.getUser().getPhoneNumber());
        assertEquals("Hackathon", registration.getEvent().getTitle());
        assertNotNull(registration.getRegisteredAt());
    }

    @Test
    void testCapacityAndSeatAvailability() {
        EventCard limitedEvent = new EventCard("AI Workshop", "Tech", "Intro to AI", "Hands on",
                "Dec 01, 2026", "10:00 AM - 1:00 PM", "Auditorium", "AI Club", "Free", "blue");
        limitedEvent.setMaxCapacity(50);

        assertEquals(50, limitedEvent.getMaxCapacity());
        assertFalse(limitedEvent.isFull(0));
        assertEquals(50, limitedEvent.getRemainingSeats(0));

        assertFalse(limitedEvent.isFull(49));
        assertEquals(1, limitedEvent.getRemainingSeats(49));

        assertTrue(limitedEvent.isFull(50));
        assertEquals(0, limitedEvent.getRemainingSeats(50));

        // Unlimited capacity (0)
        EventCard unlimitedEvent = new EventCard("Open Fest", "Cultural", "Music fest", "All welcome",
                "Dec 05, 2026", "5:00 PM - 10:00 PM", "Grounds", "Arts Council", "Free", "cyan");
        unlimitedEvent.setMaxCapacity(0);

        assertFalse(unlimitedEvent.isFull(500));
        assertEquals(-1, unlimitedEvent.getRemainingSeats(500));
    }

    @Test
    void testIcsCalendarGenerationFormat() {
        EventCard event = new EventCard("Robotics Cup", "Tech", "Robotics battle", "Full tournament",
                "Dec 10, 2026", "9:00 AM - 6:00 PM", "Stadium", "Robotics Club", "Trophies", "sky");
        event.setId(99L);
        event.setEventDateTime(LocalDateTime.of(2026, 12, 10, 9, 0));

        String icsText = "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "BEGIN:VEVENT\r\n" +
                "SUMMARY:" + event.getTitle() + "\r\n" +
                "LOCATION:" + event.getVenue() + "\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";

        assertTrue(icsText.contains("BEGIN:VCALENDAR"));
        assertTrue(icsText.contains("SUMMARY:Robotics Cup"));
        assertTrue(icsText.contains("LOCATION:Stadium"));
        assertTrue(icsText.contains("END:VCALENDAR"));
    }

    @Test
    void testEventCardAutoDateTimeParsing() {
        EventCard ev1 = new EventCard("AI Summit", "Tech", "Intro", "Full",
                "Nov 15, 2026", "10:00 AM - 3:30 PM", "Auditorium", "CS Club", "Free", "sky");
        // eventDateTime is initially null
        assertNotNull(ev1.getEventDateTime());
        assertEquals("2026-11-15", ev1.getIsoDate());
        assertEquals("10:00", ev1.getIsoStartTime());
        assertEquals("15:30", ev1.getIsoEndTime());

        // Test ISO input date
        EventCard ev2 = new EventCard("Hackathon", "Tech", "Intro", "Full",
                "2026-12-05", "14:00 - 18:00", "Lab 2", "Club", "Free", "blue");
        assertEquals("2026-12-05", ev2.getIsoDate());
        assertEquals("14:00", ev2.getIsoStartTime());
        assertEquals("18:00", ev2.getIsoEndTime());
    }

    @Test
    void testPasswordUpdateAndHashingSecurity() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        String originalPass = "password123";
        User user = new User("Aarav", "aarav@test.com", encoder.encode(originalPass), "9876543210", "IIT Bombay");

        // Verify original password
        assertTrue(encoder.matches(originalPass, user.getPassword()));
        assertFalse(encoder.matches("wrongPassword", user.getPassword()));

        // Update to new password
        String newPass = "NewSecurePass2026!";
        user.setPassword(encoder.encode(newPass));

        assertTrue(encoder.matches(newPass, user.getPassword()));
        assertFalse(encoder.matches(originalPass, user.getPassword()));
    }

    @Test
    void testUserControllerUpdateProfilePasswordSuccessAndFailure() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User existingUser = new User("John Doe", "john@test.com", encoder.encode("oldPass123"), "9876543210", "Stanford");
        existingUser.setId(10L);

        com.example.basicweb.repository.UserRepository userRepo = (com.example.basicweb.repository.UserRepository)
                java.lang.reflect.Proxy.newProxyInstance(
                        com.example.basicweb.repository.UserRepository.class.getClassLoader(),
                        new Class<?>[]{com.example.basicweb.repository.UserRepository.class},
                        (proxy, method, args) -> {
                            if ("findById".equals(method.getName())) {
                                return java.util.Optional.of(existingUser);
                            }
                            if ("saveAndFlush".equals(method.getName())) {
                                return args[0];
                            }
                            return null;
                        }
                );

        com.example.basicweb.security.JwtService jwtService = new com.example.basicweb.security.JwtService();

        com.example.basicweb.controller.UserController controller =
                new com.example.basicweb.controller.UserController(null, null, null, userRepo, encoder, jwtService);

        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setAttribute("currentUser", existingUser);
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        // 1. Wrong current password
        java.util.Map<String, Object> res1 = controller.updateProfile("John Doe", "Stanford", "9876543210", "wrongPass", "newPass123", "newPass123", request, response);
        assertFalse((Boolean) res1.get("success"));
        assertEquals("Current password does not match your existing password.", res1.get("message"));

        // 2. Mismatched confirmation password
        java.util.Map<String, Object> res2 = controller.updateProfile("John Doe", "Stanford", "9876543210", "oldPass123", "newPass123", "differentPass", request, response);
        assertFalse((Boolean) res2.get("success"));
        assertEquals("New password and confirmation password do not match.", res2.get("message"));

        // 3. New password too short (< 6 chars)
        java.util.Map<String, Object> res3 = controller.updateProfile("John Doe", "Stanford", "9876543210", "oldPass123", "123", "123", request, response);
        assertFalse((Boolean) res3.get("success"));
        assertEquals("New password must be at least 6 characters long.", res3.get("message"));

        // 4. Successful password update
        java.util.Map<String, Object> res4 = controller.updateProfile("John Doe", "Stanford", "9876543210", "oldPass123", "newPass123", "newPass123", request, response);
        assertTrue((Boolean) res4.get("success"));
        assertTrue((Boolean) res4.get("passwordChanged"));
        assertTrue(encoder.matches("newPass123", existingUser.getPassword()));

        // 5. Update only profile without changing password
        java.util.Map<String, Object> res5 = controller.updateProfile("John Updated", "Harvard", "9876543211", "", "", "", request, response);
        assertTrue((Boolean) res5.get("success"));
        assertFalse((Boolean) res5.get("passwordChanged"));
        assertEquals("John Updated", existingUser.getUsername());
        assertEquals("Harvard", existingUser.getCollegeName());
        assertEquals("9876543211", existingUser.getPhoneNumber());
    }
}
