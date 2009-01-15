<#function startEndDate date>
    <#return date?string("yyyyMMdd'T'HHmm")/>
</#function>

<#function getDateFormat apiVersion>
    <#if apiVersion == "1.0">
        <#return "yyyymmddThhmm"/>
    <#else>
        <#return "yyyy-MM-dd'T'HH:mmZ"/>
    </#if>
</#function>