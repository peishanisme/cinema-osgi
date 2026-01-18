package com.cbse.cinema.booking;

import com.cbse.cinema.api.BookingService;
import org.osgi.service.component.annotations.Component;

@Component(service = BookingService.class)
public class BookingServiceImpl implements BookingService {

    @Override
    public String book(String movie, int seats) {
        return "Booked " + seats + " seats for " + movie;
    }
}
