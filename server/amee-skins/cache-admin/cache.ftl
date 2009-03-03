<#include '/includes/top.ftl'>
<head>
    <#include '/includes/head.ftl'>
</head>
<body>
<!-- start outer -->
<div id="outer">
<h2>Cache Administration</h2>
<p><a href='/'>Home</a> /
   <a href='/cache'>Caches</a> /
   <a href='/cache/${cache.name?html}'>${cache.name?html}</a>
<h3>Cache: ${cache.name?html}</h3>
<#if authService.hasActions("cache.view")>
<p>
Size: ${cache.size}<br/>
Memory Store Size: ${cache.memoryStoreSize}<br/>
Disk Store Size: ${cache.diskStoreSize}<br/>
Objects in Cache: ${stats.objectCount}<br/>
Max Elements in Memory: ${cache.maxElementsInMemory}<br/>
Cache Hits: ${stats.cacheHits}<br/>
Cache Misses: ${stats.cacheMisses}<br/>
Cache Hits In Memory: ${stats.inMemoryHits}<br/>
Cache Hits On Disk: ${stats.onDiskHits}<br/>
</p>
</#if>
<#if authService.hasActions("cache.modify")>
<p>
  <form action="/cache/${cache.name}" method="POST" enctype="application/x-www-form-urlencoded">
    <input type="hidden" value="invalidate" name="invalidate"/>
    <input type="submit" value="Invalidate"/><br/>
  </form>
</p>
</#if>
<h3>Keys</h3>
<#if authService.hasActions("cache.view")>
<p>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>
<table>
<tr>
<#list sheet.columns as column>
  <#if cacheSort.sortBy == column.name>
        <#assign pre = "<strong>">
        <#assign post = "</strong>">
        <#assign sortOrder = sheet.getColumn(column.name).newSortOrder>
    <#else>
        <#assign pre = "">
        <#assign post = "">
        <#assign sortOrder = sheet.getColumn(column.name).sortOrder>
    </#if>
  <th><a href="/cache/${cache.name}?sortBy=${column.name?html}&sortOrder=${sortOrder?html}">${column.name?html}</a></th>
</#list>
</tr>
<#list sheet.rows as row>
  <tr>
    <#list sheet.columns as column>
        <#assign cell = row.findCell(column)>
        <#if cell.valueType == 'DATE'>
            <td>${cell.valueAsDate?datetime?string.short}</td>
        <#else>
            <td>${cell}</td>
        </#if>
    </#list>
  </tr>
</#list>
</table>
<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
</p>
</#if>
</div>
<!-- end outer -->
<#include '/includes/bottom.ftl'>