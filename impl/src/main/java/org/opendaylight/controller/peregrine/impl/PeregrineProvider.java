/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.impl;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.base.rev180215.GroupStreamID;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.GroupListener;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.GroupStatus;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.InterfaceList;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.InterfaceListBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.interfacelist.ConfigList;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.interfacelist.ConfigListBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupinterfaceconfiguration.interfacelist.ConfigListKey;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.AccumulatedLatency;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.AccumulatedLatencyBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.FailedInterfaces;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.FailedInterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.FailedInterfacesKey;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.InterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.InterfaceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfo;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StatusInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StreamID;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.status.rev180215.groupstatus.StreamIDBuilder;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.GroupTalker;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.DataFrameSpecification;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.DataFrameSpecificationBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.DataFrameSpecificationKey;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.EndStationInterfaces;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.EndStationInterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.EndStationInterfacesKey;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.InterfaceCapabilities;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.InterfaceCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.StreamRank;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.StreamRankBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.TrafficSpecification;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.TrafficSpecificationBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.UserToNetworkRequirements;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.UserToNetworkRequirementsBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.Field;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.Ieee802MacAddressesBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ieee802.mac.addresses.Ieee802MacAddresses;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.Interval;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.IntervalBuilder;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.TimeAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.AddAllListenersInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.AddAllTalkersInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.AddAllTalkersInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.AddAllTalkersInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.GetExamplesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.GetExamplesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.GetStreamStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.GetStreamStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.GetStreamStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.PeregrineService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Listeners;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.ListenersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.StatusBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Talkers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.TalkersBuilder;


import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

