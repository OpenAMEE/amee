package com.amee.domain;

import com.amee.domain.data.LocaleName;

import java.util.Map;

/**
 * Some entities will expect a bean implementing this bean to be present.
 */
public interface ILocaleService {

    public Map<String, LocaleName> getLocaleNames(IAMEEEntityReference entity);

    public LocaleName getLocaleName(IAMEEEntityReference entity);

    public LocaleName getLocaleName(IAMEEEntityReference entity, String locale);

    public String getLocaleNameValue(IAMEEEntityReference entity);

    public String getLocaleNameValue(IAMEEEntityReference entity, String defaultName);

    public void setLocaleName(IAMEEEntityReference entity, String locale, String name);
}