package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.PartialUpdateDocument;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import de.hybris.platform.core.Registry;

import java.io.IOException;
import java.util.List;

public class CoveoRebuildStreamService extends CoveoAbstractStreamService<StreamService> {

    StreamService rebuildStreamService;

    @Override
    public void init(CoveoSource coveoSource, String[] userAgents) {
        this.coveoSource = coveoSource;
        rebuildStreamService = createStreamService(createCatalogSource(coveoSource), userAgents);
    }

    @Override
    protected StreamService createStreamService(CatalogSource catalogSource, String[] userAgents) {
        final StreamService streamService = Registry.getApplicationContext().getBean(StreamService.class);
        streamService.init(catalogSource, userAgents);
        return streamService;
    }

    @Override
    public void pushDocument(DocumentBuilder document) throws IOException, InterruptedException {
        rebuildStreamService.add(document);
    }

    @Override
    public void pushPartialDocument(List<PartialUpdateDocument> documents) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Partial updates are not supported in rebuild streams");
    }

    @Override
    public void closeStream() throws NoOpenStreamException, IOException, InterruptedException {
        rebuildStreamService.close();
    }

}
