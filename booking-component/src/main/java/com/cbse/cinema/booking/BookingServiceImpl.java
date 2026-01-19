package com.cbse.cinema.booking;

import java.sql.*;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.cbse.cinema.api.service.BookingService;
import com.cbse.cinema.api.service.DatabaseService;
import com.cbse.cinema.api.service.MovieService;

@Component(service = BookingService.class)
public class BookingServiceImpl implements BookingService {

    @Reference
    private DatabaseService dbService;

    @Reference
    private MovieService movieService;

    @Override
    public double calculateSubtotal(List<Integer> seats) {
        double subtotal = 0.0;
        for (Integer seatId : seats) {
            if (java.util.Arrays.asList(27, 28, 35, 36).contains(seatId))
                subtotal += 25.0;
            else if (seatId >= 56)
                subtotal += 30.0;
            else if (seatId < 8)
                subtotal += 15.0;
            else
                subtotal += 10.0;
        }
        return subtotal;
    }

    @Override
    public double calculateFinalTotal(List<Integer> seats, double discount) {
        final double BOOKING_FEE_RATE = 0.10;
        final double TAX_RATE = 0.10;
        double subtotal = calculateSubtotal(seats);

        // 2. Standard Calculations (Sequential)
        double bookingFee = subtotal * BOOKING_FEE_RATE;

        // UC-18/19 Formula: Tax applies to the net amount after fee and discount
        double taxableAmount = Math.max(0, (subtotal + bookingFee) - discount);
        double tax = taxableAmount * TAX_RATE;

        double total = (subtotal + bookingFee + tax) - discount;

        // 3. Clean Printout
        System.out.println("\n[PRICING BREAKDOWN]");
        System.out.println(String.format("Subtotal    : RM %8.2f", subtotal));
        System.out.println(String.format("Fee (10%%)   : RM %8.2f", bookingFee));
        if (discount > 0) {
            System.out.println(String.format("Discount    : -RM %7.2f", discount));
        }
        System.out.println(String.format("Tax (10%%)   : RM %8.2f", tax));
        System.out.println("----------------------------");
        System.out.println(String.format("TOTAL       : RM %8.2f", Math.max(0, total)));
        System.out.println("----------------------------\n");

        return Math.max(0, total);
    }

    @Override
    public int createBooking(String userId, String sessionId, List<Integer> seatNumbers, double totalPrice, String promoCode) {
        
        // Step 1: Verify that ALL requested seats are currently LOCKED by THIS user
        String checkLockSql = "SELECT COUNT(*) FROM seats " +
                            "WHERE session_id = CAST(? AS TEXT) " +
                            "AND seat_number = ANY(?) " +
                            "AND status = 'LOCKED' " +
                            "AND locked_by = CAST(? AS UUID)";

        try (Connection conn = dbService.getConnection()) {
            Array seatArray = conn.createArrayOf("integer", seatNumbers.toArray());

            try (PreparedStatement checkStmt = conn.prepareStatement(checkLockSql)) {
                checkStmt.setString(1, sessionId);
                checkStmt.setArray(2, seatArray);
                checkStmt.setString(3, userId);
                
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) != seatNumbers.size()) {
                    // Flow: One or more seats are not locked by this user
                    System.out.println("[!] Error: Some seats are no longer reserved for you. Please re-select.");
                    return -1; 
                }
            }

