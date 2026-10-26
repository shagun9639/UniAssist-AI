package com.uniassist;

import com.uniassist.dto.TicketDtos.CreateTicketRequest;
import com.uniassist.dto.TicketDtos.SupportTicketDto;
import com.uniassist.dto.TicketDtos.TicketResponseRequest;
import com.uniassist.entity.User;
import com.uniassist.repository.UserRepository;
import com.uniassist.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SupportTicketServiceTest {

    @Autowired
    private SupportTicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testTicketWorkflow() {
        User student = userRepository.findByEmail("student@uniassist.edu").orElseThrow();
        User admin = userRepository.findByEmail("admin@uniassist.edu").orElseThrow();

        CreateTicketRequest createReq = new CreateTicketRequest();
        createReq.setCategory("Examination");
        createReq.setSubject("Discrepancy in 3rd semester grade card");
        createReq.setDescription("My grade card shows absent for elective paper although I attended.");
        createReq.setPriority("HIGH");

        SupportTicketDto ticket = ticketService.createTicket(student.getId(), createReq);
        assertNotNull(ticket);
        assertEquals("OPEN", ticket.getStatus());
        assertEquals("Examination", ticket.getCategory());

        // Student lists tickets
        List<SupportTicketDto> studentTickets = ticketService.getUserTickets(student.getId());
        assertFalse(studentTickets.isEmpty());

        // Admin adds staff response
        TicketResponseRequest responseReq = new TicketResponseRequest();
        responseReq.setResponseText("We have requested attendance logs from the examination hall supervisor.");
        var responseDto = ticketService.addResponse(admin.getId(), ticket.getId(), responseReq, true);
        assertNotNull(responseDto);

        // Verify status transitioned to IN_PROGRESS
        SupportTicketDto updatedTicket = ticketService.getTicketById(student.getId(), ticket.getId(), false);
        assertEquals("IN_PROGRESS", updatedTicket.getStatus());
        assertEquals(1, updatedTicket.getResponses().size());
    }
}
