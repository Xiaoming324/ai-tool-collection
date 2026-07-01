package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.TravelItinerary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TravelItineraryVO {

    private Long id;

    private String chatId;

    private String title;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private String itineraryContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TravelItineraryVO(TravelItinerary itinerary) {
        this.id = itinerary.getId();
        this.chatId = itinerary.getChatId();
        this.title = itinerary.getTitle();
        this.destination = itinerary.getDestination();
        this.startDate = itinerary.getStartDate();
        this.endDate = itinerary.getEndDate();
        this.itineraryContent = itinerary.getItineraryContent();
        this.createdAt = itinerary.getCreatedAt();
        this.updatedAt = itinerary.getUpdatedAt();
    }
}
