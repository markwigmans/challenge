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
package com.ximedes.sva.frontend.api;

import com.ximedes.sva.frontend.message.Account;
import com.ximedes.sva.frontend.message.Transfer;
import com.ximedes.sva.frontend.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 *
 */
@RestController
@Slf4j
public class TransactionController {

    private final TransferService transferService;

    @Autowired
    public TransactionController(final TransferService transferService) {
        this.transferService = transferService;
    }

    @RequestMapping(value = "/transaction/{transactionId}", method = RequestMethod.GET)
    public ResponseEntity<Account> queryTransaction(@RequestParam String transactionId) {
        log.debug("queryTransaction({})", transactionId);

        // TODO
        return null;
    }
}
