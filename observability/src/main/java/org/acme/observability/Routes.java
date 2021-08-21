/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.observability;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.opentracing.OpenTracingTracer;
import org.apache.camel.opentracing.OpenTracingTracingStrategy;

@ApplicationScoped
public class Routes extends RouteBuilder {

    @Inject
    ExampleProcessor exampleProcessor;

    @Override
    public void configure() {

        // Create spans for each processor
        OpenTracingTracer tracer = new OpenTracingTracer();
        tracer.setTracingStrategy(new OpenTracingTracingStrategy(tracer));
        tracer.init(getContext());

        // Invokes a simple greeting endpoint every 10 seconds
        from("timer:greeting?period=10000")
                .to("netty-http:http://localhost:8099/greeting");

        from("netty-http:0.0.0.0:8099/greeting")
                // Random delay to simulate latency
                .delay(simple("${random(1000, 5000)}"))
                .process(exampleProcessor)
                .setBody(constant("Hello From Camel Quarkus!"));
    }
}
