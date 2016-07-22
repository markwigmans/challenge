/******************************************************************************
 * Copyright 2014,2015 Mark Wigmans
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
 ******************************************************************************/
package com.ximedes.sva.api;

import com.ximedes.sva.service.AccountService;
import com.ximedes.sva.message.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 *
 */
@RestController
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public ResponseEntity createAccount(@RequestBody Account request) {
        log.debug("createAccount({})", request);

        final String accountId = UUID.randomUUID().toString();

        accountService.createAccount(accountId);

        final URI location = UriComponentsBuilder.newInstance().pathSegment("/account", accountId).build().encode().toUri();
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public ResponseEntity<Account> queryAccount(@RequestParam String accountId) {
        log.debug("queryAccount({})", accountId);

        final Account account = accountService.queryAccount(accountId);
        if (account != null) {
            return new ResponseEntity(account, HttpStatus.OK);
        } else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
