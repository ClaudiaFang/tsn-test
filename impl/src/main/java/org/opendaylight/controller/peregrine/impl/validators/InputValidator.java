/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.impl.validators;

import org.opendaylight.controller.peregrine.impl.validators.util.DataValidationFailedWithMessageException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.peregrine.impl.validators.util.ValidationConstants;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.Domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
TODO:
Test the pre-commit for PUT/POST/DELETE
 */

@Singleton
public class InputValidator {
    private static final Logger LOG = LoggerFactory.getLogger(InputValidator.class);

    private final DOMDataTreeCommitCohortRegistry registry;

    @Inject
    public InputValidator(final DOMDataBroker domDataBroker) {
        this.registry = (DOMDataTreeCommitCohortRegistry) domDataBroker.getSupportedExtensions()
                .get(org.opendaylight.controller.md.sal.dom.api.DOMDataTreeCommitCohortRegistry.class);
        registerValidationCohorts();
    }

    private void registerValidationCohorts() {
        TSNDomainCohort myCohort = new TSNDomainCohort(this);

        LOG.info("registerValidationCohorts: sfp cohort created");
        registry.registerCommitCohort(ValidationConstants.DOMAINS_DID, myCohort);
        LOG.info("registerValidationCohorts:initialized. registered cohort: {}", myCohort);
    }

    /**
     * Performs validation of a service function path.
     *
     * @param tsnDomains
     *            a candidate SFP that is being added / updated in a currently
     *            open transaction
     * @return true when validation is passed (i.e. when the SFs contained in
     *         the SFP are coherent (type-wise) with the type definitions in the
     *         associated SFC; false afterwards)
     * @throws DataValidationFailedWithMessageException
     *             when validation cannot be performed because some of the
     *             referenced SFs / SFCs do not exist
     */
    @SuppressWarnings("checkstyle:AvoidHidingCauseException")
    protected boolean validateServiceFunctionPath(Domain tsnDomains)
            throws DataValidationFailedWithMessageException {
        if (tsnDomains != null) {
            LOG.info("Get request",
                    tsnDomains.toString());

            if(tsnDomains.getId() == 0)
                throw ValidationConstants.TSN_DOMAIN_NUMBER_EXCEED;

            LOG.info("validateServiceFunctionPath:SFP validation passed!");
            return true;

        }
        return false;
    }
}
