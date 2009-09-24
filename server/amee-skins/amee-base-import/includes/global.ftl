<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canView>
    <#return authorizationContext.isAuthorized(PermissionEntry.VIEW)>
</#function>

<#function canViewEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.VIEW, entity)>
</#function>

<#function canCreate>
    <#return authorizationContext.isAuthorized(PermissionEntry.CREATE)>
</#function>

<#function canCreateEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.CREATE, entity)>
</#function>

<#function canModify>
    <#return authorizationContext.isAuthorized(PermissionEntry.MODIFY)>
</#function>

<#function canModifyEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.MODIFY, entity)>
</#function>

<#function canDelete>
    <#return authorizationContext.isAuthorized(PermissionEntry.DELETE)>
</#function>

<#function canDeleteEntity entity>
    <#return authorizationService.isAuthorized(authorizationContext, PermissionEntry.DELETE, entity)>
</#function>