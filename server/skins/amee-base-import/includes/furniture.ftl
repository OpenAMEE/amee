<#function startEndDate date>
    <#return date?string("yyyy-MM-dd'T'HH:mmZ")/>
</#function>

<#function getDateFormat apiVersion>
    <#if apiVersion == "1.0">
        <#return "yyyymmddThhmm"/>
    <#else>
        <#return "yyyy-MM-dd'T'HH:mmZ"/>
    </#if>
</#function>