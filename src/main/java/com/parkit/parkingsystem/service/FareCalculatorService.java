package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();
        double durationMillis = outHour - inHour;

        if(durationMillis < 30 * 60 * 1000){
            ticket.setPrice(0);
        }else{
            double duration = durationMillis - (30 * 60 * 1000);
            duration = duration / (60 * 60 * 1000);

            switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                if(discount) {
                    ticket.setPrice(duration * (0.95 * Fare.CAR_RATE_PER_HOUR));
                }
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                if(discount) {
                    ticket.setPrice(duration * (0.95 * Fare.BIKE_RATE_PER_HOUR));
                }
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}
