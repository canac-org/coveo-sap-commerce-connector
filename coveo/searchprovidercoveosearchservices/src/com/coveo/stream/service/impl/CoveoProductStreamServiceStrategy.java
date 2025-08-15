package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.PartialUpdateDocument;
import com.coveo.pushapiclient.PartialUpdateOperator;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoStreamService;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
import com.coveo.stream.service.utils.CoveoFieldValueResolverUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.*;

public class CoveoProductStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {

    private static final Logger LOG = Logger.getLogger(CoveoProductStreamServiceStrategy.class);

    private static final String PARTIAL_UPDATE = "PARTIAL_UPDATE";
    private static final String COVEO_NAME_INDEX_ATTRIBUTE = "name";

    List<SnLanguage> languages;
    List<SnCurrency> currencies;
    List<CoveoSnCountry> countries;
    List<T> streamServices;

    private final ConfigurationService configurationService;
    private final CommonI18NService commonI18NService;

    public CoveoProductStreamServiceStrategy(List<SnLanguage> languages, List<SnCurrency> currencies,
                                             List<CoveoSnCountry> countries, List<T> incomingStreamServices,
                                             ConfigurationService configurationService, CommonI18NService commonI18NService) {
        this.languages = languages;
        this.currencies = currencies;
        this.countries = countries;
        this.streamServices = new ArrayList<>();
        incomingStreamServices.forEach(streamService -> {
            CoveoSource coveoSource = streamService.getCoveoSource();
            if (coveoSource.getObjectType().equals(CoveoCatalogObjectType.PRODUCTANDVARIANT)) {
                if (LOG.isDebugEnabled()) LOG.debug("Adding stream service based on source " + coveoSource.getId());
                streamServices.add(streamService);
            }
        });
        this.configurationService = configurationService;
        this.commonI18NService = commonI18NService;
    }

