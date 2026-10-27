package com.example.basicweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
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
}
