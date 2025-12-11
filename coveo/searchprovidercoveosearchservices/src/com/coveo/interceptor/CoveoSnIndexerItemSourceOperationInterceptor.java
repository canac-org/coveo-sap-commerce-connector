package com.coveo.interceptor;

import de.hybris.platform.searchservices.admin.model.interceptor.SnIndexerItemSourceOperationInterceptor;
import de.hybris.platform.searchservices.enums.SnDocumentOperationType;
import de.hybris.platform.searchservices.model.SnFieldModel;
import de.hybris.platform.searchservices.model.SnIndexerItemSourceOperationModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

/**
 * @author Maxime Gagnon
 */
public class CoveoSnIndexerItemSourceOperationInterceptor extends SnIndexerItemSourceOperationInterceptor {

    @Override
    public void onValidate(SnIndexerItemSourceOperationModel indexerItemSourceOperation, InterceptorContext context) throws InterceptorException {
        super.onValidate(indexerItemSourceOperation, context);

        final SnDocumentOperationType documentOperationType = indexerItemSourceOperation.getDocumentOperationType();
        final Collection<SnFieldModel> fields = CollectionUtils.emptyIfNull(indexerItemSourceOperation.getFields());

        if (documentOperationType == SnDocumentOperationType.SHALLOW_MERGE && fields.isEmpty()) {
            throw new InterceptorException(getLocalizedMessage(RESOURCE_ITEMSOURCEOPERATION_FIELDS_EMPTY, SnIndexerItemSourceOperationModel.FIELDS, indexerItemSourceOperation, indexerItemSourceOperation.getItemtype(), SnFieldModel._TYPECODE), this);
        }
    }
}
