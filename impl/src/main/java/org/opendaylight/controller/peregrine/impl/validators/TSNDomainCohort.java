/*
 * Copyright © 2017 Copyright (c) 2018 Itri, inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.peregrine.impl.validators;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Iterator;
import javassist.ClassPool;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohort;
import org.opendaylight.controller.peregrine.impl.validators.util.ValidationConstants;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf._1._0.cnc.cnc.data.rev180401.tsn_domains.Domain;

/**
 * Pre-commit semantic validation for service function path datastore objects
 *
 * <p>
 * After registration, the canCommit() method will be invoked in order to
 * validate SFP object creations / modifications. This validation will check
 * coherence with referenced SF, SFC type definitions
 */
public class TSNDomainCohort implements DOMDataTreeCommitCohort{
    private static final Logger LOG = LoggerFactory.getLogger(TSNDomainCohort.class);
    private final ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
    private final ImmutableSet<YangModuleInfo> infos = BindingReflections.loadModuleInfos();
    private BindingRuntimeContext bindingContext;
    private final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(
            StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault())));
    private final InputValidator inputValidator;

    public TSNDomainCohort(InputValidator inputv) {
        this.inputValidator = inputv;
    }

    @Override
    @Deprecated
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(Object txId,
                                                                                     DOMDataTreeCandidate candidate, SchemaContext ctx) {

        LOG.info("canCommit:called! txId={}, candidate={}, context={} ", txId, candidate, ctx);

        DataTreeCandidateNode candidateRoot = candidate.getRootNode();
        NormalizedNode nn = candidateRoot.getDataAfter().orElse(null);
        if (nn == null) {
            LOG.info("canCommit:no sfp after the change");
            return ValidationConstants.SUCCESS_CAN_COMMIT_FUTURE;
        }

        LOG.info("canCommit:updating codec contexts");
        moduleContext.addModuleInfos(infos);
        bindingContext = BindingRuntimeContext.create(moduleContext, ctx);
        codecRegistry.onBindingRuntimeContextUpdated(bindingContext);

        LOG.info("canCommit:Mapping service ready");

        LOG.info("canCommit:before deserializing:  {}", nn);
        // (nn is an immutableMapNode). Contains an unmodifiable collection
        // reference of all this thing
        // https://wiki.opendaylight.org/view/OpenDaylight_Controller:MD-SAL:Design:Normalized_DOM_Model
        Collection collection = (Collection<MapEntryNode>) nn.getValue();
        LOG.info("canCommit:collection containing the sfs:  {}", collection);
        Iterator<MapEntryNode> menIter = collection.iterator();
        while (menIter.hasNext()) {
            MapEntryNode meNode = menIter.next();
            LOG.info("canCommit:sfp to process: {}", meNode);
            LOG.info("canCommit:the first SF (as nn):  {}", meNode);
            DataObject dobj = codecRegistry.fromNormalizedNode(ValidationConstants.DOMAIN_YII, meNode)
                    .getValue();
            LOG.info("canCommit:registerValidationCohorts:the first SFP (as dataobject):  {}", dobj);
            Domain tsnDomains = (Domain) dobj;
            LOG.info("canCommit:registerValidationCohorts:the implemented interface: {}",
                    dobj.getImplementedInterface());
            LOG.info("canCommit:the first SF (as binding representation):  {}", tsnDomains);
            try {
                if (!inputValidator.validateServiceFunctionPath(tsnDomains)) {
                    return ValidationConstants.FAILED_CAN_COMMIT_SFP_FUTURE;
                }
            } catch (DataValidationFailedException dvfe) {
                return Futures.immediateFailedCheckedFuture(dvfe);
            }
        }
        return ValidationConstants.SUCCESS_CAN_COMMIT_FUTURE;
    }
}
