package org.example.backendweride.platform.iam.domain.model.services;

import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.domain.model.commands.SignInCommand;
import org.example.backendweride.platform.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * Account Command Service Interface
 *
 * @summary Defines operations for handling account-related commands.
 */
public interface AccountCommandService {
    Optional<Account> handle (SignUpCommand command);
    Optional<ImmutablePair<Account, String>> handle(SignInCommand command);
}
