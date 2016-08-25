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
package com.ximedes.sva.frontend.api;

import com.ximedes.sva.frontend.message.Account;
import com.ximedes.sva.frontend.service.AccountService;
import kamon.annotation.EnableKamon;
import kamon.annotation.Trace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@EnableKamon
class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Trace("createAccount")
    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity> createAccount(@RequestBody Account request) throws Exception {
        log.debug("createAccount({})", request);

        return accountService.createAccount(request).thenApply(account -> {
            if (account != null) {
                final URI location = UriComponentsBuilder.newInstance().pathSegment("/account", account.getAccountId()).build().encode().toUri();
                final HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setLocation(location);
                return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
            } else {
                // very likely due to a timeout
                return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
            }
        });
    }

    @Trace("queryAccount")
    @RequestMapping(value = "/account/{accountId}", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity> queryAccount(@PathVariable String accountId) throws ExecutionException, InterruptedException {
        log.debug("queryAccount({})", accountId);

        return accountService.queryAccount(accountId).thenApply(account -> {
            if (account != null) {
                return new ResponseEntity(account, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        });
    }
}
