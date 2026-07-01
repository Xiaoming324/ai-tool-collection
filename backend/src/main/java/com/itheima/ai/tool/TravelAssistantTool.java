package com.itheima.ai.tool;

import tools.jackson.databind.JsonNode;
import com.itheima.ai.entity.po.TravelItinerary;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.ITravelItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TravelAssistantTool {

    private static final int DEFAULT_FORECAST_DAYS = 3;
    private static final int MAX_FORECAST_DAYS = 7;
    private static final int DEFAULT_ATTRACTION_LIMIT = 8;

    private final ITravelItineraryService travelItineraryService;
    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent", "AI-Tool-Collection/1.0 (https://github.com/xiaoming-ma/ai-tool-collection)")
            .build();

    @Tool(description = "Get a destination guide summary for a city, region, or country")
    public String getDestinationGuide(@ToolParam(description = "Destination name in English, such as Tokyo or Paris or Beijing") String place) {
        if (!StringUtils.hasText(place)) {
            return "Destination name is required.";
        }

        try {
            String encodedPlace = URLEncoder.encode(place.trim(), StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encodedPlace;
            JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (node == null || !node.hasNonNull("extract")) {
                return "No destination guide was found for " + place + ".";
            }

            String title = node.path("title").asText(place);
            String extract = node.path("extract").asText();
            String description = node.path("description").asText();

            return """
                    Destination: %s
                    Description: %s
                    Guide summary: %s
                    """.formatted(
                    title,
                    StringUtils.hasText(description) ? description : "N/A",
                    extract
            ).trim();
        } catch (Exception e) {
            log.error("getDestinationGuide failed for place={}", place, e);
            return "Failed to fetch destination guide for " + place + ".";
        }
    }

    @Tool(description = "Search nearby attractions for a destination")
    public String searchAttractions(@ToolParam(description = "City or destination name in English, such as Kyoto or New York or Shanghai") String city) {
        if (!StringUtils.hasText(city)) {
            return "Destination name is required.";
        }

        try {
            GeoLocation geoLocation = geocode(city);
            String url = UriComponentsBuilder.fromUriString("https://en.wikipedia.org/w/api.php")
                    .queryParam("action", "query")
                    .queryParam("list", "geosearch")
                    .queryParam("gscoord", geoLocation.latitude() + "|" + geoLocation.longitude())
                    .queryParam("gsradius", 10000)
                    .queryParam("gslimit", DEFAULT_ATTRACTION_LIMIT)
                    .queryParam("format", "json")
                    .queryParam("origin", "*")
                    .toUriString();

            JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            JsonNode places = node == null ? null : node.path("query").path("geosearch");
            if (places == null || !places.isArray() || places.isEmpty()) {
                return "No nearby attractions were found for " + city + ".";
            }

            StringJoiner joiner = new StringJoiner("\n");
            joiner.add("Nearby attractions for " + geoLocation.displayName() + ":");
            int index = 1;
            for (JsonNode placeNode : places) {
                joiner.add(index++ + ". " + placeNode.path("title").asText("Unknown")
                        + " (" + placeNode.path("dist").asText("?") + " meters away)");
            }
            return joiner.toString();
        } catch (Exception e) {
            log.error("searchAttractions failed for city={}", city, e);
            return "Failed to search attractions for " + city + ".";
        }
    }

    @Tool(description = "Get weather forecast for a destination")
    public String getWeather(
            @ToolParam(description = "City or destination name in English, such as Tokyo or Beijing or Shanghai") String city,
            @ToolParam(description = "Trip start date in yyyy-MM-dd format. Optional.", required = false) String startDate,
            @ToolParam(description = "How many days to check, from 1 to 7. Optional.", required = false) Integer days) {
        if (!StringUtils.hasText(city)) {
            return "Destination name is required.";
        }

        try {
            GeoLocation geoLocation = geocode(city);
            int forecastDays = normalizeForecastDays(days);
            String url = buildWeatherUrl(geoLocation, startDate, forecastDays);
            JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            JsonNode daily = node == null ? null : node.path("daily");
            JsonNode dates = daily == null ? null : daily.path("time");
            if (dates == null || !dates.isArray() || dates.isEmpty()) {
                return "No weather forecast was found for " + city + ".";
            }

            StringJoiner joiner = new StringJoiner("\n");
            joiner.add("Weather forecast for " + geoLocation.displayName() + ":");
            for (int i = 0; i < dates.size(); i++) {
                String line = "%s: %s, max %s°C, min %s°C, precipitation probability %s%%"
                        .formatted(
                                dates.path(i).asText(),
                                weatherCodeToText(daily.path("weather_code").path(i).asInt()),
                                daily.path("temperature_2m_max").path(i).asText("?"),
                                daily.path("temperature_2m_min").path(i).asText("?"),
                                daily.path("precipitation_probability_max").path(i).asText("?")
                        );
                joiner.add(line);
            }
            return joiner.toString();
        } catch (Exception e) {
            log.error("getWeather failed for city={}", city, e);
            return "Failed to fetch weather for " + city + ".";
        }
    }

    @Tool(description = "Get country information for travel planning")
    public String getCountryInfo(@ToolParam(description = "Country name, such as Japan or France") String country) {
        if (!StringUtils.hasText(country)) {
            return "Country name is required.";
        }

        try {
            String encodedCountry = URLEncoder.encode(country.trim(), StandardCharsets.UTF_8);
            String url = "https://restcountries.com/v3.1/name/" + encodedCountry;
            JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (node == null || !node.isArray() || node.isEmpty()) {
                return "No country information was found for " + country + ".";
            }

            JsonNode countryNode = node.path(0);
            String officialName = countryNode.path("name").path("official").asText(country);
            String capital = firstArrayText(countryNode.path("capital"));
            String region = countryNode.path("region").asText("N/A");
            String subregion = countryNode.path("subregion").asText("N/A");
            String currencies = joinFieldNames(countryNode.path("currencies"));
            String languages = joinFieldValues(countryNode.path("languages"));
            String timezones = joinArray(countryNode.path("timezones"));

            return """
                    Country: %s
                    Capital: %s
                    Region: %s / %s
                    Currencies: %s
                    Languages: %s
                    Timezones: %s
                    """.formatted(
                    officialName,
                    StringUtils.hasText(capital) ? capital : "N/A",
                    region,
                    subregion,
                    StringUtils.hasText(currencies) ? currencies : "N/A",
                    StringUtils.hasText(languages) ? languages : "N/A",
                    StringUtils.hasText(timezones) ? timezones : "N/A"
            ).trim();
        } catch (Exception e) {
            log.error("getCountryInfo failed for country={}", country, e);
            return "Failed to fetch country information for " + country + ".";
        }
    }

    @Tool(description = "Get exchange rate between two currencies")
    public String getExchangeRate(
            @ToolParam(description = "Base currency code, such as USD or CNY") String fromCurrency,
            @ToolParam(description = "Target currency code, such as JPY or EUR") String toCurrency,
            @ToolParam(description = "Amount to convert. Optional.", required = false) BigDecimal amount) {
        if (!StringUtils.hasText(fromCurrency) || !StringUtils.hasText(toCurrency)) {
            return "Both currency codes are required.";
        }

        BigDecimal normalizedAmount = amount == null ? BigDecimal.ONE : amount;
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.frankfurter.app/latest")
                    .queryParam("from", fromCurrency.trim().toUpperCase(Locale.ROOT))
                    .queryParam("to", toCurrency.trim().toUpperCase(Locale.ROOT))
                    .queryParam("amount", normalizedAmount)
                    .toUriString();

            JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (node == null || !node.has("rates")) {
                return "No exchange rate was found.";
            }

            JsonNode rates = node.path("rates");
            String targetCurrency = toCurrency.trim().toUpperCase(Locale.ROOT);
            if (!rates.has(targetCurrency)) {
                return "No exchange rate was found for " + fromCurrency + " to " + toCurrency + ".";
            }

            BigDecimal converted = rates.path(targetCurrency).decimalValue().setScale(2, RoundingMode.HALF_UP);
            return "%s %s = %s %s (date: %s)".formatted(
                    normalizedAmount.stripTrailingZeros().toPlainString(),
                    fromCurrency.trim().toUpperCase(Locale.ROOT),
                    converted.toPlainString(),
                    targetCurrency,
                    node.path("date").asText("N/A")
            );
        } catch (Exception e) {
            log.error("getExchangeRate failed for {}→{}", fromCurrency, toCurrency, e);
            return "Failed to fetch exchange rate from " + fromCurrency + " to " + toCurrency + ".";
        }
    }

    @Tool(description = "Save a travel itinerary for the current user")
    public String saveItinerary(
            @ToolParam(description = "A concise itinerary title") String title,
            @ToolParam(description = "Main destination, such as Japan or Paris") String destination,
            @ToolParam(description = "Trip start date in yyyy-MM-dd format. Optional.", required = false) String startDate,
            @ToolParam(description = "Trip end date in yyyy-MM-dd format. Optional.", required = false) String endDate,
            @ToolParam(description = "The full itinerary content to save") String itineraryContent,
            ToolContext toolContext) {
        try {
            LocalDate parsedStartDate = parseDate(startDate);
            LocalDate parsedEndDate = parseDate(endDate);
            Long userId = getRequiredLong(toolContext, "userId");
            Long sessionId = getRequiredLong(toolContext, "sessionId");
            String chatId = getRequiredString(toolContext, "chatId");

            TravelItinerary itinerary = travelItineraryService.saveItinerary(
                    userId,
                    sessionId,
                    chatId,
                    title,
                    destination,
                    parsedStartDate,
                    parsedEndDate,
                    itineraryContent
            );
            return "Saved itinerary successfully. itineraryId=%s, title=%s".formatted(itinerary.getId(), itinerary.getTitle());
        } catch (BusinessException e) {
            return "Failed to save itinerary: " + e.getMessage();
        } catch (Exception e) {
            return "Failed to save itinerary.";
        }
    }

    @Tool(description = "List saved travel itineraries for the current user")
    public String listMyTrips(ToolContext toolContext) {
        try {
            Long userId = getRequiredLong(toolContext, "userId");
            List<TravelItinerary> itineraries = travelItineraryService.listByUserId(userId);
            if (itineraries.isEmpty()) {
                return "No saved itineraries were found.";
            }

            StringJoiner joiner = new StringJoiner("\n");
            joiner.add("Saved itineraries:");
            itineraries.stream()
                    .limit(10)
                    .forEach(itinerary -> joiner.add(
                            "- id=%s, title=%s, destination=%s, dates=%s to %s".formatted(
                                    itinerary.getId(),
                                    itinerary.getTitle(),
                                    itinerary.getDestination(),
                                    Objects.toString(itinerary.getStartDate(), "N/A"),
                                    Objects.toString(itinerary.getEndDate(), "N/A")
                            )));
            return joiner.toString();
        } catch (Exception e) {
            return "Failed to list saved itineraries.";
        }
    }

    @Tool(description = "Get one saved itinerary by id for the current user")
    public String getTripDetail(@ToolParam(description = "Saved itinerary id") Long itineraryId,
                                ToolContext toolContext) {
        if (itineraryId == null) {
            return "Itinerary id is required.";
        }

        try {
            Long userId = getRequiredLong(toolContext, "userId");
            TravelItinerary itinerary = travelItineraryService.getByIdAndUserId(itineraryId, userId);
            if (itinerary == null) {
                return "Itinerary was not found.";
            }

            return """
                    Itinerary id: %s
                    Title: %s
                    Destination: %s
                    Start date: %s
                    End date: %s
                    Content:
                    %s
                    """.formatted(
                    itinerary.getId(),
                    itinerary.getTitle(),
                    itinerary.getDestination(),
                    Objects.toString(itinerary.getStartDate(), "N/A"),
                    Objects.toString(itinerary.getEndDate(), "N/A"),
                    itinerary.getItineraryContent()
            ).trim();
        } catch (Exception e) {
            return "Failed to fetch itinerary details.";
        }
    }

    private GeoLocation geocode(String city) {
        String url = UriComponentsBuilder.fromUriString("https://geocoding-api.open-meteo.com/v1/search")
                .queryParam("name", city.trim())
                .queryParam("count", 1)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();

        JsonNode node = restClient.get().uri(url).retrieve().body(JsonNode.class);
        JsonNode results = node == null ? null : node.path("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new BusinessException("Destination not found");
        }

        JsonNode result = results.path(0);
        String name = result.path("name").asText(city);
        String country = result.path("country").asText("");
        return new GeoLocation(
                result.path("latitude").asDouble(),
                result.path("longitude").asDouble(),
                StringUtils.hasText(country) ? name + ", " + country : name
        );
    }

    private String buildWeatherUrl(GeoLocation geoLocation, String startDate, int forecastDays) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", geoLocation.latitude())
                .queryParam("longitude", geoLocation.longitude())
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                .queryParam("timezone", "auto");

        LocalDate parsedStartDate = parseDate(startDate);
        if (parsedStartDate != null) {
            builder.queryParam("start_date", parsedStartDate)
                    .queryParam("end_date", parsedStartDate.plusDays(forecastDays - 1L));
        } else {
            builder.queryParam("forecast_days", forecastDays);
        }
        return builder.toUriString();
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("Invalid date format, expected yyyy-MM-dd");
        }
    }

    private int normalizeForecastDays(Integer days) {
        if (days == null || days <= 0) {
            return DEFAULT_FORECAST_DAYS;
        }
        return Math.min(days, MAX_FORECAST_DAYS);
    }

    private Long getRequiredLong(ToolContext toolContext, String key) {
        Object value = getRequiredValue(toolContext, key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String getRequiredString(ToolContext toolContext, String key) {
        Object value = getRequiredValue(toolContext, key);
        return value.toString();
    }

    private Object getRequiredValue(ToolContext toolContext, String key) {
        if (toolContext == null || toolContext.getContext() == null) {
            throw new BusinessException("Tool context is missing");
        }
        Map<String, Object> context = toolContext.getContext();
        Object value = context.get(key);
        if (value == null) {
            throw new BusinessException("Tool context key is missing: " + key);
        }
        return value;
    }

    private String weatherCodeToText(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    private String firstArrayText(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return null;
        }
        return node.path(0).asText();
    }

    private String joinArray(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (JsonNode child : node) {
            joiner.add(child.asText());
        }
        return joiner.toString();
    }

    private String joinFieldNames(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(", ");
        node.propertyNames().forEach(joiner::add);
        return joiner.toString();
    }

    private String joinFieldValues(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(", ");
        node.properties().forEach(entry -> joiner.add(entry.getValue().asText()));
        return joiner.toString();
    }

    private record GeoLocation(double latitude, double longitude, String displayName) {
    }
}
