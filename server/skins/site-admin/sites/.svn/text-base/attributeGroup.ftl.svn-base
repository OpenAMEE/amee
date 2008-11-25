<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroups'>Attribute Groups</a> /
   <a href='/environments/${environment.uid}/attributeGroups/${attributeGroup.uid}'>${attributeGroup.name}</a></p>
<h2>Details</h2>
<#if browser.attributeGroupActions.allowView>
<p>
Name: ${attributeGroup.name}<br/>
Created: ${attributeGroup.created?string.short}<br/>
Modified: ${attributeGroup.modified?string.short}<br/>
</p>
</#if>
<#if browser.attributeGroupActions.allowModify>
  <h2>Update Attribute Group</h2>
  <p>
  <form action='/environments/${environment.uid}/attributeGroups/${attributeGroup.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>

    <div class="columnBlock paddingTop clearfix">
    <div class="twoColSkinnyA alignRight"><label for="attributeGroupDefinitionUid">Attribute Group Definition:</label></div>
        <div class="twoColFatB">
            <select id="attributeGroupDefinitionUid" name="attributeGroupDefinitionUid" class="inputSelectStd">
              <#list attributeGroupDefinitions as agd>
                  <option value="${agd.uid}"<#if attributeGroup.attributeGroupDefinition.uid == agd.uid> selected="selected"</#if>>${agd.name}</option>
              </#list>
            </select>
        </div>
    </div>

    <div class="columnBlock paddingTop clearfix">
    <div class="twoColSkinnyA alignRight"><label for="name">Attribute Group Name:</label></div>
    <div class="twoColFatB">
        <input id="name" name="name" type="text" value="${attributeGroup.name?html}" maxlength="100" size="30" class="inputTextLong"/>
        <span class="errors"></span>
    </div>
    </div>

    <#include '/includes/editAttributes.ftl'>

    <br/><input type='submit' value="Update Attribute Group"/><br/>

  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>