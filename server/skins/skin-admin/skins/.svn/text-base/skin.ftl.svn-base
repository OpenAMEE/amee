<#assign sectionName = "skins">
<#include '/includes/before_content.ftl'>
<script type='text/javascript'>
function addOption(theSel, theText, theValue)
{
  var newOpt = new Option(theText, theValue);
  var selLength = theSel.length;
  theSel.options[selLength] = newOpt;
}

function deleteOption(theSel, theIndex)
{ 
  var selLength = theSel.length;
  if(selLength>0)
  {
    theSel.options[theIndex] = null;
  }
}

function moveOptions(theSelFrom, theSelTo)
{
  
  var selLength = theSelFrom.length;
  var selectedText = new Array();
  var selectedValues = new Array();
  var selectedCount = 0;
  
  var i;
  for(i=selLength-1; i>=0; i--)
  {
    if(theSelFrom.options[i].selected)
    {
      selectedText[selectedCount] = theSelFrom.options[i].text;
      selectedValues[selectedCount] = theSelFrom.options[i].value;
      deleteOption(theSelFrom, i);
      selectedCount++;
    }
  }
  
  for(i=selectedCount-1; i>=0; i--)
  {
    addOption(theSelTo, selectedText[i], selectedValues[i]);
  }
}
function selectAllOptions(fm)
{
  var selObj = fm.skin_imports;
  for (var i=0; i<selObj.options.length; i++) {
    selObj.options[i].selected = true;
  }
  fm.submit();
}
</script>
<h2>Skin Administration</h2>
<p><a href='/skins'>Skins</a> / 
   <a href='/skins/${skin.uid}'>Skin</a></p>
<p>
<p><h3>Details</h3>
Name: ${skin.name}<br/>
Path: ${skin.path}<br/>
<#if skin.parentAvailable>
  Extends: <a href='/skins/${skin.parent.uid}'>${skin.parent.path}</a><br/>
</#if>
Created: ${skin.created?string.short}<br/>
Modified: ${skin.modified?string.short}<br/>
</p>
</p>

<#if skin.childrenAvailable>
  <p><h3>Derived Skins</h3>
  <table>
  <tr>
    <th>Path</th>
    <th>Modified</th>
    <th>Action</th>
  </tr>
  <#list skin.children as childSkin>
    <tr>
      <td>${childSkin.path}</td>
      <td>${childSkin.modified?string.short}</td>
      <td>
        <#if skinAdmin.skinActions.allowView>
          <a href='/skins/${childSkin.uid}'>View</a>
        </#if>
      </td>
    </tr>
  </#list>
  </table>
  </p>
</#if>
<#if skinAdmin.skinActions.allowModify>
  <h3>Update</h3>
  <p>
  <form action='/skins/${skin.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${skin.name}' type='text' size='30'/><br/>
  Path: <input name='path' value='${skin.path}' type='text' size='30'/><br/>
  SVN URL: <input name='svnUrl' value='${skin.svnUrl}' type='text' size='60'/><br/>
  SVN Username: <input name='svnUsername' value='${skin.svnUsername}' type='text' size='30'/><br/>
  SVN Password: <input name='svnPassword' value='${skin.svnPassword}' type='text' size='30'/><br/><br/>
  <input type='submit' value="Update"/><br/>
  </form>
  </p>
    
  <h3>Parent</h3>
  <p>
  <form action='/skins/${skin.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  <select name='parent'>
  <option value='root' <#if !skin.parentAvailable>selected='selected'</#if>>root</option> 
   <#list skins as sk>
    <#if sk.uid != skin.uid>
       <option value='${sk.uid}'<#if skin.parentAvailable><#if skin.parent.uid = sk.uid> selected='selected'</#if></#if>>${sk.path}</option>
    </#if>
    </#list>
  </select><br/><br/>
  <input type='submit' value="Update"/><br/>
  </form>
  </p>

  <h3>Imports</h3>
  <p>
    <form action='/skins/${skin.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  <table border='0'>
   <tr>
    <td>
     <select name='skin_imports' size='10' multiple='multiple'>
       <#list skin.importedSkins as isk>
        <option value='${isk.uid}'>${isk.path}</option>
       </#list>
     </select>
   </td>
   <td align='center' valign='middle'>
    <input type='button' value='--&gt;'
      onclick='moveOptions(this.form.skin_imports, this.form.sel2);' /><br />
    <input type='button' value='&lt;--'
      onclick='moveOptions(this.form.sel2, this.form.skin_imports);' />
   </td>
   <td>
    <select name='sel2' size='10' multiple='multiple'>
       <#list skins as osk>
       <#if !skin.importedSkins?seq_contains(osk)>
       <#if skin.uid != osk.uid>
        <option value='${osk.uid}'>${osk.path}</option>
       </#if>
       </#if>
       </#list>
    </select>
   </td>
  </tr>
</table><br/>
<input type='button' onclick='selectAllOptions(this.form)' value="Update"/><br/>
  </form>
  </p>
</#if>

<#if skinAdmin.skinActions.allowModify>
  <h3>Import Skin File Content from SVN</h3>
  <p>
  <form action='/skins/${skin.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  <input name='svnImport' value='true' type='hidden'/>
  <input type='submit' value="SVN Import"/><br/><br/>
  </form>
  </p>
</#if>

<#include '/includes/after_content.ftl'>