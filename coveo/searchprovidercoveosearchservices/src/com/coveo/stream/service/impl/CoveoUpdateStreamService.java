package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.PartialUpdateDocument;
import com.coveo.pushapiclient.ShallowMergeDocument;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import de.hybris.platform.core.Registry;

import java.io.IOException;
import java.util.List;

public class CoveoUpdateStreamService extends CoveoAbstractStreamService<UpdateStreamService> {

    UpdateStreamService updateStreamService;

    @Override
    public void init(CoveoSource coveoSource, String[] userAgents) {
        this.coveoSource = coveoSource;
        updateStreamService = createStreamService(createCatalogSource(coveoSource), userAgents);
    }

    @Override
    protected UpdateStreamService createStreamService(CatalogSource catalogSource, String[] userAgents) {
        final UpdateStreamService updateStreamService = Registry.getApplicationContext().getBean(UpdateStreamService.class);
        // Initialize with catalog queue support (true) to enable shallow merge
        updateStreamService.init(catalogSource, userAgents, true);
        return updateStreamService;
    }

    @Override
    public void pushDocument(DocumentBuilder document) throws IOException, InterruptedException {
        updateStreamService.addOrUpdate(document);
    }

    @Override
    public void pushPartialDocument(List<PartialUpdateDocument> documents) throws IOException, InterruptedException {
        for (PartialUpdateDocument partialUpdateDocument : documents) {
            updateStreamService.addPartialUpdate(partialUpdateDocument);
        }
    }

    @Override
    public void pushShallowMergeDocument(List<ShallowMergeDocument> documents) throws IOException, InterruptedException {
        for (ShallowMergeDocument shallowMergeDocument : documents) {
            updateStreamService.addShallowMerge(shallowMergeDocument);
        }
    }

    @Override
    public void closeStream() throws IOException, InterruptedException, NoOpenFileContainerException {
        updateStreamService.close();
    }

}
