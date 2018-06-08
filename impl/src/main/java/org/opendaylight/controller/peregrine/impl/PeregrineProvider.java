/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;

import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.DELETE;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.WRITE;
import static org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType.OPERATIONAL;
import static org.opendaylight.yangtools.yang.common.RpcError.ErrorType.APPLICATION;

import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;

import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.Listeners;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.ListenersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.StreamsStatus;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.StreamsStatusBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.Talkers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.TalkersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.TsnDomains;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.TsnDomainsBuilder;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.listeners.Listener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.listeners.ListenerBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.listeners.ListenerKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.streams_status.Status;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.streams_status.StatusBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.talkers.Talker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.talkers.TalkerBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.talkers.TalkerKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.DomainKey;

import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.base.rev180215.GroupInterfaceCapabilities;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.base.rev180215.GroupInterfaceId;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.base.rev180215.GroupStreamID;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.base.rev180215.GroupUserToNetworkRequirements;

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
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.Ieee802VlanTagBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.Ipv4TupleBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.Ipv6TupleBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.Interval;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.IntervalBuilder;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.TimeAware;
import org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.trafficspecification.TimeAwareBuilder;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.GetExamplesOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.GetExamplesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.CncFunctinsService;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.peregrine.impl.validators.*;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.CncDataData;
public class PeregrineProvider
        implements CncFunctinsService, AutoCloseable{

    private static final Logger LOG = LoggerFactory.getLogger(PeregrineProvider.class);

    private DataBroker dataBroker;

    private static final InstanceIdentifier<Listeners> LISTENERS_IID = InstanceIdentifier.builder(Listeners.class).build();
    private static final InstanceIdentifier<StreamsStatus> STREAMS_STATUS_IID = InstanceIdentifier.builder(StreamsStatus.class).build();
    private static final InstanceIdentifier<Talkers> TALKERS_IID = InstanceIdentifier.builder(Talkers.class).build();
    private static final InstanceIdentifier<TsnDomains> TSN_DOMAINS_IID = InstanceIdentifier.builder(TsnDomains.class).build();

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

        Preconditions.checkNotNull(dataBroker, "dataBroker must be set");

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
    public Future<RpcResult<GetExamplesOutput>> getExamples(){

        final SettableFuture<RpcResult<GetExamplesOutput>> futureResult = SettableFuture.create();


        LinkedList<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.Talkers> talkersLinkedList = new LinkedList<>();
        for(GroupTalker ta: genTalkerList()){
            talkersLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.TalkersBuilder(ta).build());
        }

        LinkedList<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.Listeners> listenersLinkedList = new LinkedList<>();
        for(GroupListener li: genListenerList()){
            listenersLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.ListenersBuilder(li).build());
        }

        LinkedList<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.Status> statusLinkedList = new LinkedList<>();
        for(GroupStatus st: genStatusLists()){
            statusLinkedList.add(
                    new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.StatusBuilder(st).build());
        }

        /*ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        ListenableFuture<Optional<StreamsStatus>> readFuture = tx.read(OPERATIONAL, STREAMS_STATUS_IID);

        try {
            readFuture.get().get().getStatus();
        }
        catch (Exception ex){
            if(ex instanceof ExecutionException){

                futureResult.set(RpcResultBuilder.<GetExamplesOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, ex.getMessage()).build());
            }
            if(ex instanceof InterruptedException){
                futureResult.set(RpcResultBuilder.<GetExamplesOutput>failed().withError(RpcError.ErrorType.APPLICATION,
                        "Unexpected error committing status", ex).build());
            }
            tx.cancel();
            return futureResult;
        }
        tx.commit();*/

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        ListenableFuture<Optional<StreamsStatus>> readFuture = tx.read(OPERATIONAL, STREAMS_STATUS_IID);

        final ListenableFuture<Void> commitFuture =
            Futures.transformAsync(readFuture, data -> {

                if ( data.isPresent()){
                    statusLinkedList.clear();

                    for(GroupStatus st: data.get().getStatus() ){
                        statusLinkedList.add(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.functions.rev180401.getexamples.output.StatusBuilder(st)
                                .build());
                    }
                }
                else {
                    LOG.debug("Data not exists!");
                    return Futures.immediateFailedCheckedFuture(
                            new TransactionCommitFailedException("", makeToasterOutOfBreadError()));
                }

                LOG.debug("Read status from data store: {}", data.get().getStatus());

                return tx.submit();
            });

        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
            }

            @Override
            public void onFailure(final Throwable ex) {
                if (ex instanceof OptimisticLockFailedException) {

                    futureResult.set(RpcResultBuilder.<GetExamplesOutput>failed()
                            .withError(RpcError.ErrorType.APPLICATION, ex.getMessage()).build());

                } else if (ex instanceof TransactionCommitFailedException) {
                    LOG.debug("Failed to commit status", ex);

                    futureResult.set(RpcResultBuilder.<GetExamplesOutput>failed()
                            .withRpcErrors(((TransactionCommitFailedException)ex).getErrorList()).build());

                } else {
                    LOG.debug("Unexpected error committing Toaster status", ex);
                    futureResult.set(RpcResultBuilder.<GetExamplesOutput>failed().withError(RpcError.ErrorType.APPLICATION,
                            "Unexpected error committing status", ex).build());
                }
            }
        });

        GetExamplesOutput output = new GetExamplesOutputBuilder()
                .setStatus(statusLinkedList)
                .setListeners(listenersLinkedList)
                .setTalkers(talkersLinkedList)
                .build();

        LOG.info("getStreamStatus(): return {}", output);

        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<java.lang.Void>> removeAllCNCDataFromDataStore(){
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.delete(CONFIGURATION, TSN_DOMAINS_IID);
        tx.delete(CONFIGURATION, TALKERS_IID);
        tx.delete(CONFIGURATION, LISTENERS_IID);
        tx.delete(OPERATIONAL, STREAMS_STATUS_IID);
        tx.submit();
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<java.lang.Void>> addCNCTestDataToDataStore(){
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(CONFIGURATION,TSN_DOMAINS_IID,  buildTSNDomains());
        tx.put(OPERATIONAL,STREAMS_STATUS_IID, buildStreamsStatus());
        tx.put(CONFIGURATION, TALKERS_IID, buildTalkers());
        tx.put(CONFIGURATION, LISTENERS_IID, buildListeners());
        tx.submit();
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
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
                .setMacAddress("AA-BB-CC-DD-EE-FF").build();

        List<FailedInterfaces> failedInterfacesList = new LinkedList<>();
        FailedInterfaces failedInterfaces = new FailedInterfacesBuilder()
                .setKey(new FailedInterfacesKey("AA-BB-CC-DD-EE-FF"))
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
                .setMacAddress("AA-BB-CC-DD-EE-FF")
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

        GroupInterfaceId endStationInterfaces= new EndStationInterfacesBuilder()
                .setInterfaceName("eth0")
                .setKey(new EndStationInterfacesKey("eth0","AA-BB-CC-DD-EE-FF"))
                .setMacAddress("AA-BB-CC-DD-EE-FF")
                .build();

        GroupInterfaceCapabilities interfaceCapabilities = new InterfaceCapabilitiesBuilder()
                .setCbSequenceTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setCbStreamIdenTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setVlanTagCapable(false)
                .build();

        GroupStreamID streamID = new StreamIDBuilder()
                .setUniqueID(new Random().nextInt(65535))
                .setMacAddress("AA-BB-CC-DD-EE-FF")
                .build();

        GroupUserToNetworkRequirements userToNetworkRequirements = new UserToNetworkRequirementsBuilder()
                .setMaxLatency(1234L)
                .setNumSeamlessTrees((short)2)
                .build();

        GroupListener listener = new ListenerBuilder()
                .setEndStationInterfaces(new LinkedList<org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfaces>() {{
                        add(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.EndStationInterfacesBuilder(endStationInterfaces).build());
                    }})
                .setInterfaceCapabilities(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.InterfaceCapabilitiesBuilder(interfaceCapabilities).build())
                .setStreamID(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.StreamIDBuilder(streamID).build())
                .setUserToNetworkRequirements(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.listener.rev180215.grouplistener.UserToNetworkRequirementsBuilder(userToNetworkRequirements).build())
                .build();
        output.add(listener);

        return output;
    }

    private List<GroupTalker> genTalkerList(){

        List<GroupTalker> output = new LinkedList<>();

        List<DataFrameSpecification> dataFrameSpecificationList = new LinkedList<>();

        Field field = new Ieee802MacAddressesBuilder()
                .setIeee802MacAddresses(
                        new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ieee802.mac.addresses.Ieee802MacAddressesBuilder()
                                .setDestinationMacAddress("AA-AA-AA-AA-AA-AA")
                                .setSourceMacAddress("BB-BB-BB-BB-BB-BB")
                                .build())
                .build();
        DataFrameSpecification dataFrameSpecification = new DataFrameSpecificationBuilder()
                .setField(field)
                .setIndex((short)1)
                .setKey(new DataFrameSpecificationKey((short)1))
                .build();
        dataFrameSpecificationList.add(dataFrameSpecification);

        field = new Ieee802VlanTagBuilder()
                .setIeee802VlanTag(
                        new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ieee802.vlan.tag.Ieee802VlanTagBuilder()
                                .setPriorityCodePoint((short) 5)
                                .setVlanId( 300 )
                                .build())
                .build();
        dataFrameSpecification = new DataFrameSpecificationBuilder()
                .setField(field)
                .setIndex((short)2)
                .setKey(new DataFrameSpecificationKey((short)2))
                .build();
        dataFrameSpecificationList.add(dataFrameSpecification);

        field = new Ipv4TupleBuilder()
                .setIpv4Tuple(
                        new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ipv4.tuple.Ipv4TupleBuilder()
                                .setDestinationIpAddress(new Ipv4Address("192.168.56.101"))
                                .setDestinationPort( 300 )
                                .setDscp((short) 50)
                                .setProtocol(17)
                                .setSourceIpAddress(new Ipv4Address("192.168.56.102"))
                                .setSourcePort(80)
                                .build())
                .build();
        dataFrameSpecification = new DataFrameSpecificationBuilder()
                .setField(field)
                .setIndex((short)3)
                .setKey(new DataFrameSpecificationKey((short)3))
                .build();
        dataFrameSpecificationList.add(dataFrameSpecification);

        field = new Ipv6TupleBuilder()
                .setIpv6Tuple(
                        new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.dataframespecification.field.ipv6.tuple.Ipv6TupleBuilder()
                                .setDestinationIpAddress(new Ipv6Address("FE80::0202:B3FF:FE1E:8329"))
                                .setDestinationPort( 300 )
                                .setDscp((short) 50)
                                .setProtocol(17)
                                .setSourceIpAddress(new Ipv6Address("FE80::0202:B3FF:FE1E:832A"))
                                .setSourcePort(80)
                                .build())
                .build();
        dataFrameSpecification = new DataFrameSpecificationBuilder()
                .setField(field)
                .setIndex((short)4)
                .setKey(new DataFrameSpecificationKey((short)4))
                .build();
        dataFrameSpecificationList.add(dataFrameSpecification);

        EndStationInterfaces endStationInterfaces = new EndStationInterfacesBuilder()
                .setInterfaceName("eth0")
                .setKey(new EndStationInterfacesKey("eth0","AA-BB-CC-DD-EE-FF"))
                .setMacAddress("AAB:CC:DD:EE:FF")
                .build();
        List<EndStationInterfaces> endStationInterfacesList = new LinkedList<>();
        endStationInterfacesList.add(endStationInterfaces);

        InterfaceCapabilities interfaceCapabilities = new InterfaceCapabilitiesBuilder()
                .setCbSequenceTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setCbStreamIdenTypeList(new LinkedList<Long>(){{ add(1L); add(2L);}})
                .setVlanTagCapable(false)
                .build();

        GroupStreamID streamID
                = new StreamIDBuilder()
                .setUniqueID(new Random().nextInt(65535))
                .setMacAddress("AA-BB-CC-DD-EE-FF")
                .build();

        StreamRank rank = new StreamRankBuilder()
                .setRank((short)5)
                .build();

        Interval interval = new IntervalBuilder()
                .setDenominator(2L)
                .setNumerator(3L)
                .build();

        TimeAware timeAware = new TimeAwareBuilder()
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

        GroupTalker talker = new TalkerBuilder()
                .setDataFrameSpecification(dataFrameSpecificationList)
                .setEndStationInterfaces(endStationInterfacesList)
                .setInterfaceCapabilities(interfaceCapabilities)
                .setStreamID(new org.opendaylight.yang.gen.v1.urn.ieee.std._802._1q.yang.ieee802.dot1q.cc.talker.rev180215.grouptalker.StreamIDBuilder(streamID).build())
                .setStreamRank(rank)
                .setTrafficSpecification(trafficSpecification)
                .setUserToNetworkRequirements(userToNetworkRequirements)
                .build();
        output.add(talker);

        return output;
    }

    private TsnDomains buildTSNDomains(){

        List<Domain> output = new LinkedList<>();

        output.add( new DomainBuilder()
                .setName("Domain1")
                .setKey(new DomainKey(12345))
                .setId(12345)
                .build());

        output.add( new DomainBuilder()
                .setName("Domain2")
                .setKey(new DomainKey(12346))
                .setId(12346)
                .build());

        return new TsnDomainsBuilder()
                .setDomain(output)
                .build();
    }

    private StreamsStatus buildStreamsStatus(){

        List<Status> output = new LinkedList<>();
        for(GroupStatus st: genStatusLists()){
            output.add( new StatusBuilder(st).build());
        }

        return new StreamsStatusBuilder()
                .setStatus(output)
                .build();
    }

    private Talkers buildTalkers(){

        List<Talker> output = new LinkedList<>();
        for(GroupTalker ta: genTalkerList()){
            output.add( new TalkerBuilder(ta).setKey(new TalkerKey(ta.getStreamID().getUniqueID())).build());
        }

        return new TalkersBuilder()
                .setTalker(output)
                .build();
    }

    private Listeners buildListeners(){

        List<Listener> output = new LinkedList<>();
        for(GroupListener li: genListenerList()){
            output.add( new ListenerBuilder(li).setKey(new ListenerKey(li.getStreamID().getUniqueID())).build());
        }

        return new ListenersBuilder()
                .setListener(output)
                .build();
    }

    private RpcError makeToasterOutOfBreadError() {
        return RpcResultBuilder.newError(APPLICATION, "resource-denied", "Toaster is out of bread", "out-of-stock",
                null, null);
    }
}