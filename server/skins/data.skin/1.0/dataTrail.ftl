<#if browser.pathItem??><p><a href='/data'>Data</a><#list browser.pathItem.pathItems as p><#if p.path != ''> / <a href='/data${p.fullPath}'>${p.name}</#if></a></#list></p></#if>
