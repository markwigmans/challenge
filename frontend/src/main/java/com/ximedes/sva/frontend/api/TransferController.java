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
import java.util.concurrent.ExecutionException;

/**
 *
 */
@RestController
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(final TransferService transferService) {
        this.transferService = transferService;
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public ResponseEntity createTransfer(@RequestBody Transfer request) throws Exception {
        log.debug("createTransfer({})", request);

        final Transfer transfer = transferService.createTransfer(request).get();
        final URI location = UriComponentsBuilder.newInstance().pathSegment("/transfer", transfer.getTransferId()).build().encode().toUri();
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.GET)
    public ResponseEntity<Transfer> queryTransfer(@PathVariable String transferId) throws ExecutionException, InterruptedException {
        log.debug("queryTransfer({})", transferId);

        final Transfer transfer = transferService.queryTransfer(transferId).get();

        if (transfer != null) {
            return new ResponseEntity(transfer, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
