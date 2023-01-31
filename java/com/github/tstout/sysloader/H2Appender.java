package com.github.tstout.sysloader;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "H2Appender", category = "core", elementType = "appender")
public class H2Appender extends AbstractAppender {

  private ConcurrentMap<String, LogEvent> eventMap = new ConcurrentHashMap<>();

  protected H2Appender(String name, Filter filter) {
    super(name, filter, null);
  }

  @PluginFactory
  public static H2Appender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Filter") Filter filter) {
    return new H2Appender(name, filter);
  }

  @Override
  public void append(LogEvent event) {
    eventMap.put(Instant.now().toString(), event);
  }
}