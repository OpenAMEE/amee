package com.amee.domain;

/**
 * Some entities will expect a bean implementing this bean to be present.
 */
public interface IMetadataService {

    public Metadata getMetadataForEntity(IAMEEEntityReference entity, String name);

    public void persist(Metadata metadata);
}
