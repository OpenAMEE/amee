<#include '/includes/before_content.ftl'>
<h1>User Upload</h1>
<p>
<form action='/environments/${environment.uid}/users/upload?method=put' method='POST' enctype='multipart/form-data'>
    Clone from: <select name="cloneUserUid">
        <option value="">(none)</option>
        <#list users as u>
            <option value="${u.uid}">${u.username}</option>
        </#list>
        </select><br/>
    File: <input type='file' name='userDataFile'/><br/><br/>
    <input type='submit' value="Upload"/>
</form>
</p>
<#include '/includes/after_content.ftl'>