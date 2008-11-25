<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<h1>App Administration</h1>
<p><a href='/apps'>Apps</a> / 
   <a href='/apps/${app.uid}'>${app.name}</a> / 
   <a href='/apps/${app.uid}/targets'>Targets</a> / 
   <a href='/apps/${app.uid}/targets/${target.name}'>${target.name}</a></p>
<h2>Details</h2>
<#if browser.appActions.allowView>
<p>
Name: ${target.name}<br/>
URI Pattern: ${target.uriPattern}<br/>
Target: ${target.target}<br/>
Created: ${target.created?string.short}<br/>
Modified: ${target.modified?string.short}<br/>
</p>
</#if> 
<#if browser.appActions.allowModify>
  <h2>Update Target</h2>
  <p>
  <form action='/apps/${app.uid}/targets/${target.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Type: <select name='type'> <br/>
    <option value='SEAM_COMPONENT_RESOURCE' <#if target.type == 'SEAM_COMPONENT_RESOURCE'>selected='selected'</#if>>Seam Component Resource</option><br/>
    <option value='DIRECTORY_RESOURCE' <#if target.type == 'DIRECTORY_RESOURCE'>selected='selected'</#if>>Directory Resource</option><br/>
    <option value='RESTLET_CLASS' <#if target.type == 'RESTLET_CLASS'>selected='selected'</#if>>Restlet Class</option><br/>
    </select><br/>
  Name: <input name='name' value='${target.name}' type='text' size='30'/><br/>
  URI Pattern: <input name='uriPattern' value='${target.uriPattern}' type='text' size='60'/><br/>
  Target: <input name='target' value='${target.target}' type='text' size='60'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${target.description}</textarea><br/>
  Default:
    Yes <input type="radio" name="defaultTarget" value="true"<#if target.defaultTarget> checked</#if>/>
    No <input type="radio" name="defaultTarget" value="false"<#if !target.defaultTarget> checked</#if>/><br/>
  Enabled:
    Yes <input type="radio" name="enabled" value="true"<#if target.enabled> checked</#if>/>
    No <input type="radio" name="enabled" value="false"<#if !target.enabled> checked</#if>/><br/>
  <br/><input type='submit' value="Update Target"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>