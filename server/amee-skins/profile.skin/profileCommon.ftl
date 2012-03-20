<#assign sectionName = "profiles">
<#if profile?? && node??>
    <#assign basePath = "/profiles/" + profile.displayPath + node.fullPath>
<#else>
    <#assign basePath = "/profiles">
</#if>
<#assign pageTitle = "AMEE - Profiles - " + basePath>