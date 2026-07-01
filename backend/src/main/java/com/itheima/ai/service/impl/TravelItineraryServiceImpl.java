package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.TravelItinerary;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.mapper.TravelItineraryMapper;
import com.itheima.ai.service.ITravelItineraryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravelItineraryServiceImpl extends ServiceImpl<TravelItineraryMapper, TravelItinerary>
        implements ITravelItineraryService {

    @Override
    public TravelItinerary saveItinerary(Long userId,
                                         Long sessionId,
                                         String chatId,
                                         String title,
                                         String destination,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         String itineraryContent) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("Itinerary title is required");
        }
        if (!StringUtils.hasText(destination)) {
            throw new BusinessException("Destination is required");
        }
        if (!StringUtils.hasText(itineraryContent)) {
            throw new BusinessException("Itinerary content is required");
        }

        TravelItinerary itinerary = new TravelItinerary()
                .setUserId(userId)
                .setSessionId(sessionId)
                .setChatId(chatId)
                .setTitle(title)
                .setDestination(destination)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setItineraryContent(itineraryContent)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        save(itinerary);
        return itinerary;
    }

    @Override
    public List<TravelItinerary> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(TravelItinerary::getUserId, userId)
                .orderByDesc(TravelItinerary::getCreatedAt)
                .list();
    }

    @Override
    public TravelItinerary getByIdAndUserId(Long itineraryId, Long userId) {
        return lambdaQuery()
                .eq(TravelItinerary::getId, itineraryId)
                .eq(TravelItinerary::getUserId, userId)
                .one();
    }

    @Override
    public void removeBySessionId(Long sessionId) {
        lambdaUpdate()
                .eq(TravelItinerary::getSessionId, sessionId)
                .remove();
    }
}
