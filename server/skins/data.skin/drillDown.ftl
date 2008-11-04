<#include 'dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<h1>Data Category Drill Down</h1>
<#include 'dataTrail.ftl'>
<h2>Selections</h2>
<p>
<table>
<tr>
    <th>Name</th>
    <th>Value</th>
</tr>
<#assign choiceUrl = '${basePath}/drill?'>
<#list selections as selection>
  <#assign choiceUrl = choiceUrl + '${selection.name?url}=${selection.value?url}&'>
  <tr>
    <td>${selection.name}</td>
    <td>${selection.value}</td>
  </tr>
</#list>
</table>
</p>
<h2>Choices for ${choices.name}</h2>
<p>
<table>
<tr>
    <th>Name</th>
    <th>Value</th>
</tr>
<#list choices.choices as choice >
  <tr>
    <td>${choice.name}</td>
    <td><a href='${choiceUrl}${choices.name?url}=${choice.value?url}'>${choice.name}</a></td>
  </tr>
</#list>
</table>
</p>
<#include '/includes/after_content.ftl'>