package com.parkit.parkingsystem.integration;

import java.util.Date;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private static FareCalculatorService fareCalculatorService;

    @Mock
    private static InputReaderUtil inputReaderUtil;


    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

   @BeforeEach
   private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        assertNotNull(ticket);
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertEquals(false, ticket.getParkingSpot().isAvailable());

    }

    @Test
    public void testParkingLotExit(){

        testParkingACar();

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // TODO: check that the fare generated and out time are populated correctly in the database

        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getInTime().before(ticket.getOutTime()));
        assertTrue(ticket.getPrice() >= 0);

    }

    @Test
    public void testParkingLotExitRecurringUser(){

        TicketDAO ticketDAOSpy = spy(TicketDAO.class);
        when(ticketDAOSpy.getNbTicket("ABCDEF")).thenReturn(1);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOSpy);

        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAOSpy.getTicket("ABCDEF");

        verify(ticketDAOSpy, times(2)).getNbTicket("ABCDEF");

        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getInTime().before(ticket.getOutTime()));
        assertTrue(ticket.getPrice() >= 0);

   }

}
