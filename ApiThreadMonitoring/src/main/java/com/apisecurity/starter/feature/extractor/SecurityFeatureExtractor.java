package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;

public interface SecurityFeatureExtractor {

    /**
     * Extracts features from the given event and merges them into the feature set.
     *
     * @param event      The raw API security event.
     * @param featureSet The feature set being built.
     */
    void extract(ApiSecurityEvent event, SecurityFeatureSet.SecurityFeatureSetBuilder featureSet);
}
