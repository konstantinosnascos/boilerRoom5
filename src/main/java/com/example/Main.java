package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("No file path provided. Usage: java Main<filepath>");
            System.exit(1);
        }
        Path filePath = Paths.get(args[0]);


        List<Order> validOrders = processFile(filePath);
        if (validOrders != null)
        {
            generateSummary(validOrders, filePath);
        }
    }

    private static Order parseOrderLine(String line)
    {
        try
        {
            String[] parts = line.split(";", -1);
            if (parts.length != 3)
            {
                throw new InvalidOrderFormatException(
                        "Incorrect number of fields: expected 3, got " +parts.length);
            }

            String orderIdStr = parts[0].trim();
            String customerIdStr = parts[1].trim();
            String amountStr = parts[2].trim();

            if (orderIdStr.isEmpty() || customerIdStr.isEmpty() || amountStr.isEmpty())
            {
                throw new InvalidOrderFormatException("Empty field(s) not allowed.");
            }

            int orderId = Integer.parseInt(orderIdStr);
            int customerId = Integer.parseInt(customerIdStr);
            double amount = Double.parseDouble(amountStr);

            if (amount < 0)
            {
                throw new InvalidOrderFormatException("Amount cannot be negative: " + amount);
            }

            return new Order(orderId, customerId, amount);

        } catch (NumberFormatException | InvalidOrderFormatException e)
        {
            logger.warn("Skipping invalid line: '{}'. Reason: {}", line, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("Unexpected error parsing line: '{}'. Reason: {}", line, e.getMessage());
            return null;
        }
    }
    private static void generateSummary(List<Order> validOrders, Path filePath)
    {
        double total = validOrders.stream().mapToDouble(Order::getAmount).sum();
        double average = validOrders.stream().mapToDouble(Order::getAmount).average().orElse(0.0);
        long uniqueCustomers = validOrders.stream().map(Order::getCustomerId).distinct().count();

        logger.info("==== SUMMARY for {} ====", filePath.getFileName());
        logger.info("Total valid orders: {}", validOrders.size());
        logger.info("Total amount: {} SEK", String.format("%.2f", total));
        logger.info("Average order value: {} SEK", String.format("%.2f", average));
        logger.info("Unique customers: {}", uniqueCustomers);
        logger.info("============================");

    }
    public static List<Order> processFile(Path filePath) {
        List<Order> validOrders = new ArrayList<>();
        logger.info("Starting import for file: {}", filePath);

        try (Stream<String> lines = Files.lines(filePath)) {
            validOrders = lines
                    .peek(line -> logger.debug("Processing line: {}", line))
                    .map(Main::parseOrderLine)
                    .filter(Objects::nonNull)
                    .toList();

            logger.info("Import completed: {} valid orders imported from {}", validOrders.size(), filePath.getFileName());

        } catch (NoSuchFileException e) {
            logger.error("File not found: {}", filePath, e);
        } catch (AccessDeniedException e) {
            logger.error("Access denied to file: {}", filePath, e);
        } catch (IOException e) {
            logger.error("I/O error reading file: {}", filePath, e);
        } catch (Exception e) {
            logger.error("Unexpected error during import of file: {}", filePath, e);
        }

        return validOrders;
    }
}