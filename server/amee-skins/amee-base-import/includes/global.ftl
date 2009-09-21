<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<#assign logo = false>
<#assign supportDetails = false>
<#assign footerCopy = "">

<#function canModify entity>
    <#return entity.accessSpecification?? && entity.accessSpecification.actual?seq_contains(PermissionEntry.MODIFY)>
</#function>

<#function canCreate entity>
    <#return entity.accessSpecification?? && entity.accessSpecification.actual?seq_contains(PermissionEntry.CREATE)>
</#function>

<#function canDelete entity>
    <#return entity.accessSpecification?? && entity.accessSpecification.actual?seq_contains(PermissionEntry.DELETE)>
</#function>