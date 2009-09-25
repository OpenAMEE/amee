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