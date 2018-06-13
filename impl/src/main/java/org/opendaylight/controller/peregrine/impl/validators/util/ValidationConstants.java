/*
 * Copyright (c) 2017 Ericsson Spain and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.impl.validators.util;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.TsnDomains;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.Domain;

public class ValidationConstants {

    private static final String TSN_DOMAIN_VALIDATION_ERROR = "TSN domains validation error";

    /**
     * Yang instance identifiers.
     */
    public static final org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier DOMAIN_YII =
            YangInstanceIdentifier
                    .builder().node(TsnDomains.QNAME).node(Domain.QNAME).build();

    /**
     * DOM data tree identifiers.
     */
    public static final DOMDataTreeIdentifier DOMAINS_DID = new DOMDataTreeIdentifier(
            org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION, DOMAIN_YII);

    /*
     * Exceptions
     */
    public static final DataValidationFailedWithMessageException TSN_DOMAIN_NUMBER_EXCEED =
            new DataValidationFailedWithMessageException(YangInstanceIdentifier.class,
                    DOMAIN_YII, "Domain id can not be 0.", TSN_DOMAIN_VALIDATION_ERROR);

    /*
     * Futures
     */
    public static final CheckedFuture<PostCanCommitStep, DataValidationFailedException> FAILED_CAN_COMMIT_SFP_FUTURE =
            Futures.immediateFailedCheckedFuture(TSN_DOMAIN_NUMBER_EXCEED);
    public static final CheckedFuture<PostCanCommitStep, DataValidationFailedException> SUCCESS_CAN_COMMIT_FUTURE =
            Futures.immediateCheckedFuture(PostCanCommitStep.NOOP);

    private ValidationConstants() {
    }
}
