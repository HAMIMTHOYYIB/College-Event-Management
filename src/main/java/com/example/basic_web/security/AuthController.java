package com.example.basicweb.security;

import com.example.basicweb.entity.CarouselSlide;
import com.example.basicweb.entity.EventCard;
import com.example.basicweb.entity.User;
import com.example.basicweb.repository.CarouselSlideRepository;
import com.example.basicweb.repository.EventCardRepository;
import com.example.basicweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    private final JwtService jwtService;
    private final JwtAuthenticationInterceptor jwtInterceptor;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            JwtService jwtService,
            JwtAuthenticationInterceptor jwtInterceptor,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.jwtInterceptor = jwtInterceptor;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Pre-populate single admin, demo student, initial carousel slides, and events.
     */
    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepo,
            CarouselSlideRepository slideRepo,
            EventCardRepository eventRepo,
            PasswordEncoder encoder) {
        return args -> {
            // 1. Ensure the single Event Admin exists
            if (userRepo.findByEmail("eventadmin@gmail.com").isEmpty()) {
                User admin = new User(
                        "Event Administrator",
                        "eventadmin@gmail.com",
                        encoder.encode("admin123"),
                        "+1 (800) 555-0199",
                        "Central Campus Administration",
                        "ADMIN"
                );
                userRepo.save(admin);
                System.out.println("Seeded single Admin: eventadmin@gmail.com / admin123");
            }

            // 2. Ensure a sample student exists
            if (userRepo.findByEmail("john@example.com").isEmpty()) {
                User demoUser = new User(
                        "John Doe",
                        "john@example.com",
                        encoder.encode("password123"),
                        "+1 (555) 019-2834",
                        "Stanford University",
                        "USER"
                );
                userRepo.save(demoUser);
            }

            // 3. Seed initial Carousel Slides if empty
            if (slideRepo.count() == 0) {
                slideRepo.save(new CarouselSlide(
                        "★ Innovation Center",
                        "AI & Supercomputing Research Lab 2026",
                        "Explore state-of-the-art supercomputing clusters, hardware testbeds, and collaborative research spaces available for verified students.",
                        "Access Granted • Level 4 Tech Hub",
                        "sky",
                        1
                ));
                slideRepo.save(new CarouselSlide(
                        "🚀 Hackathon 2026",
                        "Inter-College 48-Hour Global Hackathon",
                        "Represent your college, team up with peers, and build groundbreaking web and mobile products to compete for $25,000 in student grants.",
                        "Registrations Open • Free Entry",
                        "blue",
                        2
                ));
                slideRepo.save(new CarouselSlide(
                        "🌐 Career Mentorship",
                        "Direct Tech Mentorship & Alumni Network",
                        "Connect 1-on-1 with alumni software engineers, product leaders, and startup founders for resume reviews and interview preparation.",
                        "Weekly Sessions • 1,200+ Mentors",
                        "cyan",
                        3
                ));
            }

            // 4. Seed initial Event Cards if empty
            if (eventRepo.count() == 0) {
                EventCard ev1 = new EventCard(
                        "Web3 & Cloud Architecture Bootcamp",
                        "Technology",
                        "Hands-on intensive bootcamp exploring modern microservices, Kubernetes clusters, and cloud deployment pipelines.",
                        "Join our 2-day hands-on technical workshop organized by the Computer Science Society. In this workshop, students will configure Docker containers, orchestrate microservices with Kubernetes, benchmark performance, and deploy scalable cloud architectures. Free cloud credits and certificates are provided to all attendees.",
                        "Nov 15, 2026",
                        "10:00 AM - 3:30 PM",
                        "Tech Pavilion - Lab Hall 3",
                        "Computer Science Society",
                        "Certificate Included",
                        "sky"
                );
                ev1.setEventDateTime(LocalDateTime.of(2026, 11, 15, 10, 0));
                eventRepo.save(ev1);

                EventCard ev2 = new EventCard(
                        "Autonomous Drone Obstacle Challenge",
                        "Sports",
                        "Exciting autonomous robotics navigation races and first-person drone obstacle challenges.",
                        "The Robotics Society invites students and drone enthusiasts to the 2026 Autonomous Drone & Rover Championship. Teams will deploy autonomous perception models to navigate complex indoor obstacle courses, followed by live high-speed exhibition matches. Cash awards for the top three fastest runs.",
                        "Nov 18, 2026",
                        "2:00 PM - 6:00 PM",
                        "University Sports Arena",
                        "Robotics Society",
                        "Prize: $3,500",
                        "blue"
                );
                ev2.setEventDateTime(LocalDateTime.of(2026, 11, 18, 14, 0));
                eventRepo.save(ev2);

                EventCard ev3 = new EventCard(
                        "Product Design & UI/UX Design Sprint",
                        "Workshops",
                        "Master digital design systems, user journeys, Figma prototyping, and modern web aesthetics.",
                        "Led by senior product designers, this interactive 4-hour workshop guides you through user research, wireframing, creating component libraries, and designing intuitive interfaces with micro-interactions. Bring your laptop and your creative ideas!",
                        "Nov 22, 2026",
                        "11:00 AM - 3:00 PM",
                        "Design Hub Studio A",
                        "Design & Media Guild",
                        "Interactive Workshop",
                        "cyan"
                );
                ev3.setEventDateTime(LocalDateTime.of(2026, 11, 22, 11, 0));
                eventRepo.save(ev3);

                EventCard ev4 = new EventCard(
                        "Campus Annual Cultural & Music Fest",
                        "Cultural",
                        "An evening of live band performances, acoustic sets, theatrical drama, and artisan food trucks.",
                        "Celebrate campus diversity and student talent at our flagship cultural celebration! Featuring live band battles, dance showcases, spoken word poetry, and regional culinary stalls curated by student communities. Free entry for all students.",
                        "Nov 28, 2026",
                        "5:00 PM - 10:00 PM",
                        "Open Air Amphitheatre",
                        "Student Cultural Council",
                        "Festival Pass: Free",
                        "sky"
                );
                ev4.setEventDateTime(LocalDateTime.of(2026, 11, 28, 17, 0));
                eventRepo.save(ev4);

                EventCard ev5 = new EventCard(
                        "AI Ethics & Future of Robotics Symposium",
                        "Seminars",
                        "Renowned researchers discuss generative AI safety, model alignment, and robotics automation.",
                        "A premier academic symposium featuring keynote addresses and panel debates with leading AI researchers, robotics engineers, and policy advocates. Topics include autonomous systems regulation, neural network interpretability, and responsible technology leadership.",
                        "Dec 02, 2026",
                        "1:30 PM - 5:00 PM",
                        "Main Auditorium Hall",
                        "Faculty of Engineering",
                        "Keynote & Q&A",
                        "blue"
                );
                ev5.setEventDateTime(LocalDateTime.of(2026, 12, 2, 13, 30));
                eventRepo.save(ev5);

                EventCard ev6 = new EventCard(
                        "Inter-Department Cricket & Football Cup",
                        "Sports",
                        "Cheer for your department in the semester championship across football, cricket, and athletics.",
                        "The biggest athletic event of the semester! Departments compete for the championship trophy across football, cricket, and track events. Live commentary, student refreshments, and medal presentations following the final matches.",
                        "Dec 06, 2026",
                        "9:00 AM - 5:00 PM",
                        "Central Sports Field",
                        "Athletics Council",
                        "Annual Cup 2026",
                        "cyan"
                );
                ev6.setEventDateTime(LocalDateTime.of(2026, 12, 6, 9, 0));
                eventRepo.save(ev6);
            }
        };
    }

    /**
     * Show Login page. If already authenticated, redirect appropriately.
     */
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtInterceptor.resolveToken(request);
        if (token != null && jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return "ADMIN".equalsIgnoreCase(user.getRole()) ? "redirect:/admin" : "redirect:/";
                } else {
                    clearJwtCookie(response);
                }
            }
        }
        return "views/login";
    }

    /**
     * Process login by validating email and password from SQL database.
     * Admin automatically redirected to /admin; students to /.
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            HttpServletResponse response,
            Model model) {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Email and password are required.");
            return "views/login";
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "views/login";
        }

        User user = userOpt.get();
        issueJwtCookie(response, user.getEmail(), user.getUsername());

        // Redirect Admin directly to Admin portal; student to Home page
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }

    /**
     * Show Signup page. Only for students (admin does not signup).
     */
    @GetMapping("/signup")
    public String showSignupPage(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtInterceptor.resolveToken(request);
        if (token != null && jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return "ADMIN".equalsIgnoreCase(user.getRole()) ? "redirect:/admin" : "redirect:/";
                } else {
                    clearJwtCookie(response);
                }
            }
        }
        return "views/signup";
    }

    /**
     * Process student signup, hash password, store in SQL database,
     * issue 24h JWT cookie, and redirect to /.
     */
    @PostMapping("/signup")
    public String processSignup(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String collegeName,
            HttpServletResponse response,
            Model model) {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()
                || collegeName == null || collegeName.isBlank()) {
            model.addAttribute("error", "All fields are required for signup.");
            return "views/signup";
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Prevent creating admin accounts through signup
        if ("eventadmin@gmail.com".equalsIgnoreCase(normalizedEmail)) {
            model.addAttribute("error", "Administrator account cannot be created through signup. Please login directly.");
            return "views/signup";
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            model.addAttribute("error", "An account with this email already exists. Please log in.");
            return "views/signup";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            return "views/signup";
        }

        // Create student account with role USER
        User newUser = new User(
                username.trim(),
                normalizedEmail,
                passwordEncoder.encode(password),
                phoneNumber.trim(),
                collegeName.trim(),
                "USER"
        );
        userRepository.save(newUser);

        // Issue 24-hour JWT token cookie and redirect to /
        issueJwtCookie(response, newUser.getEmail(), newUser.getUsername());

        return "redirect:/";
    }

    /**
     * Invalidate JWT cookie and redirect to /login.
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        clearJwtCookie(response);
        return "redirect:/login";
    }

    private void issueJwtCookie(HttpServletResponse response, String email, String username) {
        String token = jwtService.generateToken(email, username);
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
