package org.acme.observability;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.microprofile.opentracing.Traced;

import java.util.logging.Logger;

@ApplicationScoped
public class ExampleProcessor implements Processor {

    private static final Logger log = Logger.getLogger(ExampleProcessor.class.getName());

    @Traced
    @Override
    public void process(final Exchange exchange) {
        log();
    }

    @Traced
    void log() {
        log.info("ExampleProcessor.process called.");
    }
}
