package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.PaymentService;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "cinema", name = "book-pay", description = "Process payment for a booking (UC-20)")
@Service
public class PayBookingCommand implements Action {

    @Reference
    private PaymentService paymentService;

    @Argument(index = 0, name = "bookingId", description = "ID of the booking", required = true)
    private int bookingId;

    @Argument(index = 1, name = "method", description = "Payment Method (Card, Cash, E-Wallet, Bank Transfer)", required = true)
    private String method;

    @Argument(index = 2, name = "card", description = "Card number (only if method is Card)", required = false)
    private String card;

    @Argument(index = 3, name = "expiry", description = "Card expiry date MM/YY (only if method is Card)", required = false)
    private String expiry;

    @Argument(index = 4, name = "cvv", description = "Card CVV (only if method is Card)", required = false)
    private String cvv;

    @Override
    public Object execute() throws Exception {
        boolean success;

        // Normalize method input to avoid case issues (e.g., "card" vs "Card")
        String selectedMethod = method.trim();

        if ("Card".equalsIgnoreCase(selectedMethod)) {
            // --- MANDATORY CHECK FOR CARD DETAILS ---
            if (card == null || card.trim().isEmpty() || expiry == null || expiry.trim().isEmpty() || cvv == null
                    || cvv.trim().isEmpty()) {
                System.out.println("\n[ERROR] Card payment requires a card number, expiry date, and CVV.");
                System.out.println("USAGE: cinema:book-pay " + bookingId + " Card <card_number> <expiry_date> <cvv>");
                return null;
            }

            // Simulating processing with the card number provided
            success = paymentService.processCardPayment(bookingId, card, expiry, cvv);

        } else if ("Cash".equalsIgnoreCase(selectedMethod) ||
                "E-Wallet".equalsIgnoreCase(selectedMethod) ||
                "Bank Transfer".equalsIgnoreCase(selectedMethod)) {

            // Handles methods that don't need upfront card numbers
            success = paymentService.processOtherPayment(bookingId, selectedMethod);

        } else {
            System.out.println("[ERROR] Unknown payment method: " + method);
            System.out.println("Valid methods: Card, Cash, E-Wallet, Bank Transfer");
            return null;
        }

        // --- UC-21: Confirmation Output ---
        if (success) {
            // UC-21: System generates reference and shows confirmation
            paymentService.displayConfirmation(bookingId);
        } else {
            System.out.println("Payment failed. Please try again.");
        }
        return null;
    }

}