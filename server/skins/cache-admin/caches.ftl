<#include '/includes/top.ftl'>
<head>
    <#include '/includes/head.ftl'>
</head>
<body>

<!-- start outer -->
<div id="outer">

<h2>Cache Administration</h2>

<p><a href='/'>Home</a> /
    <a href='/cache'>Caches</a>

<h3>All Caches</h3>

<#if authService.hasActions("cache.list")>

<#assign pagerItemsLabel = 'caches'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<p>
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
  <th><a href="/cache?sortBy=${column.name}&sortOrder=${sortOrder}">${pre}${column.name}${post}</a></th>
</#list>
</tr>
<#list sheet.rows as row>
  <tr>
    <#list sheet.columns as column>
      <td>${row.findCell(column)}</a></td>
    </#list>
  </tr>
</#list>
</table>
</p>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</#if>
<p><strong>Note:</strong> This is a list of current caches on just *this* server.
    You may need to access this page via a dedicated server URL to get meaningful results.</p>
<#if authService.hasActions("cache.modify")>
<h3>Actions</h3>

<p>
  <form action="/cache" method="POST" enctype="application/x-www-form-urlencoded">
    <input type="hidden" value="invalidate" name="invalidate"/>
    <input type="submit" value="Invalidate All Caches"/><br/>
  </form>
</p>
</#if>
</div>
<!-- end outer -->
<#include '/includes/bottom.ftl'>