public class PeregrineProvider
        implements PeregrineService, AutoCloseable{

    private static final Logger LOG = LoggerFactory.getLogger(PeregrineProvider.class);

    private DataBroker dataBroker;

    public PeregrineProvider(final DataBroker dataBroker) {

        this.dataBroker = dataBroker;

        LOG.debug("PeregrineProvider creation complete");
    }

    public PeregrineProvider() {

        LOG.debug("PeregrineProvider creation complete");
    }

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PeregrineProvider Session Initiated");

        // Preconditions.checkNotNull(dataBroker, "dataBroker must be set");

        // Register our MXBean.
        // register();
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("PeregrineProvider Closed");

        // Unregister our MXBean.
        // unregister();
    }


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

        LinkedList<org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getstreamstatus.output.Status> statusLinkedList = new LinkedList<>();
        for(GroupStatus st: genStatusLists()){
            statusLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getstreamstatus.output.StatusBuilder(st).build());
        }

        GetStreamStatusOutput output = new GetStreamStatusOutputBuilder()
                .setStatus(statusLinkedList)
                .build();

        LOG.info("getStreamStatus(): return {}", output);

        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<GetExamplesOutput>> getExamples(){

        LinkedList<org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Talkers> talkersLinkedList = new LinkedList<>();
        for(GroupTalker ta: genTalkerList()){
            talkersLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.TalkersBuilder(ta).build());
        }

        LinkedList<org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Listeners> listenersLinkedList = new LinkedList<>();
        for(GroupListener li: genListenerList()){
            listenersLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.ListenersBuilder(li).build());
        }

        LinkedList<org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.Status> statusLinkedList = new LinkedList<>();
        for(GroupStatus st: genStatusLists()){
            statusLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.moit.tsn.cnc.rev180401.getexamples.output.StatusBuilder(st).build());
        }

        GetExamplesOutput output = new GetExamplesOutputBuilder()
                .setStatus(statusLinkedList)
                .setListeners(null)
                .setTalkers(talkersLinkedList)
                .build();

        LOG.info("getStreamStatus(): return {}", output);

        return RpcResultBuilder.success(output).buildFuture();
    }

    private List<GroupStatus> genStatusLists(){
        StatusInfo statusInfo = new StatusInfoBuilder()
                .setTalkerStatus(StatusInfo.TalkerStatus.Ready)
                .setListenerStatus(StatusInfo.ListenerStatus.Ready)
                .setFailureCode((short)1)
                .build();

        AccumulatedLatency accumulatedLatency = new AccumulatedLatencyBuilder()
                .setAL(10L)
                .build();

        StreamID streamID = new StreamIDBuilder()
                .setUniqueID(new Random().nextInt(65535))
                .setMacAddress("AA:BB:CC:DD:EE:FF").build();

        List<FailedInterfaces> failedInterfacesList = new LinkedList<>();
        FailedInterfaces failedInterfaces = new FailedInterfacesBuilder()
                .setKey(new FailedInterfacesKey("AA:BB:CC:DD:EE:FF"))
                .build();

        failedInterfacesList.add(failedInterfaces);


        List<InterfaceList> interfaceListsList = new LinkedList<>();

        List<ConfigList> configListList = new LinkedList<>();
        ConfigList configList = new ConfigListBuilder()
                .setKey(new ConfigListKey((short)2))
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


        List<GroupStatus> output = new LinkedList<>();
        for(int i=0; i<3;i++){
            GroupStatus status = new StatusBuilder()
                    .setStatusInfo(statusInfo)
                    .setAccumulatedLatency(accumulatedLatency)
                    .setFailedInterfaces(failedInterfacesList)
                    .setInterfaceConfiguration(interfaceConfiguration)
                    .setStreamID(streamID)
                    .build();
            output.add(status);
        }

        return output;
    }

    private List<GroupListener> genListenerList(){

        List<GroupListener> output = new LinkedList<>();

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfaces endStationInterfaces=
                new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfacesBuilder()
                .setInterfaceName("eth0")
                .setKey(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfacesKey("eth0","AA:BB:CC:DD:EE:FF"))
                .setMacAddress("AA:BB:CC:DD:EE:FF")
                .build();
        List<org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfaces> endStationInterfacesList = new LinkedList<>();
        endStationInterfacesList.add(endStationInterfaces);

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.InterfaceCapabilities interfaceCapabilities
                = new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.InterfaceCapabilitiesBuilder()
                .setCbSequenceTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setCbStreamIdenTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setVlanTagCapable(false)
                .build();

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.StreamID streamID
                = new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.StreamIDBuilder()
                .setUniqueID(new Random().nextInt(65535))
                .setMacAddress("AA:BB:CC:DD:EE:FF")
                .build();

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.UserToNetworkRequirements userToNetworkRequirements
                = new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.UserToNetworkRequirementsBuilder()
                .setMaxLatency(1234L)
                .setNumSeamlessTrees((short)2)
                .build();

        GroupListener talker = new ListenersBuilder()
                .setEndStationInterfaces(endStationInterfacesList)
                .setInterfaceCapabilities(interfaceCapabilities)
                .setStreamID(streamID)
                .setUserToNetworkRequirements(userToNetworkRequirements)
                .build();
        output.add(talker);

        return output;
    }

    private List<GroupTalker> genTalkerList(){

        List<GroupTalker> output = new LinkedList<>();

        Field field = new Ieee802MacAddressesBuilder()
                .setIeee802MacAddresses(
                        new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ieee802.mac.addresses.Ieee802MacAddressesBuilder()
                                .setDestinationMacAddress("AA:AA:AA:AA:AA:AA")
                                .setSourceMacAddress("BB:BB:BB:BB:BB:BB")
                                .build())
                .build();

        DataFrameSpecification dataFrameSpecification = new DataFrameSpecificationBuilder()
                .setField(field)
                .setIndex((short)1)
                .setKey(new DataFrameSpecificationKey((short)1))
                .build();

        List<DataFrameSpecification> dataFrameSpecificationList = new LinkedList<>();
        dataFrameSpecificationList.add(dataFrameSpecification);

        EndStationInterfaces endStationInterfaces = new EndStationInterfacesBuilder()
                .setInterfaceName("eth0")
                .setKey(new EndStationInterfacesKey("eth0","AA:BB:CC:DD:EE:FF"))
                .setMacAddress("AA:BB:CC:DD:EE:FF")
                .build();
        List<EndStationInterfaces> endStationInterfacesList = new LinkedList<>();
        endStationInterfacesList.add(endStationInterfaces);

        InterfaceCapabilities interfaceCapabilities = new InterfaceCapabilitiesBuilder()
                .setCbSequenceTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setCbStreamIdenTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setVlanTagCapable(false)
                .build();

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.StreamID streamID
                = new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.StreamIDBuilder()
                .setUniqueID(new Random().nextInt(65535))
                .setMacAddress("AA:BB:CC:DD:EE:FF")
                .build();

        StreamRank rank = new StreamRankBuilder()
                .setRank((short)5)
                .build();

        Interval interval = new IntervalBuilder()
                .setDenominator(2L)
                .setNumerator(3L)
                .build();

        org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.TimeAware timeAware
                = new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.TimeAwareBuilder()
                .setEarliestTransmitOffset(2L)
                .setJitter(5L)
                .setLatestTransmitOffset(3L)
                .build();

        TrafficSpecification trafficSpecification = new TrafficSpecificationBuilder()
                .setInterval(interval)
                .setMaxFrameSize(1500)
                .setMaxFramesPerInterval(5)
                .setTimeAware(timeAware)
                .setTransmissionSelection((short)2)
                .build();

        UserToNetworkRequirements userToNetworkRequirements = new UserToNetworkRequirementsBuilder()
                .setMaxLatency(1234L)
                .setNumSeamlessTrees((short)2)
                .build();

        GroupTalker talker = new TalkersBuilder()
                .setDataFrameSpecification(dataFrameSpecificationList)
                .setEndStationInterfaces(endStationInterfacesList)
                .setInterfaceCapabilities(interfaceCapabilities)
                .setStreamID(streamID)
                .setStreamRank(rank)
                .setTrafficSpecification(trafficSpecification)
                .setUserToNetworkRequirements(userToNetworkRequirements)
                .build();
        output.add(talker);

        return output;
    }
}