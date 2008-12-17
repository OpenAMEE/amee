<#include 'dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<h1>Data Item Value</h1>
<#include 'dataTrail.ftl'>
<h2>Data Item Value Details</h2>
<p>Value: ${itemValue.value}<br/>
   Full Path: ${browser.fullPath}<br/>
   Item Value Definition: ${itemValue.itemValueDefinition.name}<br/>
   Value Definition: ${itemValue.itemValueDefinition.valueDefinition.name}<br/>
   Value Type: ${itemValue.itemValueDefinition.valueDefinition.valueType}<br/>
   Item: ${itemValue.item.label}<br/>
   Environment: ${itemValue.item.environment.name}<br/>
   UID: ${itemValue.uid}<br/>
   Created: ${itemValue.created?string.short}<br/>
   Modified: ${itemValue.modified?string.short}<br/>
</p>
<#if browser.dataItemActions.allowModify>
  <h2>Update Data Item Value</h2>
  <p>
  <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Value:
  <#if itemValue.itemValueDefinition.choicesAvailable>
    <select name='value'>
    <#list itemValue.itemValueDefinition.choiceList as choice>
      <option value='${choice.value}'<#if itemValue.value == choice.value> selected</#if>>${choice.name}</option>
    </#list>
    </select>
  <#else>
    <input name='value' value='${itemValue.value}' type='text' size="30"/><br/>
    <#if itemValue.hasUnits()>
        Unit: <input name='unit' value='${itemValue.unit}' type='text' size="30"/><br/>
    </#if>
    <#if itemValue.hasPerUnits()>
        PerUnit: <input name='perUnit' value='${itemValue.perUnit}' type='text' size="30"/><br/>
    </#if>
  </#if><br/><br/>
  <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>