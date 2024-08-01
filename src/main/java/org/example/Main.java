package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar IdeaPlatformTest-1.0-SNAPSHOT.jar <path_to_json_file>");
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
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

            for (Flight flight : vvoToTlvFlights) {
                long flightDuration = calculateFlightDuration(dateTimeFormat, flight);
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

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static long calculateFlightDuration(SimpleDateFormat dateTimeFormat, Flight flight) throws ParseException {
        if (flight.getDepartureDate() == null || flight.getDepartureTime() == null ||
                flight.getArrivalDate() == null || flight.getArrivalTime() == null) {
            return -1;
        }

        Date departure = dateTimeFormat.parse(flight.getDepartureDate() + " " + flight.getDepartureTime());
        Date arrival = dateTimeFormat.parse(flight.getArrivalDate() + " " + flight.getArrivalTime());
        return arrival.getTime() - departure.getTime();
    }

    private static String formatDuration(long durationMillis) {
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "ч " + minutes + "мин";
    }
}