package org.example.cli;

import org.example.engine.UserStore;
import org.example.shared.Action;
import org.example.shared.Currency;
import org.example.shared.Side;
import org.example.shared.Trade;

import java.time.Clock;
import java.util.Scanner;

public final class Cli {

    private final UserStore userStore;

    public Cli(final UserStore userStore) {
        this.userStore = userStore;
    }

    public void awaitCommands() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("CLI starting");
        System.out.println("Available commands: add, amend, cancel, list, quit");
        while (true) {
            System.out.println("> ");
            final String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exiting CLI. Goodbye!");
                break;
            }

            switch (input.toLowerCase()) {
                case "add":
                    addTrade(scanner);
                    break;
                case "amend":
                    amendTrade(scanner);
                    break;
                case "cancel":
                    cancelTrade(scanner);
                    break;
                case "list":
                    listTrades();
                    break;
                default:
                    System.out.println("Unknown command. Available commands: add, amend, cancel, list, quit");
            }
        }
        scanner.close();
    }

    private void listTrades() {
        userStore.getAllPnLData();
    }

    private void cancelTrade(final Scanner scanner) {
        System.out.println("Cancelling a trade...");

        try {
            final String user = promptInput(scanner, "org.example.engine.User");
            final String account = promptInput(scanner, "org.example.engine.Account");
            final String portfolio = promptInput(scanner, "org.example.engine.Portfolio");
            final String strategy = promptInput(scanner, "org.example.engine.Strategy");
            final String bbgCode = promptInput(scanner, "BBGCode");
            final String tradeId = promptInput(scanner, "org.example.shared.Trade ID");
            final Currency currency = Currency.from(promptInput(scanner, "org.example.shared.Currency").toUpperCase());
            final Side side = Side.from(promptInput(scanner, "Buy or Sell (B/S)").toUpperCase());
            final double price = Double.parseDouble(promptInput(scanner, "Price"));
            final int volume = Integer.parseInt(promptInput(scanner, "Volume"));
            final long timestamp = Clock.systemUTC().millis();

            final Trade trade = new Trade(tradeId, currency, side, price, volume, Action.CANCEL, timestamp);
            userStore.registerTrade(trade, user, account, portfolio, strategy, bbgCode);
            System.out.println("Attempting to cancel trade.");
        } catch (final Exception e) {
            System.err.println("error occurred when cancelling trade because " + e);
        }
    }

    private void amendTrade(final Scanner scanner) {
        System.out.println("Amending a trade...");

        try {
            final String user = promptInput(scanner, "org.example.engine.User");
            final String account = promptInput(scanner, "org.example.engine.Account");
            final String portfolio = promptInput(scanner, "org.example.engine.Portfolio");
            final String strategy = promptInput(scanner, "org.example.engine.Strategy");
            final String bbgCode = promptInput(scanner, "BBGCode");
            final String tradeId = promptInput(scanner, "org.example.shared.Trade ID");
            final Currency currency = Currency.from(promptInput(scanner, "org.example.shared.Currency").toUpperCase());
            final Side side = Side.from(promptInput(scanner, "Buy or Sell (B/S)").toUpperCase());
            final double price = Double.parseDouble(promptInput(scanner, "Price"));
            final int volume = Integer.parseInt(promptInput(scanner, "Volume"));
            final long timestamp = Clock.systemUTC().millis();

            final Trade trade = new Trade(tradeId, currency, side, price, volume, Action.AMEND, timestamp);
            userStore.registerTrade(trade, user, account, portfolio, strategy, bbgCode);
            System.out.println("Attempting to amend trade. If does not exist, will be treated like a new trade");
        } catch (final Exception e) {
            System.err.println("error occurred when amending trade because " + e);
        }
    }

    private void addTrade(final Scanner scanner) {
        System.out.println("Adding a new trade...");

        try {
            final String user = promptInput(scanner, "org.example.engine.User");
            final String account = promptInput(scanner, "org.example.engine.Account");
            final String portfolio = promptInput(scanner, "org.example.engine.Portfolio");
            final String strategy = promptInput(scanner, "org.example.engine.Strategy");
            final String bbgCode = promptInput(scanner, "BBGCode");
            final String tradeId = promptInput(scanner, "org.example.shared.Trade ID");
            final Currency currency = Currency.from(promptInput(scanner, "org.example.shared.Currency").toUpperCase());
            final Side side = Side.from(promptInput(scanner, "Buy or Sell (B/S)").toUpperCase());
            final double price = Double.parseDouble(promptInput(scanner, "Price"));
            final int volume = Integer.parseInt(promptInput(scanner, "Volume"));
            final long timestamp = Clock.systemUTC().millis();

            final Trade trade = new Trade(tradeId, currency, side, price, volume, Action.NEW, timestamp);
            userStore.registerTrade(trade, user, account, portfolio, strategy, bbgCode);
            System.out.println("org.example.shared.Trade added successfully.");
        } catch (final Exception e) {
            System.err.println("error occurred when adding trade because " + e);
        }
    }

    private static String promptInput(final Scanner scanner, final String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }
}
