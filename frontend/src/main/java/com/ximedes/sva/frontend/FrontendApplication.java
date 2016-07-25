/**
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
package com.ximedes.sva.frontend;

import com.ximedes.sva.BuildInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * TODO: For improvements regarding startup time see {@linktourl http://www.alexecollins.com/spring-boot-performance/}
 */
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {FrontendConfig.class, BuildInfo.class})
public class FrontendApplication {
    /**
     * Start the whole application
     */
    public static void main(final String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
