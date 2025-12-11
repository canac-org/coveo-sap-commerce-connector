package com.coveo.service.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnIdentityProvider;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.enums.SnDocumentOperationType;
import de.hybris.platform.searchservices.indexer.service.SnIndexerBatchContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerItemSourceOperation;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerBatchStrategy;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author Maxime Gagnon
 */
public class CoveoSnIndexerBatchStrategy extends DefaultSnIndexerBatchStrategy {

    @Override
    protected void addIndexDocumentBatchOperationRequests(final SnIndexerBatchContext indexerBatchContext, final SnIdentityProvider<ItemModel> identityProvider, final List<SnDocumentBatchOperationRequest> documentBatchOperationRequests, final SnIndexerItemSourceOperation indexerItemSourceOperation, final List<PK> pks) throws SnException, InterruptedException {
        if (SnDocumentOperationType.CREATE == indexerItemSourceOperation.getDocumentOperationType() //
                || SnDocumentOperationType.CREATE_UPDATE == indexerItemSourceOperation.getDocumentOperationType() //
                || SnDocumentOperationType.PARTIAL_UPDATE == indexerItemSourceOperation.getDocumentOperationType() //
                || SnDocumentOperationType.SHALLOW_MERGE == indexerItemSourceOperation.getDocumentOperationType()) //
        {
            addIndexDocumentBatchOperationRequests(indexerBatchContext, documentBatchOperationRequests, pks, identityProvider, indexerItemSourceOperation);
        } else if (SnDocumentOperationType.DELETE == indexerItemSourceOperation.getDocumentOperationType()) {
            addDeleteDocumentBatchOperationRequests(indexerBatchContext, documentBatchOperationRequests, pks, identityProvider);
        } else {
            throw new SnException(MessageFormat.format("Cannot process document operation type ''{0}''", indexerItemSourceOperation.getDocumentOperationType()));
        }
    }
}