            // Step 2: Proceed with Booking Creation (Now that we know the seats are safe)
            conn.setAutoCommit(false);
            String sql = "INSERT INTO bookings (user_id, session_id, total_price, status, applied_promo) " +
                        "VALUES (CAST(? AS UUID), CAST(? AS TEXT), ?, 'PENDING', ?) RETURNING booking_id";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, sessionId);
                pstmt.setDouble(3, totalPrice);
                pstmt.setString(4, (promoCode == null || promoCode.isEmpty()) ? null : promoCode);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int bookingId = rs.getInt(1);

                    // Step 3: Link seats to booking
                    String linkSql = "INSERT INTO booking_seats (booking_id, seat_number) VALUES (?, ?)";
                    try (PreparedStatement lPstmt = conn.prepareStatement(linkSql)) {
                        for (int seat : seatNumbers) {
                            lPstmt.setInt(1, bookingId);
                            lPstmt.setInt(2, seat);
                            lPstmt.addBatch();
                        }
                        lPstmt.executeBatch();
                    }

                    conn.commit();
                    return bookingId;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getSeatsByBooking(int bookingId) {
        List<Integer> seats = new java.util.ArrayList<>();
        String sql = "SELECT seat_number FROM booking_seats WHERE booking_id = ?";
        try (Connection conn = dbService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seats.add(rs.getInt("seat_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    @Override
    public double getPromoDiscount(String promoCode, double subtotal) {
        // UC-19 Flow: Validate code (active, min purchase)
        String sql = "SELECT discount_type, discount_value, min_purchase FROM promos WHERE promo_code = ? AND is_active = true";
        try (Connection conn = dbService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, promoCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double min = rs.getDouble("min_purchase");
                if (subtotal < min) {
                    System.out.println("Promo not applicable: Min purchase RM" + min + " required.");
                    return 0.0;
                }
                String type = rs.getString("discount_type");
                double val = rs.getDouble("discount_value");
                return "PERCENT".equalsIgnoreCase(type) ? (subtotal * (val / 100.0)) : val;
            }
        } catch (SQLException e) {
            System.out.println("Promo expired or unavailable.");
        }
        return 0.0;
    }

    @Override
    public boolean updateBookingTotal(int bookingId, double newTotal, String promoCode) {
        // UC-19: Update both the price and the recorded promo code
        String sql = "UPDATE bookings SET total_price = ?, applied_promo = ? WHERE booking_id = ?";
        try (Connection conn = dbService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newTotal);
            pstmt.setString(2, promoCode);
            pstmt.setInt(3, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public String cancelBooking(int bookingId) {
        try (Connection conn = dbService.getConnection()) {
            // 1. UC-23 Flow 2: Check Eligibility (Time Window)
            String checkSql = "SELECT b.status, s.show_time as showtime, b.total_price " +
                            "FROM bookings b " +
                            "JOIN sessions s ON b.session_id = s.sessionid " +
                            "WHERE b.booking_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp showtime = rs.getTimestamp("showtime");
                String status = rs.getString("status");
                double amount = rs.getDouble("total_price");

                // Check if less than 24 hours (Exception Flow 4.1)
                // long diffInMs = showtime.getTime() - System.currentTimeMillis();
                // long diffInHours = diffInMs / (1000 * 60 * 60);

                // if (diffInHours < 24) {
                //     return "FAILED: Cancellation not permitted within 24 hours of showtime.";
                // }

                // 2. UC-23 Flow 3: Update Booking and Release Seats
                conn.setAutoCommit(false); // Start transaction
                try {
                    // Update Booking Status
                    String updateBooking = "UPDATE bookings SET status = 'CANCELLED', cancelled_at = CURRENT_TIMESTAMP, " +
                                        "refund_status = ? WHERE booking_id = ?";
                    PreparedStatement upPstmt = conn.prepareStatement(updateBooking);
                    upPstmt.setString(1, amount > 0 ? "REFUND_INITIATED" : "NONE");
                    upPstmt.setInt(2, bookingId);
                    upPstmt.executeUpdate();

                    // Release Seats (UC-23 Post-condition)
                    String releaseSeats = "UPDATE seats SET " +
                        "status = 'AVAILABLE', " +
                        "locked_by = NULL, " +
                        "locked_at = NULL " +
                        "WHERE seat_number IN (" +
                        "  SELECT seat_number FROM booking_seats WHERE booking_id = ?" +
                        ") " +
                        "AND session_id = (SELECT session_id FROM bookings WHERE booking_id = ?)";
                    PreparedStatement relPstmt = conn.prepareStatement(releaseSeats);
                    relPstmt.setInt(1, bookingId);
                    relPstmt.setInt(2, bookingId);
                    relPstmt.executeUpdate();

                    conn.commit();
                    
                    // 3. UC-23 Flow 4: Simulated Refund logic
                    String refundMsg = (amount > 0) ? " Refund of RM " + amount + " initiated." : "";
                    return "SUCCESS: Booking #" + bookingId + " cancelled." + refundMsg;

                } catch (SQLException e) {
                    conn.rollback();
                    return "FAILED: Database error during cancellation.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "FAILED: Booking ID not found.";
    }

   @Override
    public void getBookingDetails(int bookingId) {
        // UC-24: Full join to get Movie, Session, and Hall details
        String sql = "SELECT b.booking_id, b.status, b.booking_ref, b.total_price, " +
                    "m.title as movie_title, s.show_time, s.hall_name, " +
                    "array_agg(bs.seat_number) as seat_list " +
                    "FROM bookings b " +
                    "JOIN sessions s ON b.session_id = s.sessionid " + 
                    "JOIN movies m ON s.movieid = m.movieid " +              
                    "JOIN booking_seats bs ON b.booking_id = bs.booking_id " +
                    "WHERE b.booking_id = ? " +
                    "GROUP BY b.booking_id, m.title, s.show_time, s.hall_name";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n========================================");
                System.out.println("       MOVIE TICKET: #" + rs.getString("booking_ref"));
                System.out.println("========================================");
                System.out.println("MOVIE    : " + rs.getString("movie_title"));
                System.out.println("HALL     : " + rs.getString("hall_name"));
                System.out.println("TIME     : " + rs.getTimestamp("show_time"));
                System.out.println("SEATS    : " + rs.getString("seat_list"));
                System.out.println("----------------------------------------");
                System.out.println("STATUS   : " + rs.getString("status"));
                System.out.println("TOTAL    : RM " + String.format("%.2f", rs.getDouble("total_price")));
                System.out.println("========================================\n");
            } else {
                System.out.println("[!] Error: Booking details not found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Ensure table names 'movies', 'movie_sessions', and 'halls' exist.");
            e.printStackTrace();
        }
    }

    @Override
    public void listUserBookings(String userId) {
        // UC-2 Flow of Events: Retrieve ID, date, total, and status
        String sql = "SELECT b.booking_id, b.created_at, b.total_price, b.status, m.title " +
                    "FROM bookings b " +
                    "JOIN sessions s ON b.session_id = s.sessionid " +
                    "JOIN movies m ON s.movieid = m.movieid " +
                    "WHERE b.user_id = CAST(? AS TEXT) " +
                    "ORDER BY b.created_at DESC";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n================================================================");
            System.out.println(String.format("%-10s | %-20s | %-12s | %-10s", "ID", "MOVIE", "TOTAL", "STATUS"));
            System.out.println("----------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(String.format("%-10d | %-20s | RM %-9.2f | %-10s", 
                    rs.getInt("booking_id"),
                    rs.getString("title"),
                    rs.getDouble("total_price"),
                    rs.getString("status")));
            }

            if (!found) {
                // Exception Flow 3.1
                System.out.println("No Bookings found.");
            }
            System.out.println("================================================================\n");
            System.out.println("To view full details or QR ticket, use: cinema:book-details [ID]");

        } catch (SQLException e) {
            System.err.println("Error retrieving booking list: " + e.getMessage());
        }
    }
}