<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canView entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.VIEW)>
</#function>

<#function canCreate entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.CREATE)>
</#function>

<#function canModify entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.MODIFY)>
</#function>

<#function canDelete entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.DELETE)>
</#function>