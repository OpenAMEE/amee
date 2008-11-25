<#assign sectionName = "skins">
<#include '/includes/before_content.ftl'>
<h2>Skin Administration</h2>
<p><a href='/skins'>Skins</a></p>
<#if skinAdmin.skinActions.allowList>
<p>
<table>
<tr>
  <th>Path</th>
  <th>Extends</th>
  <th>Modified</th>
  <th>SVN import</th>
</tr>
<#list skins as skin>
 <form action="/skins/${skin.uid}?method=put" method="POST" enctype="application/x-www-form-urlencoded">
  <input name="svnImport" value="true" type="hidden"/> 
  <tr>
    <td><a href='/skins/${skin.uid}'>${skin.path}</a></td>
    <td>
      <#if skin.parentAvailable>
        <a href='/skins/${skin.parent.uid}'>${skin.parent.path}</a>
      <#else>
        (root)
      </#if>
    </td>
    <td>${skin.modified?string.short}</td>
	<td>
  <input type="submit" class="inputSubmitStd noMargin" value="SVN Import"/>
 </td>
  </tr> </form>
</#list>
</table>
</p>
<#if skinAdmin.skinActions.allowCreate>
  <h3>Create Skin</h3>
  <p>
  <form action='/skins' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' type='text' size='30'/><br/>
  Path: <input name='path' type='text' size='30'/><br/><br/>
  <input type='submit' value="Create Skin"/><br/>
  </form>
  </p>
</#if>
</#if>
<#include '/includes/after_content.ftl'>