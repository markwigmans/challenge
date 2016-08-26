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

import com.ximedes.sva.frontend.message.Transfer;
import com.ximedes.sva.frontend.service.TransferService;
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
class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(final TransferService transferService) {
        this.transferService = transferService;
    }

    @Trace("createTransfer")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity> createTransfer(@RequestBody Transfer request) throws Exception {
        log.debug("createTransfer({})", request);

        return transferService.createTransfer(request).thenApply(transfer -> {
            if (transfer != null) {
                final URI location = UriComponentsBuilder.newInstance().pathSegment("/transfer", transfer.getTransferId()).build().toUri();
                final HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setLocation(location);
                return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
            } else {
                // very likely due to a timeout
                return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
            }
        });
    }

    @Trace("queryTransfer")
    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity> queryTransfer(@PathVariable String transferId) throws ExecutionException, InterruptedException {
        log.debug("queryTransfer({})", transferId);

        return transferService.queryTransfer(transferId).thenApply(transfer -> {
            if (transfer != null) {
                return new ResponseEntity(transfer, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        });
    }
}

