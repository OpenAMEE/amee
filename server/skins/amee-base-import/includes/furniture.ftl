<#function startEndDate date>
    <#return date?string("yyyy-MM-dd'T'HH:mmZ")/>
</#function>

<#function getDateFormat apiVersion>
    <#if apiVersion == "1.0">
        <#return "yyyyMMdd"/>
    <#else>
        <#return "yyyy-MM-dd'T'HH:mmZ"/>
    </#if>
</#function>

<#assign loader = '<img src="/images/ajax-loader.gif"/>'>