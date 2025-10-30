package org.example.backendweride.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.iam.domain.model.queries.GetAccountByIdQuery;
import org.example.backendweride.platform.iam.domain.services.AccountQueryService;
import org.example.backendweride.platform.iam.interfaces.rest.resources.AccountResource;
import org.example.backendweride.platform.iam.interfaces.rest.transform.AccountResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accounts Controller
 *
 * @summary Handles HTTP requests related to account retrieval.
 */
@RestController
@RequestMapping(value = "api/v1/accounts")
@Tag(name = "Accounts", description = "Accounts Endpoints")
public class AccountsController {
    private final AccountQueryService accountQueryService;
    public AccountsController(AccountQueryService accountQueryService) {
        this.accountQueryService = accountQueryService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResource> getAccountById(@PathVariable Long accountId) {
        var getAccountByIdQuery = new GetAccountByIdQuery(accountId);
        var account = accountQueryService.handle(getAccountByIdQuery);
        if(account.isEmpty()) return ResponseEntity.notFound().build();

        var userResource = AccountResourceFromEntityAssembler.toResourceFromEntity(account.get());
        return ResponseEntity.ok(userResource);
    }
}