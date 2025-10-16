package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Path filePath = getFilePathFromUser(args);

        if (filePath == null) {
            logger.error("Ingen fil valdes. Avslutar.");
            System.exit(1);
        }

        logger.info("Startar import från: {}", filePath.getFileName());
        List<Order> validOrders = processFile(filePath);
        generateSummary(validOrders, filePath);
    }

    /**
     * Försök få fil från argument, annars visa meny och låt användaren välja
     */
    private static Path getFilePathFromUser(String[] args) {
        Path inputPath = null;

        // 1. Om användaren angav en fil i argument
        if (args.length > 0) {
            inputPath = Paths.get(args[0]);
            if (Files.exists(inputPath)) {
                return inputPath;
            } else {
                logger.warn("Angiven fil hittades inte: {}", inputPath);
            }
        }

        // 2. Leta efter filer i incoming/
        Path incomingDir = Paths.get("incoming");
        List<Path> validFiles = new ArrayList<>();

        if (!Files.exists(incomingDir)) {
            logger.error("Mappen 'incoming/' finns inte.");
            return null;
        }

        if (!Files.isDirectory(incomingDir)) {
            logger.error("incoming/ är inte en mapp.");
            return null;
        }

        // Samla alla giltiga filer
        try (Stream<Path> files = Files.list(incomingDir)) {
            validFiles = files
                    .filter(Files::isRegularFile)
                    .filter(Main::looksLikeOrderFile)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            logger.error("Fel vid läsning av mappen incoming/", e);
            return null;
        }

        if (validFiles.isEmpty()) {
            logger.error("Inga giltiga filer funna i 'incoming/'. Stödjer ; -separerade filer (CSV/TEXT) med header eller data i format: orderId;customerId;amount");
            return null;
        }

        // Visa meny
        Scanner scanner = new Scanner(System.in);
        logger.info("Hittade {} giltig(a) fil(er) i 'incoming/':", validFiles.size());
        System.out.println("==================================================");
        for (int i = 0; i < validFiles.size(); i++) {
            Path file = validFiles.get(i);
            try (BufferedReader br = Files.newBufferedReader(file)) {
                String firstLine = br.readLine();
                System.out.printf("[%d] %s  | Förlagd rad: %s%n", i + 1, file.getFileName(), firstLine != null ? firstLine : "tom");
            } catch (Exception e) {
                System.out.printf("[%d] %s  | Förlagd rad: läsning misslyckades%n", i + 1, file.getFileName());
            }
        }
        System.out.println("==================================================");

        // Be om val
        while (true) {
            System.out.printf("Välj en fil (1–%d) eller 0 för att avsluta: ", validFiles.size());
            if (!scanner.hasNextInt()) {
                System.out.println("Ange en siffra!");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();
            if (choice == 0) {
                System.out.println("Avslutar.");
                return null;
            }
            if (choice >= 1 && choice <= validFiles.size()) {
                return validFiles.get(choice - 1);
            }
            System.out.println("Ogiltigt val. Försök igen.");
        }
    }

    /**
     * Kollar om filen ser ut som en orderfil
     */
    private static boolean looksLikeOrderFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = null;
            int linesTested = 0;
            while (linesTested < 5 && (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";", -1);
                if (parts.length != 3) continue;

                try {
                    Integer.parseInt(parts[0].trim());  // orderId
                    Integer.parseInt(parts[1].trim());  // customerId
                    Double.parseDouble(parts[2].trim()); // amount
                    return true;
                } catch (NumberFormatException e) {
                    // fortsätt testa nästa rad
                }
                linesTested++;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validera och parsa varje rad
     */
    private static Order parseOrderLine(String line) {
        try {
            String[] parts = line.split(";", -1);
            if (parts.length != 3) {
                throw new InvalidOrderFormatException("För många eller för få fält: " + parts.length + " (förväntat: 3)");
            }

            String orderIdStr = parts[0].trim();
            String customerIdStr = parts[1].trim();
            String amountStr = parts[2].trim();

            if (orderIdStr.isEmpty() || customerIdStr.isEmpty() || amountStr.isEmpty()) {
                throw new InvalidOrderFormatException("Tomma fält tillåts inte");
            }

            int orderId = Integer.parseInt(orderIdStr);
            int customerId = Integer.parseInt(customerIdStr);
            double amount = Double.parseDouble(amountStr);

            if (amount < 0) {
                throw new InvalidOrderFormatException("Belopp kan inte vara negativt: " + amount);
            }

            return new Order(orderId, customerId, amount);
        } catch (NumberFormatException e) {
            logger.warn("Ogiltigt nummerformat i rad: '{}'. Förväntar sig numeriska värden.", line);
            return null;
        } catch (InvalidOrderFormatException e) {
            logger.warn("Ogiltig rad: '{}'. Orsak: {}", line, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("Okänt fel vid parsning av rad: '{}'.", line);
            return null;
        }
    }

    public static List<Order> processFile(Path filePath) {
        List<Order> validOrders = new ArrayList<>();
        logger.info("Startar läsning av fil: {}", filePath.getFileName());

        try (Stream<String> lines = Files.lines(filePath)) {
            validOrders = lines
                    .peek(line -> logger.debug("Bearbetar rad: {}", line))
                    .map(Main::parseOrderLine)
                    .filter(Objects::nonNull)
                    .toList();

            logger.info("Import klar: {} giltiga ordrar importerade från {}", validOrders.size(), filePath.getFileName());
        } catch (NoSuchFileException e) {
            logger.error("Filen hittades inte: {}", filePath, e);
        } catch (AccessDeniedException e) {
            logger.error("Behörighet nekad: {}", filePath, e);
        } catch (IOException e) {
            logger.error("IO-fel vid läsning: {}", filePath, e);
        } catch (Exception e) {
            logger.error("Okänt fel vid import: {}", filePath, e);
        }

        return validOrders;
    }

    private static void generateSummary(List<Order> validOrders, Path filePath) {
        double total = validOrders.stream().mapToDouble(Order::getAmount).sum();
        double average = validOrders.stream().mapToDouble(Order::getAmount).average().orElse(0.0);
        long uniqueCustomers = validOrders.stream().map(Order::getCustomerId).distinct().count();

        logger.info("==== SAMMANFATTNING för {} ====", filePath.getFileName());
        logger.info("Totalt giltiga ordrar: {}", validOrders.size());
        logger.info("Totalt belopp: {} SEK", String.format("%.2f", total));
        logger.info("Genomsnittligt belopp: {} SEK", String.format("%.2f", average));
        logger.info("Antal unika kunder: {}", uniqueCustomers);
        logger.info("==================================");
    }
}