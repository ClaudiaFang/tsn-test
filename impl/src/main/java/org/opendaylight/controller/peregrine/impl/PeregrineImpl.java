/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.peregrine.impl;

import com.google.common.util.concurrent.Futures;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.InterfaceList;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.InterfaceListBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.interfacelist.ConfigList;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.interfacelist.ConfigListBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.AccumulatedLatency;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.AccumulatedLatencyBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.FailedInterfaces;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.FailedInterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.InterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.InterfaceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfo;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfo.ListenerStatus;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfo.TalkerStatus;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StreamID;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StreamIDBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.AddAllListenersInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.AddAllTalkersInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.GetStreamStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.GetStreamStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.PeregrineService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.getstreamstatus.output.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.peregrine.rev180401.getstreamstatus.output.StatusBuilder;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class PeregrineImpl implements PeregrineService {

    @Override
    public Future<RpcResult<Void>> addAllListeners(AddAllListenersInput input) {
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<Void>> addAllTalkers(AddAllTalkersInput input) {
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<GetStreamStatusOutput>> getStreamStatus() {

        StatusInfo statusInfo = new StatusInfoBuilder()
                .setTalkerStatus(TalkerStatus.Ready)
                .setListenerStatus(ListenerStatus.Ready)
                .setFailureCode((short)1)
                .build();

        AccumulatedLatency accumulatedLatency = new AccumulatedLatencyBuilder()
                .setAL(10L)
                .build();

        StreamID streamID = new StreamIDBuilder()
                .setUniqueID(1234567890)
                .setMacAddress("AA:BB:CC:DD:EE:FF").build();

        List<FailedInterfaces> failedInterfacesList = new LinkedList<>();
        FailedInterfaces failedInterfaces = new FailedInterfacesBuilder()
                .build();
        failedInterfacesList.add(failedInterfaces);


        List<InterfaceList> interfaceListsList = new LinkedList<>();

        List<ConfigList> configListList = new LinkedList<>();
        ConfigList configList = new ConfigListBuilder()
                .setIndex((short)2)
                .build();
        configListList.add(configList);

        InterfaceList interfaceList = new InterfaceListBuilder()
                .setConfigList(configListList)
                .setMacAddress("AA:BB:CC:DD:EE:FF")
                .build();
        interfaceListsList.add(interfaceList);
        InterfaceConfiguration interfaceConfiguration = new InterfaceConfigurationBuilder()
                .setInterfaceList(interfaceListsList)
                .build();


        List<Status> statusList = new LinkedList<>();
        Status status = new StatusBuilder()
                .setStatusInfo(statusInfo)
                .setAccumulatedLatency(accumulatedLatency)
                .setFailedInterfaces(failedInterfacesList)
                .setInterfaceConfiguration(interfaceConfiguration)
                .setStreamID(streamID)
                .build();

        statusList.add(status);

        GetStreamStatusOutput output = new GetStreamStatusOutputBuilder().setStatus(statusList).build();

        return RpcResultBuilder.success(output).buildFuture();
    }
}