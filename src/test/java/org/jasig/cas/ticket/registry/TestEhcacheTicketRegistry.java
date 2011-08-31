package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.logging.Logger;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @author Marc-Antoine Garrigue
 * @author Darin Pope
 */
@ContextConfiguration("/ticketRegistry.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEhcacheTicketRegistry {
  private static final Logger logger = Logger.getLogger(TestEhcacheTicketRegistry.class.getName());

  private static final int TICKETS_IN_REGISTRY = 10;

  @Autowired
  private EhcacheTicketRegistry ticketRegistry;

  protected Authentication getAuthentication() {
    return new ImmutableAuthentication(new SimplePrincipal("test"));
  }

  @Test
  public void testAddTicketToCache() {
    try {
      Ticket ticket = new TicketGrantingTicketImpl("TGT000TEST", getAuthentication(), new NeverExpiresExpirationPolicy());
      this.ticketRegistry.addTicket(ticket);
      Ticket gt = this.ticketRegistry.getTicket(ticket.getId());
      Assert.assertEquals("ticket ids do not equal",gt.getId(),ticket.getId());
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetNullTicket() {
    try {
      this.ticketRegistry.getTicket(null, TicketGrantingTicket.class);
    } catch (Exception e) {
      Assert.fail("Exception caught.  None expected.");
    }
  }

  @Test
  public void testGetNonExistingTicket() {
    try {
      this.ticketRegistry.getTicket("FALALALALALAL",TicketGrantingTicket.class);
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetExistingTicketWithProperClass() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGTTEST555",getAuthentication(), new NeverExpiresExpirationPolicy()));
      this.ticketRegistry.getTicket("TGTTEST555", TicketGrantingTicket.class);
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetExistingTicketWithInproperClass() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGTTEST123",getAuthentication(), new NeverExpiresExpirationPolicy()));
      this.ticketRegistry.getTicket("TGTTEST123", ServiceTicket.class);
    } catch (ClassCastException e) {
      return;
    }
    Assert.fail("ClassCastException expected.");
  }

  @Test
  public void testGetNullTicketWithoutClass() {
    try {
      this.ticketRegistry.getTicket(null);
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetNonExistingTicketWithoutClass() {
    try {
      this.ticketRegistry.getTicket("FALALALALALAL");
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetExistingTicket() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGT444TEST",
          getAuthentication(), new NeverExpiresExpirationPolicy()));
      this.ticketRegistry.getTicket("TGT444TEST");
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testDeleteExistingTicket() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGTTEST999",getAuthentication(), new NeverExpiresExpirationPolicy()));
      Assert.assertTrue("Ticket was not deleted.", this.ticketRegistry.deleteTicket("TGTTEST999"));
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testDeleteNonExistingTicket() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGTTEST133",getAuthentication(), new NeverExpiresExpirationPolicy()));
      Assert.assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket("TGTTEST1333"));
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testDeleteNullTicket() {
    try {
      this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGTTEST333",getAuthentication(), new NeverExpiresExpirationPolicy()));
      Assert.assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null));
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testGetTicketsIsZero() {
    try {
      this.ticketRegistry.removeAllTickets();
      Assert.assertEquals("The size of the empty registry is not zero.",this.ticketRegistry.getTickets().size(), 0);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
    }
  }

  /*
  @Test
  public void testGetTicketsFromRegistryEqualToTicketsAdded() {
    final Collection<Ticket> tickets = new ArrayList<Ticket>();
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("service", "test");

    for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
      final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
          "TEST" + i, getAuthentication(),
          new NeverExpiresExpirationPolicy());
      final ServiceTicket st = ticketGrantingTicket.grantServiceTicket(
          "tests" + i, SimpleWebApplicationServiceImpl.createServiceFrom(request),
          new NeverExpiresExpirationPolicy(), false);
      tickets.add(ticketGrantingTicket);
      tickets.add(st);
      this.ticketRegistry.addTicket(ticketGrantingTicket);
      this.ticketRegistry.addTicket(st);
    }

    try {
      Collection<Ticket> ticketRegistryTickets = this.ticketRegistry.getTickets();
      Assert.assertEquals(
          "The size of the registry is not the same as the collection.",
          ticketRegistryTickets.size(), tickets.size());

      for (final Ticket ticket : tickets) {
        if (!ticketRegistryTickets.contains(ticket)) {
          Assert.fail("Ticket was added to registry but was not found in retrieval of collection of all tickets.");
        }
      }
    } catch (Exception e) {
      Assert.fail("Caught an exception. But no exception should have been thrown.");
    }
  }
  */
}
