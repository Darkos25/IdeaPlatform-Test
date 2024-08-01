package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar flight-analyzer.jar <path_to_json_file>");
            return;
        }

        String jsonFilePath = args[0];
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TicketResponse ticketResponse = objectMapper.readValue(new File(jsonFilePath), TicketResponse.class);
            List<Flight> flights = ticketResponse.getTickets();

            List<Flight> vvoToTlvFlights = new ArrayList<>();
            for (Flight flight : flights) {
                if ("VVO".equals(flight.getOrigin()) && "TLV".equals(flight.getDestination())) {
                    vvoToTlvFlights.add(flight);
                }
            }

            Map<String, Long> minFlightTimeByCarrier = new HashMap<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");

            for (Flight flight : vvoToTlvFlights) {
                long flightDuration = calculateFlightDuration(dateFormatter, flight);
                if (flightDuration != -1) {
                    minFlightTimeByCarrier.merge(flight.getCarrier(), flightDuration, Math::min);
                }
            }

            System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом для каждого авиаперевозчика:");
            for (Map.Entry<String, Long> entry : minFlightTimeByCarrier.entrySet()) {
                System.out.println("Перевозчик: " + entry.getKey() + ", Время: " + formatDuration(entry.getValue()));
            }

            double totalPrice = 0;
            for (Flight flight : vvoToTlvFlights) {
                totalPrice += flight.getPrice();
            }
            double averagePrice = totalPrice / vvoToTlvFlights.size();

            List<Integer> prices = new ArrayList<>();
            for (Flight flight : vvoToTlvFlights) {
                prices.add(flight.getPrice());
            }
            Collections.sort(prices);
            double medianPrice;
            int size = prices.size();
            if (size % 2 == 0) {
                medianPrice = (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
            } else {
                medianPrice = prices.get(size / 2);
            }

            System.out.println("Разница между средней ценой и медианой для полетов между Владивостоком и Тель-Авивом:");
            System.out.println("Средняя цена: " + averagePrice);
            System.out.println("Медиана: " + medianPrice);
            System.out.println("Разница: " + Math.abs(averagePrice - medianPrice));

        } catch (IOException | DateTimeParseException e) {
            e.printStackTrace();
        }
    }

    private static long calculateFlightDuration(DateTimeFormatter dateFormatter, Flight flight) {
        try {
            if (isNullOrEmpty(flight.getDeparture_date()) || isNullOrEmpty(flight.getDeparture_time()) ||
                    isNullOrEmpty(flight.getArrival_date()) || isNullOrEmpty(flight.getArrival_time())) {
                System.err.println("Пропущены даты или время для рейса " + flight.getCarrier());
                return -1;
            }

            LocalDateTime departure = LocalDateTime.parse(flight.getDeparture_date() + " " + flight.getDeparture_time(), dateFormatter);
            LocalDateTime arrival = LocalDateTime.parse(flight.getArrival_date() + " " + flight.getArrival_time(), dateFormatter);
            return java.time.Duration.between(departure, arrival).toMillis();
        } catch (DateTimeParseException e) {
            System.err.println("Ошибка при разборе даты для рейса " + flight.getCarrier() + ": " + e.getMessage());
            return -1;
        }
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static String formatDuration(long durationMillis) {
        if (durationMillis == -1) {
            return "N/A";
        }
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "ч " + minutes + "мин";
    }
}