/**
 * ***************************************************************************
 * Copyright 2016 Mark Wigmans
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.ximedes.sva.backend;

import com.ximedes.sva.BuildInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * TODO: For improvements regarding startup time see {@linktourl http://www.alexecollins.com/spring-boot-performance/}
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {BackendConfig.class, BuildInfo.class})
public class BackendApplication {
    /**
     * Start the whole application
     */
    public static void main(final String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
