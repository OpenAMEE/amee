<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canModify entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.MODIFY)>
</#function>

<#function canCreate entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.CREATE)>
</#function>

<#function canDelete entity>
    <#return authorizationContext.isAuthorized(entity, PermissionEntry.DELETE)>
</#function>