<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canView>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.VIEW)>
</#function>

<#function canViewEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.VIEW)>
</#function>

<#function canCreate>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.CREATE)>
</#function>

<#function canCreateEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.CREATE)>
</#function>

<#function canCreateProfile>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.CREATE_PROFILE)>
</#function>

<#function canModify>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.MODIFY)>
</#function>

<#function canModifyEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.MODIFY)>
</#function>

<#function canDelete>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.DELETE)>
</#function>

<#function canDeleteEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.DELETE)>
</#function>

<#function renderPermissionEntry pe>
    <#assign r = ''>
    <#if pe.allow>
        <#assign r = r + '+'>
    <#else>
        <#assign r = r + '-'>
    </#if>
    <#if pe.value == 'o'>
        <#assign r = r + 'OWN'>
    <#elseif pe.value == 'v'>
        <#assign r = r + 'VIEW'>
    <#elseif pe.value == 'c'>
        <#assign r = r + 'CREATE'>
    <#elseif pe.value == 'c.pr'>
        <#assign r = r + 'CREATE_PROFILE'>
    <#elseif pe.value == 'm'>
        <#assign r = r + 'MODIFY'>
    <#elseif pe.value == 'd'>
        <#assign r = r + 'DELETE'>
    <#else>
        <#assign r = r + pe.value?upper_case>
    </#if>
    <#if pe.status != AMEEStatus.ACTIVE>
        <#assign r = r + '_' + pe.status>
    </#if>
    <#return r>
</#function>
