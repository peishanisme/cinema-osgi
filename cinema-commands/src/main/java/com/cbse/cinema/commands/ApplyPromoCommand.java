package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.BookingService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;

@Command(scope = "cinema", name = "book-promo", description = "Apply promo code and recalculate price (UC-18 & UC-19)")
@Service
public class ApplyPromoCommand implements Action {

    @Reference
    private BookingService bookingService;

    @Argument(index = 0, name = "bookingId", required = true, description = "The ID of the booking to apply the promo to")
    private int bookingId;

    @Argument(index = 1, name = "promoCode", required = true, description = "The promo code to apply")
    private String promoCode;

    @Override
    public Object execute() throws Exception {
        // 1. Get seats for this booking
        List<Integer> seats = bookingService.getSeatsByBooking(bookingId);
        if (seats.isEmpty()) {
            System.out.println("Error: Booking ID not found.");
            return null;
        }

        // 2. UC-18: Get initial subtotal to check promo eligibility
        // (Temporary calculation just to check min_purchase)
        double subtotal = bookingService.calculateSubtotal(seats);

        // 3. UC-19: Get the discount
        double discount = bookingService.getPromoDiscount(promoCode, subtotal);
        if (discount <= 0) {
            System.out.println("Promo code '" + promoCode + "' could not be applied.");
            return null;
        }

        // 4. UC-18: Recalculate Final Total with the discount applied
        double newTotal = bookingService.calculateFinalTotal(seats, discount);

        // 5. Update the record
        if (bookingService.updateBookingTotal(bookingId, newTotal, promoCode)) {
            System.out.println("SUCCESS: Promo applied. Booking #" + bookingId + " updated to RM " + String.format("%.2f", newTotal));
        }
        
        return null;
    }
}