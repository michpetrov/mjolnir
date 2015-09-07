package org.jboss.mjolnir.server.service.validation;

import java.io.Serializable;

import org.jboss.mjolnir.client.domain.ValidationResult;

/**
 * Validates entities of given type
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface Validation<X extends Serializable> {

    ValidationResult validate(X entity);

}