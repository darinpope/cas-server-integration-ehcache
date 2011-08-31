/*
 * Copyright 2009 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * <a href="http://ehcache.sourceforge.net/">EHCache</a> based distributed ticket registry.
 * </p>
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * <ul>
 * <li>Tuning : use cache level time to live with different values for TGT an ST.</li>
 * <li>Tuning : have different replication strategies for TGT and ST (ST should be synchronized more quickly).</li>
 * <li>Monitoring : follow separately the number of TGT and ST.</li>
 * <ul>
 * </p>
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author <a href="mailto:darin@planetpope.com">Darin Pope</a>
 */
public final class EhcacheTicketRegistry extends AbstractDistributedTicketRegistry implements InitializingBean {

  protected Cache serviceTicketsCache;

  protected Cache ticketGrantingTicketsCache;

  @Override
  protected void updateTicket(Ticket ticket) {
    addTicket(ticket);
  }

  public void addTicket(Ticket ticket) {
    Element element = new Element(ticket.getId(), ticket);
    if (ticket instanceof ServiceTicket) {
      this.serviceTicketsCache.put(element);
    } else if (ticket instanceof TicketGrantingTicket) {
      this.ticketGrantingTicketsCache.put(element);
    } else {
      throw new IllegalArgumentException("Invalid ticket type " + ticket);
    }
  }

  public Ticket getTicket(String ticketId) {
    if (ticketId == null) {
      return null;
    }

    Element element;
    if (ticketId.startsWith(TicketGrantingTicket.PREFIX)) {
      element = this.ticketGrantingTicketsCache.get(ticketId);
    } else if (ticketId.startsWith(ServiceTicket.PREFIX)) {
      element = this.serviceTicketsCache.get(ticketId);
    } else {
      element = null;
      if (log.isInfoEnabled()) {
        log.info("Unsupported ticket prefix for ticketId '" + ticketId + "', return " + element);
      }
    }
    return element == null ? null : getProxiedTicketInstance((Ticket)element.getValue());
  }

  public boolean deleteTicket(String ticketId) {
    if (ticketId == null) {
      return false;
    }
    boolean result;
    if (ticketId.startsWith(TicketGrantingTicket.PREFIX)) {
      result = this.ticketGrantingTicketsCache.remove(ticketId);
    } else if (ticketId.startsWith(ServiceTicket.PREFIX)) {
      result = this.serviceTicketsCache.remove(ticketId);
    } else {
      result = false;
      if (log.isInfoEnabled()) {
        log.info("Unsupported ticket prefix for ticketId '" + ticketId + "', return " + result);
      }
    }
    return result;
  }

  public Collection<Ticket> getTickets() {
    List ticketGrantingTicketsKeys = ticketGrantingTicketsCache.getKeys();
    List serviceTicketsKeys = serviceTicketsCache.getKeys();
    if(ticketGrantingTicketsKeys.size() == 0 && serviceTicketsKeys.size() == 0) {
      return Collections.emptyList();
    }
    final List<Ticket> list = new ArrayList<Ticket>();
    for(final Object key:ticketGrantingTicketsKeys) {
      Element element = ticketGrantingTicketsCache.get(key);
      if(element != null) {
        Ticket ticket = (Ticket) element.getValue();
        list.add(ticket);
      }
    }
    for(final Object key:serviceTicketsKeys) {
      Element element = serviceTicketsCache.get(key);
      if(element != null) {
        Ticket ticket = (Ticket) element.getValue();
        list.add(ticket);
      }
    }
    return list;
  }

  public void removeAllTickets() {
    serviceTicketsCache.removeAll();
    ticketGrantingTicketsCache.removeAll();
  }

  public void setServiceTicketsCache(Cache serviceTicketsCache) {
    this.serviceTicketsCache = serviceTicketsCache;
  }

  public void setTicketGrantingTicketsCache(Cache ticketGrantingTicketsCache) {
    this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
  }

  public void afterPropertiesSet() throws Exception {
    if (this.serviceTicketsCache == null || this.ticketGrantingTicketsCache == null) {
      String message = "Both serviceTicketsCache and ticketGrantingTicketsCache are required properties. serviceTicketsCache=" + this.serviceTicketsCache + ", ticketGrantingTicketsCache=" + this.ticketGrantingTicketsCache;
      log.error(message);
      throw new BeanInstantiationException(this.getClass(), message);
    }

    if(log.isDebugEnabled()) {
      CacheConfiguration config = this.serviceTicketsCache.getCacheConfiguration();
      log.debug("serviceTicketsCache.maxElementsInMemory=" + config.getMaxElementsInMemory());
      log.debug("serviceTicketsCache.maxElementsOnDisk=" + config.getMaxElementsOnDisk());
      log.debug("serviceTicketsCache.timeToLive=" + config.getTimeToLiveSeconds());
      log.debug("serviceTicketsCache.timeToIdle=" + config.getTimeToIdleSeconds());
      log.debug("serviceTicketsCache.terracotta.concurrency="+config.getTerracottaConfiguration().getConcurrency());
      log.debug("serviceTicketsCache.terracotta.consistency="+config.getTerracottaConsistency());
      log.debug("serviceTicketsCache.terracotta.storageStrategy="+config.getTerracottaStorageStrategy());

      config = this.ticketGrantingTicketsCache.getCacheConfiguration();
      log.debug("ticketGrantingTicketsCache.maxElementsInMemory=" + config.getMaxElementsInMemory());
      log.debug("ticketGrantingTicketsCache.maxElementsOnDisk=" + config.getMaxElementsOnDisk());
      log.debug("ticketGrantingTicketsCache.timeToLive=" + config.getTimeToLiveSeconds());
      log.debug("ticketGrantingTicketsCache.timeToIdle=" + config.getTimeToIdleSeconds());
      log.debug("ticketGrantingTicketsCache.terracotta.concurrency="+config.getTerracottaConfiguration().getConcurrency());
      log.debug("ticketGrantingTicketsCache.terracotta.consistency="+config.getTerracottaConsistency());
      log.debug("ticketGrantingTicketsCache.terracotta.storageStrategy="+config.getTerracottaStorageStrategy());
    }
  }
}
