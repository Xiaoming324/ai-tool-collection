package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.TravelItinerary;

import java.time.LocalDate;
import java.util.List;

public interface ITravelItineraryService extends IService<TravelItinerary> {

    TravelItinerary saveItinerary(Long userId,
                                  Long sessionId,
                                  String chatId,
                                  String title,
                                  String destination,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  String itineraryContent);

    List<TravelItinerary> listByUserId(Long userId);

    TravelItinerary getByIdAndUserId(Long itineraryId, Long userId);

    void removeBySessionId(Long sessionId);
}