    @Override
    public List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents) {
        Map<String, SnDocumentBatchOperationResponse> responseMap = new HashMap<>();
        if(LOG.isDebugEnabled()) LOG.debug("Streaming Documents");
        int logIntervalPercentage = configurationService.getConfiguration().getInt(COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE);
        if (logIntervalPercentage < 0 || logIntervalPercentage > 100) {
            LOG.warn("Log interval percentage is out of range (0-100%). Using default of 20%.");
            logIntervalPercentage = COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT;
        }
        for (T streamService : streamServices) {
            CoveoSource source = streamService.getCoveoSource();
            if (isSourceConfiguredForJob(source)) {
                int totalDocumentsCount = documents.size();
                int logInterval = (int) Math.ceil(totalDocumentsCount * (logIntervalPercentage / 100.0));
                LOG.info(String.format("Streaming %s documents for source %s", totalDocumentsCount, source.getId()));
                for (int documentIndex = 1; documentIndex <= totalDocumentsCount; documentIndex++) {
                    SnDocumentBatchOperationRequest request = documents.get(documentIndex - 1);
                    SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
                    documentBatchOperationResponse.setId(request.getDocument().getId());
                    documentBatchOperationResponse.setStatus(streamDocument(request, source.getLanguage(), source.getCurrency(), source.getCountry(), streamService) ? SnDocumentOperationStatus.UPDATED : SnDocumentOperationStatus.FAILED);
                    if (!responseMap.containsKey(documentBatchOperationResponse.getId()) || documentBatchOperationResponse.getStatus() == SnDocumentOperationStatus.FAILED) {
                        responseMap.put(documentBatchOperationResponse.getId(), documentBatchOperationResponse);
                    }
                    if (logInterval != 0 && documentIndex % logInterval == 0) {
                        LOG.info(String.format("Processed %s of %s documents", documentIndex, totalDocumentsCount));
                    }
                }
            }
        }
        LOG.info(String.format("Finished streaming %s documents", responseMap.size()));
        return new ArrayList<>(responseMap.values());
    }

    private boolean isSourceConfiguredForJob(CoveoSource source) {
        return languages.contains(source.getLanguage()) && currencies.contains(source.getCurrency()) && countries.contains(source.getCountry());
    }

    private boolean streamDocument(SnDocumentBatchOperationRequest request, SnLanguage language, SnCurrency currency, CoveoSnCountry country, T streamService) {
        boolean success = true;
        if (request.getOperationType() == null) {
            LOG.error("Document operation type is null for document " + request.getDocument().getId());
            return false;
        }
        if (isApplicableForCountry(request, language, currency, country)) {
            synchronized (streamService) {
                if (PARTIAL_UPDATE.equals(request.getOperationType().getCode())) {
                    List<PartialUpdateDocument> partialUpdateDocuments = createCoveoPartialDocument(request.getDocument(), language.getId(), currency.getId());
                    if (!partialUpdateDocuments.isEmpty()) {
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Pushing partial update for document " + request.getDocument().getId());
                            }
                            streamService.pushPartialDocument(partialUpdateDocuments);
                        } catch (IOException | InterruptedException exception) {
                            success = false;
                            LOG.error("Failed to index " + request.getDocument().getId(), exception);
                        }
                    } else {
                        LOG.error("Failed to index " + request.getDocument().getId());
                        success = false;
                    }
                } else {
                    DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument(), language.getId(), currency.getId());
                    if (coveoDocument != null) {
                        try {
                            if (LOG.isDebugEnabled()) {
                                JsonObject jsonDocument = (new Gson()).toJsonTree(coveoDocument.getDocument()).getAsJsonObject();
                                LOG.debug("Pushing document: " + jsonDocument.toString());
                            }
                            streamService.pushDocument(coveoDocument);
                        } catch (IOException | InterruptedException exception) {
                            success = false;
                            LOG.error("Failed to index " + request.getDocument().getId(), exception);
                        }
                } else {
                    LOG.error("Failed to index " + request.getDocument().getId());
                    success = false;
                }
            }
        }
        return success;
    }

    protected boolean isApplicableForCountry(SnDocumentBatchOperationRequest request, SnLanguage language,
                                                    SnCurrency currency, CoveoSnCountry country) {
        Object authorizedCountries = CoveoFieldValueResolverUtils.resolveFieldValue(
                "coveoAuthorizedCountries",
                request.getDocument().getFields(),
                new Locale(language.getId()),
                Currency.getInstance(currency.getId())
        );

        // skip as default behavior is to send to all sources
        if (authorizedCountries == null) {
            return true;
        }

        if (!(authorizedCountries instanceof Collection<?>)) {
            LOG.warn("Document " + request.getDocument().getId() + " has an invalid coveoAuthorizedCountries field. This must be a collection of CountryModel objects.");
            return true;
        }

        Collection<?> countriesToCheck = (Collection<?>) authorizedCountries;
        if (countriesToCheck.isEmpty()) {
            return true;
        }

        for (Object countryToCheck : countriesToCheck) {
            if (countryToCheck instanceof CountryModel) {
                CountryModel countryModel = (CountryModel) countryToCheck;
                if (countryModel.getIsocode().equalsIgnoreCase(country.getId())) {
                    return true;
                }
            } else {
                LOG.warn("Document " + request.getDocument().getId() + " has an invalid country object " + countryToCheck + " in the coveoAuthorizedCountries field. This must be a CountryModel object.");
            }
        }

        LOG.debug("Document " + request.getDocument().getId() + " is not authorized for country " + country.getId());
        return false;
    }

    private DocumentBuilder createCoveoDocument(SnDocument document, String languageIsoCode, String currencyIsoCode) {
        DocumentInfos documentInfos = getInfosAndValidateDocument(document, languageIsoCode, currencyIsoCode);
        if (!documentInfos.isValid()) {
            return null;
        }

        Map<String, Object> values = resolveFieldValue(document, documentInfos);
        addEcProductIdToValues(document, document.getFields(), documentInfos.locale(), documentInfos.currency(), values);
        DocumentBuilder documentBuilder = new DocumentBuilder(documentInfos.documentId(), documentInfos.documentName()).withMetadata(values);

        String coveoClickableUri = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_URI_TYPE_INDEX_ATTRIBUTE, document.getFields(), documentInfos.locale(), documentInfos.currency());
        if (!StringUtils.isBlank(coveoClickableUri)) {
            documentBuilder.withClickableUri(coveoClickableUri);
        }
        return documentBuilder;
    }

    private List<PartialUpdateDocument> createCoveoPartialDocument(SnDocument document, String languageIsoCode, String currencyIsoCode) {
        DocumentInfos documentInfos = getInfosAndValidateDocument(document, languageIsoCode, currencyIsoCode);
        if (!documentInfos.isValid()) {
            return new ArrayList<>();
        }

        List<PartialUpdateDocument> partialUpdateDocuments = new ArrayList<>();
        List<String> partialFieldsToUpdate = document.getFields().keySet().stream().filter(key -> !COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE.equals(key) && !COVEO_NAME_INDEX_ATTRIBUTE.equals(key)).toList();

        for (String partialFieldToUpdate : partialFieldsToUpdate) {
            Map<String, Object> values = resolveFieldValue(document, documentInfos);
            // Only fieldValueReplace is supported for now
            partialUpdateDocuments.add(new PartialUpdateDocument(documentInfos.documentId(), PartialUpdateOperator.FIELDVALUEREPLACE, partialFieldToUpdate, values.get(partialFieldToUpdate)));
        }

        return partialUpdateDocuments;
    }

    private Map<String, Object> resolveFieldValue(SnDocument document, DocumentInfos documentInfo) {
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, Object> field : document.getFields().entrySet()) {
            Object fieldValue = CoveoFieldValueResolverUtils.resolveFieldValue(field.getValue(), documentInfo.locale(), documentInfo.currency());
            if (fieldValue != null && !Objects.equals(fieldValue, "")) {
                values.put(field.getKey(), fieldValue);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Field " + field.getKey() + " is empty or null, will not push this field for document " + documentInfo.documentId());
            }
        }
        return values;
    }

    private DocumentInfos getInfosAndValidateDocument(SnDocument document, String languageIsoCode, String currencyIsoCode) {
        Locale locale = commonI18NService.getLocaleForIsoCode(languageIsoCode);
        Currency currency = Currency.getInstance(currencyIsoCode);
        Map<String, Object> documentFields = document.getFields();

        String documentId = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE, documentFields, locale, currency);
        if (StringUtils.isBlank(documentId)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a " + COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE + " field, will not push this document");
            return new DocumentInfos(false, null, null, null, null);
        }

        String documentName = (String) CoveoFieldValueResolverUtils.resolveFieldValue("name", documentFields, locale, currency);
        if (StringUtils.isBlank(documentName)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a name field, will not push this document");
            return new DocumentInfos(false, null, null, null, null);
        }

        return new DocumentInfos(true, documentId, documentName, locale, currency);
    }

    private record DocumentInfos(boolean isValid, String documentId, String documentName, Locale locale, Currency currency) {
    }

    protected static void addEcProductIdToValues(SnDocument document, Map<String, Object> documentFields, Locale locale, Currency currency, Map<String, Object> values) {
        String documentCode = (String) CoveoFieldValueResolverUtils.resolveFieldValue("code", documentFields, locale, currency);
        if (StringUtils.isNotBlank(documentCode)) {
            values.put("ec_product_id", documentCode);
        } else {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a code field, will not push ec_product_id field");

        }
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        if (LOG.isDebugEnabled()) LOG.debug("Closing stream services");
        for (T streamService : streamServices) {
            streamService.closeStream();
        }
    }
}
