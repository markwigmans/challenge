/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ximedes.sva.backend;

import com.ximedes.sva.shared.BuildInfo;
import kamon.Kamon;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * TODO: For improvements regarding startup time see {@linktourl http://www.alexecollins.com/spring-boot-performance/}
 */
//@EnableAutoConfiguration
@Import({
        AopAutoConfiguration.class,
        JmxAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class,
})
@ComponentScan(basePackageClasses = {BackendConfig.class, BuildInfo.class})
public class BackendApplication {

    /**
     * Start the whole application
     */
    public static void main(final String[] args) {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("backend")
                .defaultHelp(true)
                .description("SVA Challenge backend");
        parser.addArgument("-m", "--monitor").help("kamon monitoring").action(Arguments.storeTrue());

        try {
            final Namespace res = parser.parseArgs(args);
            if (res.getBoolean("monitor")) {
                Kamon.start();
            }
            new SpringApplicationBuilder()
                    .bannerMode(Banner.Mode.OFF)
                    .sources(BackendApplication.class)
                    .run(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

/*
    AopAutoConfiguration matched
      - @ConditionalOnClass classes found: org.springframework.context.annotation.EnableAspectJAutoProxy,org.aspectj.lang.annotation.Aspect,org.aspectj.lang.reflect.Advice (OnClassCondition)
            - matched (OnPropertyCondition)

    AopAutoConfiguration.JdkDynamicAutoProxyConfiguration matched
      - matched (OnPropertyCondition)

    GenericCacheConfiguration matched
      - Automatic cache type (CacheCondition)

    JmxAutoConfiguration matched
      - @ConditionalOnClass classes found: org.springframework.jmx.export.MBeanExporter (OnClassCondition)
            - matched (OnPropertyCondition)

    JmxAutoConfiguration#mbeanExporter matched
      - @ConditionalOnMissingBean (types: org.springframework.jmx.export.MBeanExporter; SearchStrategy: current) found no beans (OnBeanCondition)

    JmxAutoConfiguration#mbeanServer matched
      - @ConditionalOnMissingBean (types: javax.management.MBeanServer; SearchStrategy: all) found no beans (OnBeanCondition)

    JmxAutoConfiguration#objectNamingStrategy matched
      - @ConditionalOnMissingBean (types: org.springframework.jmx.export.naming.ObjectNamingStrategy; SearchStrategy: current) found no beans (OnBeanCondition)

    NoOpCacheConfiguration matched
      - Automatic cache type (CacheCondition)

    PropertyPlaceholderAutoConfiguration#propertySourcesPlaceholderConfigurer matched
      - @ConditionalOnMissingBean (types: org.springframework.context.support.PropertySourcesPlaceholderConfigurer; SearchStrategy: current) found no beans (OnBeanCondition)

    RedisCacheConfiguration matched
      - Automatic cache type (CacheCondition)

    SimpleCacheConfiguration matched
      - Automatic cache type (CacheCondition)
*/


}

