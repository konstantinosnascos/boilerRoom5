package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    private static final Path CSV_INPUT = Paths.get(
//            "src", "main", "incoming", "orderdata_error.csv");
            "src", "main", "incoming", "orderdata.csv");
    public static void main(String[] args) {

        List<Order> validOrders = new ArrayList<>();
        logger.info("Starting import for file: {}", CSV_INPUT);

        try (Stream<String> lines = Files.lines(CSV_INPUT))
        {

            validOrders = lines
                    .skip(1)
                    .map(Main::parseOrderLine)
                    .filter(Objects::nonNull)
                    .toList();

            logger.info("Import completed: {} valid orders imported.", validOrders.size());

        } catch (NoSuchFileException e)
        {
            logger.error("File not found: {}", CSV_INPUT, e);
        } catch (IOException e)
        {
            logger.error("I/O error reading file: {}", CSV_INPUT, e);
        } catch (Exception e)
        {
            logger.error("Unexpected error during import", e);
        }

        System.out.println(validOrders);
    }

    private static Order parseOrderLine(String line)
    {
        try
        {
            String[] parts = line.split(";");
            if (parts.length != 3)
            {
                throw new InvalidOrderFormatException("Incorrect number of fields.");
            }

            if (parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank())
            {
                throw new InvalidOrderFormatException("Empty field(s).");
            }

            int orderId = Integer.parseInt(parts[0].trim());
            int customerId = Integer.parseInt(parts[1].trim());
            double amount = Double.parseDouble(parts[2].trim());

            if (amount < 0)
            {
                throw new InvalidOrderFormatException("Amount cannot be negative.");
            }

            return new Order(orderId, customerId, amount);

        } catch (InvalidOrderFormatException | NumberFormatException e)
        {
            logger.warn("Skipping invalid line: '{}'. Reason: {}", line, e.getMessage());
            return null;
        }
    }
}