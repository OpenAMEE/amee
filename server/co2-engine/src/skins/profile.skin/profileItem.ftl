<#include 'profileCommon.ftl'>
<#include '/includes/before_content.ftl'>
<h1>Profile Item</h1>
<#include 'profileTrail.ftl'>
<h2>Profile Item Details</h2>
<p><#if profileItem.name != ''>Name: ${profileItem.name}<br/></#if>
   kgCO2 pcm: ${profileItem.amountPerMonth}<br/>
   Valid From: ${profileItem.validFromFormatted}<br/>
   End: <#if profileItem.end>Yes<#else>No</#if><br/>
   Full Path: ${browser.fullPath}<br/>
   Data Item Label: ${profileItem.dataItem.label}<br/>
   Item Definition: ${profileItem.itemDefinition.name}<br/>
   Environment: ${profileItem.environment.name}<br/>
   UID: ${profileItem.uid}<br/>
   Created: ${profileItem.created?string.short}<br/>
   Modified: ${profileItem.modified?string.short}<br/>
</p>
<h2>Profile Item Values</h2>
<p>
<form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
<table>
<tr>
  <th>Name</th>
  <th>Value</th>
</tr>
<tr>
  <td>Name</td>
  <#if browser.profileItemActions.allowModify>
    <td><input name='name' value='${profileItem.name}' type='text' size='30'/><br/>
  <#else>
    <td>${profileItem.name}</td>
  </#if>
</tr>
<tr>
  <td>Valid From</td>
  <#if browser.profileItemActions.allowModify>
    <td><input name='validFrom' value='${profileItem.validFromFormatted}' type='text' size='10'/> (yyyyMMdd)<br/>
  <#else>
    <td>${profileItem.validFromFormatted}</td>
  </#if>
</tr>
<tr>
  <td>End Marker</td>
  <#if browser.profileItemActions.allowModify>
    <td><select name='end'>
          <option value='true'<#if profileItem.end> selected</#if>>Yes</option>
          <option value='false'<#if !profileItem.end> selected</#if>>No</option>
        </select></td>
  <#else>
    <td>${profileItem.end}</td>
  </#if>
</tr>
<#if 0 != profileItem.itemValues?size>
  <#list profileItem.itemValues as iv>
    <tr>
      <#if browser.profileItemActions.allowModify>
        <td><a href='${basePath}/${iv.displayPath}'>${iv.displayName}</a></td>
        <td>
          <#if iv.itemValueDefinition.choicesAvailable>
            <select name='${iv.displayPath}'>
            <#list iv.itemValueDefinition.choiceList as choice>
              <option value='${choice.value}'<#if iv.value == choice.value>selected</#if>>${choice.name}</option>
            </#list>
            </select>
          <#else>
            <input name='${iv.displayPath}' value='${iv.value}' type='text' size="30"/>
          </#if></td>
      <#else>
        <td>${iv.displayName}</td>
        <td>${iv.value}</td>
      </#if>
    </tr>
  </#list>
</#if>
</table>
</table>
<#if browser.profileItemActions.allowModify>
  <br/>
  <input type='submit' value='Update'/>
</#if>
</form>
</p>
<#include '/includes/after_content.ftl'>