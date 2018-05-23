/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.peregrine.cli.api.PeregrineCliCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeregrineCliCommandsImpl implements PeregrineCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(PeregrineCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public PeregrineCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("PeregrineCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}
