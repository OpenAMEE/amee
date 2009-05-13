<#assign sectionName = "profiles">
<#if profile?? && pathItem??>
    <#assign basePath = "/profiles/" + profile.displayPath + pathItem.fullPath>
<#else>
    <#assign basePath = "/profiles">
</#if>
<#assign pageTitle = "AMEE - Profiles - " + basePath>