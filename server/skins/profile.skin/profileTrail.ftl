<#if activeUser.apiVersion.versionOne>
    <p><a href='/profiles'>Profiles</a> / <a href='/profiles/${profile.displayPath}'>${profile.displayPath}</a><#list browser.pathItem.pathItems as p><#if p.path != ''> / <a href='/profiles/${profile.displayPath}${p.fullPath}'>${p.name}</a></#if></#list></p>
<#else>
    <p id="apiTrail"/></p>
</#if>