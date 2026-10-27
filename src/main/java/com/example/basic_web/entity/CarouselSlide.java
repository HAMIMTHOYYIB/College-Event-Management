package com.example.basicweb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "carousel_slides")
public class CarouselSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "badge_text")
    private String badgeText;

    @Column(name = "color_theme")
    private String colorTheme = "sky"; // sky, blue, indigo, teal

    @Column(name = "display_order")
    private Integer displayOrder = 1;

    public CarouselSlide() {
    }

    public CarouselSlide(String tag, String title, String description, String badgeText, String colorTheme, Integer displayOrder) {
        this.tag = tag;
        this.title = title;
        this.description = description;
        this.badgeText = badgeText;
        this.colorTheme = colorTheme;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(String colorTheme) {
        this.colorTheme = colorTheme;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
