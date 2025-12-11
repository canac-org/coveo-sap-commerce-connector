package com.coveo.stream.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.indexer.service.impl.CoveoObjectTypeSnIndexerValueProvider;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import com.coveo.stream.service.utils.CoveoFieldValueResolverUtils;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import de.hybris.platform.searchservices.enums.SnDocumentOperationType;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoProductStreamServiceStrategyTest {

    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_DE = "de";
    private static final String CURRENCY_USD = "USD";
    private static final String CURRENCY_EUR = "EUR";

    @Mock
    SnLanguage snLanguageEn;
    @Mock
    SnLanguage snLanguageFr;
    @Mock
    SnLanguage snLanguageDe;
    @Mock
    SnCurrency snCurrencyUsd;
    @Mock
    SnCurrency snCurrencyEur;
    @Mock
    CoveoSnCountry coveoSnCountryUs;
    @Mock
    CoveoSnCountry coveoSnCountryFr;
    @Mock
    CoveoSnCountry coveoSnCountryDe;

    @Mock
    CoveoSource coveoSourceUS;
    @Mock
    CoveoSource coveoSourceFR;
    @Mock
    CoveoSource coveoSourceDE;
    @Mock
    CoveoSource coveoSourceAvailability;

    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceUS;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceFR;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceDE;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceAvailability;

    @Mock
    ConfigurationService configurationService;
    @Mock
    CommonI18NService commonI18NService;
    @Mock
    private Configuration configuration;
    @Mock
    SnDocumentOperationType snDocumentOperationType;

    CoveoProductStreamServiceStrategy<CoveoAbstractStreamService<Object>> coveoProductStreamServiceStrategy;

    @Before
    public void setUp() {
        when(snLanguageEn.getId()).thenReturn(LANG_EN);
        when(snLanguageFr.getId()).thenReturn(LANG_FR);
        when(snLanguageDe.getId()).thenReturn(LANG_DE);
        when(snCurrencyUsd.getId()).thenReturn(CURRENCY_USD);
        when(snCurrencyEur.getId()).thenReturn(CURRENCY_EUR);
        when(coveoSnCountryUs.getId()).thenReturn("US");
        when(coveoSnCountryFr.getId()).thenReturn("FR");
        when(coveoSnCountryDe.getId()).thenReturn("DE");

        when(coveoSourceUS.getLanguage()).thenReturn(snLanguageEn);
        when(coveoSourceUS.getCurrency()).thenReturn(snCurrencyUsd);
        when(coveoSourceUS.getCountry()).thenReturn(coveoSnCountryUs);
        when(coveoSourceFR.getLanguage()).thenReturn(snLanguageFr);
        when(coveoSourceFR.getCurrency()).thenReturn(snCurrencyEur);
        when(coveoSourceFR.getCountry()).thenReturn(coveoSnCountryFr);
        when(coveoSourceDE.getLanguage()).thenReturn(snLanguageDe);
        when(coveoSourceDE.getCurrency()).thenReturn(snCurrencyEur);
        when(coveoSourceDE.getCountry()).thenReturn(coveoSnCountryDe);

        when(coveoSourceUS.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceFR.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceDE.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceAvailability.getObjectType()).thenReturn(CoveoCatalogObjectType.AVAILABILITY);

        when(coveoAbstractStreamServiceUS.getCoveoSource()).thenReturn(coveoSourceUS);
        when(coveoAbstractStreamServiceFR.getCoveoSource()).thenReturn(coveoSourceFR);
        when(coveoAbstractStreamServiceDE.getCoveoSource()).thenReturn(coveoSourceDE);
        when(coveoAbstractStreamServiceAvailability.getCoveoSource()).thenReturn(coveoSourceAvailability);

        when(commonI18NService.getLocaleForIsoCode(LANG_EN)).thenReturn(new Locale(LANG_EN));
        when(commonI18NService.getLocaleForIsoCode(LANG_FR)).thenReturn(new Locale(LANG_FR));
        when(commonI18NService.getLocaleForIsoCode(LANG_DE)).thenReturn(new Locale(LANG_DE));

        List<SnLanguage> languages = new ArrayList<>();
        List<SnCurrency> currencies = new ArrayList<>();
        List<CoveoSnCountry> countries = new ArrayList<>();
        List<CoveoAbstractStreamService<Object>> streamServices = new ArrayList<>();

        languages.add(snLanguageEn);
        languages.add(snLanguageFr);
        languages.add(snLanguageDe);
        currencies.add(snCurrencyUsd);
        currencies.add(snCurrencyEur);
        countries.add(coveoSnCountryUs);
        countries.add(coveoSnCountryFr);
        countries.add(coveoSnCountryDe);
        streamServices.add(coveoAbstractStreamServiceUS);
        streamServices.add(coveoAbstractStreamServiceFR);
        streamServices.add(coveoAbstractStreamServiceDE);
        streamServices.add(coveoAbstractStreamServiceAvailability);

        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE)).thenReturn(50);
        coveoProductStreamServiceStrategy = new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, streamServices, configurationService, commonI18NService);
    }

    @Test
    public void testPushDocuments() throws IOException, InterruptedException {

        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.CREATE);
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentB.setOperationType(SnDocumentOperationType.CREATE);
        SnDocumentBatchOperationRequest documentC = new SnDocumentBatchOperationRequest();
        documentC.setDocument(createDocumentFields("nameC", "codeC", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentC.setOperationType(SnDocumentOperationType.CREATE);
        documents.add(documentA);
        documents.add(documentB);
        documents.add(documentC);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            Collection<CountryModel> countryData = new ArrayList<>();
            CountryModel jp = mock(CountryModel.class);
            when(jp.getIsocode()).thenReturn("JP");
            CountryModel us = mock(CountryModel.class);
            when(us.getIsocode()).thenReturn("US");
            countryData.add(jp);
            countryData.add(us);
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), eq(documentC.getDocument().getFields()), any(Locale.class), any(Currency.class))).thenReturn(countryData);
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoClickableUri"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyURL");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(3)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_MissingOneName() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.CREATE);
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE));
        documentB.setOperationType(SnDocumentOperationType.CREATE);
        documents.add(documentA);
        documents.add(documentB);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("").thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
        }
    }

    @Test
    public void testPushDocuments_MissingOneCode() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.CREATE);
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE));
        documentB.setOperationType(SnDocumentOperationType.CREATE);
        documents.add(documentA);
        documents.add(documentB);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("").thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(2)).pushDocument(any());
            verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
        }
    }

    @Test
    public void testCloseServices() throws NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        coveoProductStreamServiceStrategy.closeServices();
        verify(coveoAbstractStreamServiceUS, times(1)).closeStream();
        verify(coveoAbstractStreamServiceFR, times(1)).closeStream();
        verify(coveoAbstractStreamServiceDE, times(1)).closeStream();
        verify(coveoAbstractStreamServiceAvailability, times(0)).closeStream();
    }

    private SnDocument createDocumentFields(String name, String code, String objectType) {
        Map<Locale, Object> localizedName = new HashMap<>();
        SnDocument snDocument = new SnDocument();
        localizedName.put(new Locale(LANG_EN), name);
        localizedName.put(new Locale(LANG_FR), name);
        localizedName.put(new Locale(LANG_DE), name);
        SnField nameField = new SnField();
        nameField.setId("name");
        nameField.setLocalized(true);
        snDocument.setFieldValue(nameField, localizedName);
        SnField codeField = new SnField();
        codeField.setId("code");
        codeField.setLocalized(false);
        snDocument.setFieldValue(codeField, code);
        snDocument.setId(code);
        SnField objectTypeField = new SnField();
        objectTypeField.setId("objectType");
        objectTypeField.setLocalized(false);
        snDocument.setFieldValue(objectTypeField, objectType);
        SnField coveoDocumentIdField = new SnField();
        coveoDocumentIdField.setId("coveoDocumentId");
        coveoDocumentIdField.setLocalized(false);
        snDocument.setFieldValue(coveoDocumentIdField, code);
        return snDocument;
    }

    @Test
    public void testAuthorizedCountries_WhenDataNull() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(null);

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, new CoveoSnCountry());

            assertTrue(result);
        }
    }

    @Test
    public void testAuthorizedCountries_WhenDataNotCollection() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(new Object());

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, new CoveoSnCountry());

            assertTrue(result);
        }
    }

    @Test
    public void testAuthorizedCountries_WhenDataEmptyCollection() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(Collections.EMPTY_LIST);

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, new CoveoSnCountry());

            assertTrue(result);
        }
    }

    @Test
    public void testAuthorizedCountries_WhenDataCollection_HasInvalidObjects() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {

            Collection countryData = new ArrayList();
            countryData.add(new Object());
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(countryData);

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, new CoveoSnCountry());

            assertFalse(result);
        }
    }

    @Test
    public void testAuthorizedCountries_WhenDataCollection_DoesntMatch_RequiredCountry() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {

            Collection<CountryModel> countryData = new ArrayList<>();
            CountryModel jp = mock(CountryModel.class);
            when(jp.getIsocode()).thenReturn("JP");
            CountryModel fr = mock(CountryModel.class);
            when(fr.getIsocode()).thenReturn("FR");
            countryData.add(jp);
            countryData.add(fr);
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(countryData);

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            CoveoSnCountry country = new CoveoSnCountry();
            country.setId("US");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, country);

            assertFalse(result);
        }
    }

    @Test
    public void testAuthorizedCountries_WhenDataCollection_Matches_RequiredCountry() {

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {

            Collection<CountryModel> countryData = new ArrayList<>();
            CountryModel jp = mock(CountryModel.class);
            when(jp.getIsocode()).thenReturn("JP");
            CountryModel us = mock(CountryModel.class);
            when(us.getIsocode()).thenReturn("US");
            countryData.add(jp);
            countryData.add(us);
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(anyString(), anyMap(), any(Locale.class), any(Currency.class))).thenReturn(countryData);

            SnDocumentBatchOperationRequest requst = new SnDocumentBatchOperationRequest();
            SnDocument document = new SnDocument();
            document.setFieldValue(new SnField(), new HashMap<>());
            requst.setDocument(document);

            SnLanguage language = new SnLanguage();
            language.setId("en");

            SnCurrency currency = new SnCurrency();
            currency.setId("USD");

            CoveoSnCountry country = new CoveoSnCountry();
            country.setId("US");

            boolean result = coveoProductStreamServiceStrategy.isApplicableForCountry(requst, language, currency, country);

            assertTrue(result);
        }
    }

    @Test
    public void testAddEcProductIdToValues_WithValidCode() {
        SnDocument document = new SnDocument();
        document.setId("doc1");
        Map<String, Object> documentFields = new HashMap<>();
        documentFields.put("code", "CODE123");
        Locale locale = Locale.ENGLISH;
        Currency currency = Currency.getInstance("USD");
        Map<String, Object> values = new HashMap<>();

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), eq(documentFields), eq(locale), eq(currency))).thenReturn("CODE123");
            CoveoProductStreamServiceStrategy.addEcProductIdToValues(document, documentFields, locale, currency, values);
            assertEquals("CODE123", values.get("ec_product_id"));
        }
    }

    @Test
    public void testAddEcProductIdToValues_WithBlankCode() {
        SnDocument document = new SnDocument();
        document.setId("doc2");
        Map<String, Object> documentFields = new HashMap<>();
        documentFields.put("code", "");
        Locale locale = Locale.ENGLISH;
        Currency currency = Currency.getInstance("USD");
        Map<String, Object> values = new HashMap<>();

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), eq(documentFields), eq(locale), eq(currency))).thenReturn("");
            CoveoProductStreamServiceStrategy.addEcProductIdToValues(document, documentFields, locale, currency, values);
            assertFalse(values.containsKey("ec_product_id"));
        }
    }

    @Test
    public void testAddEcProductIdToValues_WithNullCode() {
        SnDocument document = new SnDocument();
        document.setId("doc3");
        Map<String, Object> documentFields = new HashMap<>();
        Locale locale = Locale.ENGLISH;
        Currency currency = Currency.getInstance("USD");
        Map<String, Object> values = new HashMap<>();

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), eq(documentFields), eq(locale), eq(currency))).thenReturn(null);
            CoveoProductStreamServiceStrategy.addEcProductIdToValues(document, documentFields, locale, currency, values);
            assertFalse(values.containsKey("ec_product_id"));
        }
    }

    @Test
    public void testPushDocuments_PartialUpdate() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceAvailability, times(0)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_PartialUpdate_MissingDocumentId() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_PartialUpdate_MissingDocumentName() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_PartialUpdate_WithException() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        documents.add(documentA);

        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceUS).pushPartialDocument(any());
        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceFR).pushPartialDocument(any());
        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceDE).pushPartialDocument(any());

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
            assertEquals(SnDocumentOperationStatus.FAILED, responses.get(0).getStatus());
        }
    }

    @Test
    public void testPushDocuments_PartialUpdate_EmptyFieldValues() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFieldsWithEmptyValues("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("emptyField"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_MixedOperationTypes() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();

        // Regular document
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.CREATE);
        // Partial update document
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentB.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);

        documents.add(documentA);
        documents.add(documentB);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceUS, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushPartialDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    private SnDocument createDocumentFieldsWithEmptyValues(String name, String code, String objectType) {
        SnDocument snDocument = createDocumentFields(name, code, objectType);

        // Add an empty field for testing
        SnField emptyField = new SnField();
        emptyField.setId("emptyField");
        emptyField.setLocalized(false);
        snDocument.setFieldValue(emptyField, "");

        return snDocument;
    }

    @Test
    public void testPushDocuments_NullOperationType() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(null); // Set operation type to null
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);

            // Verify that no document operations are attempted
            verify(coveoAbstractStreamServiceUS, times(0)).pushDocument(any());
            verify(coveoAbstractStreamServiceUS, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushPartialDocument(any());

            assertEquals(documents.size(), responses.size());
            assertEquals(SnDocumentOperationStatus.FAILED, responses.get(0).getStatus());
        }
    }

    @Test
    public void testPushDocuments_ShallowMerge() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.SHALLOW_MERGE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceAvailability, times(0)).pushShallowMergeDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_ShallowMerge_MissingDocumentId() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.SHALLOW_MERGE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(0)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushShallowMergeDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_ShallowMerge_MissingDocumentName() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.SHALLOW_MERGE);
        documents.add(documentA);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(0)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceFR, times(0)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceDE, times(0)).pushShallowMergeDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }

    @Test
    public void testPushDocuments_ShallowMerge_WithException() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.SHALLOW_MERGE);
        documents.add(documentA);

        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceUS).pushShallowMergeDocument(any());
        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceFR).pushShallowMergeDocument(any());
        doThrow(new IOException("Test exception")).when(coveoAbstractStreamServiceDE).pushShallowMergeDocument(any());

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushShallowMergeDocument(any());
            assertEquals(documents.size(), responses.size());
            assertEquals(SnDocumentOperationStatus.FAILED, responses.get(0).getStatus());
        }
    }

    @Test
    public void testPushDocuments_MixedOperationTypes_WithShallowMerge() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();

        // Regular document
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentA.setOperationType(SnDocumentOperationType.CREATE);
        
        // Partial update document
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentB.setOperationType(SnDocumentOperationType.PARTIAL_UPDATE);
        
        // Shallow merge document
        SnDocumentBatchOperationRequest documentC = new SnDocumentBatchOperationRequest();
        documentC.setDocument(createDocumentFields("nameC", "codeC", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documentC.setOperationType(SnDocumentOperationType.SHALLOW_MERGE);

        documents.add(documentA);
        documents.add(documentB);
        documents.add(documentC);

        try (MockedStatic<CoveoFieldValueResolverUtils> mockedStatic = mockStatic(CoveoFieldValueResolverUtils.class)) {
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("coveoDocumentId"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyDocumentId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("name"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyNameId");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("code"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("dummyCode");
            mockedStatic.when(() -> CoveoFieldValueResolverUtils.resolveFieldValue(eq("objectType"), anyMap(), any(Locale.class), any(Currency.class))).thenReturn("productVariant");

            List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents);
            verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceUS, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceUS, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceFR, times(1)).pushShallowMergeDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushPartialDocument(any());
            verify(coveoAbstractStreamServiceDE, times(1)).pushShallowMergeDocument(any());
            assertEquals(documents.size(), responses.size());
        }
    }
}
