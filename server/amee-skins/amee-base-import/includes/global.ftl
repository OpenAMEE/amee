<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canView entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.VIEW)>
</#function>

<#function canCreate entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.CREATE)>
</#function>

<#function canModify entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.MODIFY)>
</#function>

<#function canDelete entity>
    <#return authorizationService.isAuthorized(authorizationContext, entity, PermissionEntry.DELETE)>
</#function>