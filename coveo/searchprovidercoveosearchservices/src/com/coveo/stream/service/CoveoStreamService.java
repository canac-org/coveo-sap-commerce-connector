package com.coveo.stream.service;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.PartialUpdateDocument;
import com.coveo.pushapiclient.ShallowMergeDocument;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoSource;

import java.io.IOException;
import java.util.List;

public interface CoveoStreamService {
    public CoveoSource getCoveoSource();

    void pushDocument(DocumentBuilder document) throws IOException, InterruptedException;

    void closeStream() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException;

    void pushPartialDocument(List<PartialUpdateDocument> documents) throws IOException, InterruptedException;

    void pushShallowMergeDocument(List<ShallowMergeDocument> documents) throws IOException, InterruptedException;
}
