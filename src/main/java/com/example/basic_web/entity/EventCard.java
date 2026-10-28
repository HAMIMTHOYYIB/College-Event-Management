package com.example.basicweb.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "event_cards")
public class EventCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // e.g. "Technology", "Workshops", "Cultural", "Sports", "Seminars"

    @Column(nullable = false, length = 500)
    private String shortDescription;

    @Column(nullable = false, length = 2500)
    private String fullDescription;

    @Column(name = "event_date")
    private String eventDate; // e.g. "Nov 15, 2026"

    @Column(name = "event_time")
    private String eventTime; // e.g. "10:00 AM - 4:00 PM"

    @Column(name = "event_date_time")
    private LocalDateTime eventDateTime; // For countdown timer calculations

    @Column(name = "venue")
    private String venue; // e.g. "Main Auditorium Hall A"

    @Column(name = "organizer")
    private String organizer; // e.g. "Robotics Society"

    @Column(name = "badge")
    private String badge; // e.g. "Cash Prize: $5,000", "Free Entry"

    @Column(name = "color_accent")
    private String colorAccent = "sky";

    @Column(name = "max_capacity")
    private Integer maxCapacity = 0; // 0 or null = Unlimited

    public EventCard() {
    }

    public EventCard(String title, String category, String shortDescription, String fullDescription,
                     String eventDate, String eventTime, String venue, String organizer, String badge, String colorAccent) {
        this.title = title;
        this.category = category;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.organizer = organizer;
        this.badge = badge;
        this.colorAccent = colorAccent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getEventDateTime() {
        if (this.eventDateTime != null) {
            return this.eventDateTime;
        }
        this.eventDateTime = parseDateTimeFromStrings(this.eventDate, this.eventTime);
        return this.eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public static LocalDateTime parseDateTimeFromStrings(String dateStr, String timeStr) {
        LocalDate date = null;
        if (dateStr != null && !dateStr.isBlank()) {
            String rawDate = dateStr.trim();
            try {
                date = LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {
                try {
                    date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));
                } catch (Exception ignored2) {
                    try {
                        date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH));
                    } catch (Exception ignored3) {
                        try {
                            date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        } catch (Exception ignored4) {
                            try {
                                date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                            } catch (Exception ignored5) {}
                        }
                    }
                }
            }
        }
        if (date == null) {
            date = LocalDate.now().plusDays(7);
        }

        LocalTime time = null;
        if (timeStr != null && !timeStr.isBlank()) {
            String rawTime = timeStr.trim();
            if (rawTime.contains("-")) {
                rawTime = rawTime.split("-")[0].trim();
            }
            try {
                time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                try {
                    time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
                } catch (Exception ignored2) {
                    try {
                        time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
                    } catch (Exception ignored3) {}
                }
            }
        }
        if (time == null) {
            time = LocalTime.of(10, 0);
        }

        return LocalDateTime.of(date, time);
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getColorAccent() {
        return colorAccent;
    }

    public void setColorAccent(String colorAccent) {
        this.colorAccent = colorAccent;
    }

    public String getIsoDate() {
        LocalDateTime dt = getEventDateTime();
        return (dt != null) ? dt.toLocalDate().toString() : "";
    }

    public String getIsoStartTime() {
        LocalDateTime dt = getEventDateTime();
        return (dt != null) ? dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "10:00";
    }

    public String getIsoEndTime() {
        if (eventTime != null && eventTime.contains("-")) {
            String[] parts = eventTime.split("-");
            if (parts.length > 1) {
                String endPart = parts[1].trim();
                try {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
                    LocalTime t = LocalTime.parse(endPart, f);
                    return t.format(DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception ignored) {
                    try {
                        DateTimeFormatter f = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                        LocalTime t = LocalTime.parse(endPart, f);
                        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ignored2) {
                        try {
                            LocalTime t = LocalTime.parse(endPart, DateTimeFormatter.ofPattern("HH:mm"));
                            return t.format(DateTimeFormatter.ofPattern("HH:mm"));
                        } catch (Exception ignored3) {}
                    }
                }
            }
        }
        LocalDateTime dt = getEventDateTime();
        return (dt != null) ? dt.plusHours(2).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public Integer getMaxCapacity() {
        return maxCapacity != null ? maxCapacity : 0;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = (maxCapacity != null) ? maxCapacity : 0;
    }

    public boolean isFull(long currentRegistrations) {
        return maxCapacity != null && maxCapacity > 0 && currentRegistrations >= maxCapacity;
    }

    public int getRemainingSeats(long currentRegistrations) {
        if (maxCapacity == null || maxCapacity <= 0) {
            return -1; // unlimited
        }
        return Math.max(0, (int) (maxCapacity - currentRegistrations));
    }
}
