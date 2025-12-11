package com.coveo.facades;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenUserId;
import com.coveo.SearchTokenWsDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface SearchTokenFacade {

    ResponseEntity<SearchTokenWsDTO> getSearchToken(String baseSiteId, String userId, long maxAgeMilliseconds, String userAgent);

    Set<String> getUserPriceGroups(String userId);

    HttpHeaders buildHeaders(String coveoApiKey, String userAgent);

    SearchTokenBody buildRequestBody(String userId, Set<String> userGroupIds);

    SearchTokenUserId createSearchTokenUserId(String name, String type);

}
