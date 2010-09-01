<#assign sectionName = "admin">

<#include '/includes/before_content.ftl'>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> /
   <a href='/admin/groups'>Groups</a> /
   <a href='/admin/groups/${group.name}'>${group.name}</a></p>

<h2>Details</h2>

<p>
    Name: ${group.name}<br/>
    Created: ${group.created?string.short}<br/>
    Modified: ${group.modified?string.short}<br/>
</p>

<#if canModify()>
    <h2>Update Group</h2>
    <p>
        <form action='/admin/groups/${group.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' value='${group.name}' type='text' size='30'/><br/>
            Description: <textarea name='description' rows='5' cols='60'>${group.description}</textarea><br/><br/>
            <input type='submit' value="Update Group"/><br/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>