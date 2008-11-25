<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteApp(appUid) {
  resourceUrl = '/apps/' + appUid + '?method=delete';
  resourceElem = $('Elem_' + appUid);
  resourceType = 'App'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>App Administration</h1>

<p><a href='/apps'>Apps</a></p>


<#if browser.appActions.allowList>
<h2>Apps</h2>
<p>

<#assign pagerItemsLabel = 'apps'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Action</th>
</tr>
<#list apps as a>
  <tr id='Elem_${a.uid}'>
    <td>${a.name}</td>
    <td>
        <#if browser.appActions.allowView><a href='/apps/${a.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.appActions.allowDelete><input type="image" onClick="deleteApp('${a.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#if browser.appActions.allowCreate>
  <h2>Create App</h2>
  <p>
  <form action='/apps' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' type='text' size='30'/><br/><br/>
  <input type='submit' value="Create App"/><br/>
  </form>
  </p>
</#if>

<#include '/includes/after_content.ftl'>