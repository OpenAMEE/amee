<#assign sectionName = "data">
<#if pathItem??>
    <#assign basePath = "/data" + pathItem.fullPath>
<#else>
    <#assign basePath = "/data">
</#if>