<#assign sectionName = "data">
<#if node?? && node.fullPath??>
    <#assign basePath = "/data" + node.fullPath>
<#else>
    <#assign basePath = "/data">
</#if>
<#assign pageTitle = "AMEE - Data - " + basePath>