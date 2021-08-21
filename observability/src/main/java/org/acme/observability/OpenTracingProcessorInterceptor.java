package org.acme.observability;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.camel.Exchange;
// import org.apache.camel.opentracing.ActiveSpanManager;
import org.apache.camel.opentracing.OpenTracingSpanAdapter;
import org.apache.camel.tracing.ActiveSpanManager;
import org.eclipse.microprofile.opentracing.Traced;

import static io.opentracing.contrib.interceptors.OpenTracingInterceptor.SPAN_CONTEXT;

/**
 * Sets span context from exchange
 */
//@Traced
//@Interceptor
//@Priority(value = Interceptor.Priority.LIBRARY_BEFORE)
public class OpenTracingProcessorInterceptor {

    private static final Logger log = Logger.getLogger(OpenTracingProcessorInterceptor.class.getName());

    @AroundInvoke
    public Object wrap(final InvocationContext ctx) throws Exception {

        if (!traced(ctx.getMethod())) {
            return ctx.proceed();
        }

        final Optional<Exchange> exchange = Arrays.stream(ctx.getParameters())
                .filter(Exchange.class::isInstance)
                .map(Exchange.class::cast)
                .findAny();

        exchange.map(ActiveSpanManager::getSpan)
                .filter(OpenTracingSpanAdapter.class::isInstance)
                .map(OpenTracingSpanAdapter.class::cast)
                .map(OpenTracingSpanAdapter::getOpenTracingSpan)
                .ifPresent(span -> {
                    log.info("Setting span context from exchange.");
                    ctx.getContextData().put(SPAN_CONTEXT, span.context());
                });

        return ctx.proceed();
    }

    private boolean traced(final Method method) {
        final Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        final Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null) {
            return methodTraced.value();
        }
        return classTraced != null && classTraced.value();
    }
}
