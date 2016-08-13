/**
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
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
 */
package com.ximedes.sva.frontend.api;

import com.ximedes.sva.frontend.message.Transaction;
import com.ximedes.sva.frontend.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 *
 */
@RestController
@Slf4j
public class TransactionController {

    private final TransactionService service;

    @Autowired
    public TransactionController(final TransactionService service) {
        this.service = service;
    }

    @RequestMapping(value = "/transaction/{transactionId}", method = RequestMethod.GET)
    public ResponseEntity<Transaction> queryTransaction(@PathVariable String transactionId) throws ExecutionException, InterruptedException {
        log.debug("queryTransaction({})", transactionId);

        final Transaction transaction = service.queryTransaction(transactionId).get();

        if (transaction != null) {
            return new ResponseEntity(transaction, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
