package org.example.backendweride.platform.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.example.backendweride.platform.iam.application.internal.outboundservices.hashing.HashingService;
import org.example.backendweride.platform.iam.application.internal.outboundservices.tokens.TokenService;
import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.domain.model.commands.SignInCommand;
import org.example.backendweride.platform.iam.domain.model.commands.SignUpCommand;
import org.example.backendweride.platform.iam.domain.services.AccountCommandService;
import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AccountCommandServiceImpl is responsible for handling account-related commands such as sign-up and sign-in.
 *
 * @summary This service interacts with the AccountRepository to manage account data,
 *          uses HashingService for password encoding, and TokenService for token generation.
 */
@Service
public class AccountCommandServiceImpl implements AccountCommandService {
    private final AccountRepository accountRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public AccountCommandServiceImpl(AccountRepository accountRepository, HashingService hashingService, TokenService tokenService) {
        this.accountRepository = accountRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    public Optional<Account> handle(SignUpCommand command) {
        if(accountRepository.existsByUserName(command.username()))
            throw new RuntimeException("User already exists");

        var account = new Account(command, hashingService.encode(command.password()));

        try{
            var accountCreated = accountRepository.save(account);
            return Optional.of(accountCreated);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<ImmutablePair<Account, String>> handle(SignInCommand command) {
        var accountExists = accountRepository.existsByUserName(command.username());
        if(accountExists) {
            var account = accountRepository.findByUserName(command.username());
            var token = tokenService.generateToken(account.get().getUserName());
            return Optional.of(ImmutablePair.of(account.get(), token));
        }
        throw new RuntimeException("User not found");
    }
}

